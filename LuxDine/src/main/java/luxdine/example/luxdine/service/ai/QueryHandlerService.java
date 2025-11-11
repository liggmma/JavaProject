package luxdine.example.luxdine.service.ai;

import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.service.reservation.CustomerReservationService;
import luxdine.example.luxdine.service.reservation.ReservationService;
import luxdine.example.luxdine.service.seating.TableService;
import luxdine.example.luxdine.service.user.UserService;
import luxdine.example.luxdine.common.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for extracting parameters from user queries and calling appropriate services
 * Handles date/time parsing, ID extraction, and service integration
 */
@Service
public class QueryHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(QueryHandlerService.class);

    private final ReservationService reservationService;
    private final CustomerReservationService customerReservationService;
    private final TableService tableService;
    private final UserService userService;

    @Autowired
    public QueryHandlerService(ReservationService reservationService,
                                CustomerReservationService customerReservationService,
                                TableService tableService,
                                UserService userService) {
        this.reservationService = reservationService;
        this.customerReservationService = customerReservationService;
        this.tableService = tableService;
        this.userService = userService;
    }

    /**
     * Extract reservation parameters from query
     */
    public static class ReservationParams {
        private OffsetDateTime dateTime;
        private Integer numberOfGuests;
        private boolean isValid;

        public ReservationParams(OffsetDateTime dateTime, Integer numberOfGuests, boolean isValid) {
            this.dateTime = dateTime;
            this.numberOfGuests = numberOfGuests;
            this.isValid = isValid;
        }

        public OffsetDateTime getDateTime() { return dateTime; }
        public Integer getNumberOfGuests() { return numberOfGuests; }
        public boolean isValid() { return isValid; }
    }

    /**
     * Extract reservation parameters from user query
     */
    public ReservationParams extractReservationParams(String query) {
        try {
            OffsetDateTime dateTime = extractDateTime(query);
            Integer guests = extractGuestCount(query);

            boolean isValid = dateTime != null && guests != null && guests > 0;
            return new ReservationParams(dateTime, guests, isValid);
        } catch (Exception e) {
            logger.error("Error extracting reservation params", e);
            return new ReservationParams(null, null, false);
        }
    }

    /**
     * Extract date and time from query (supports Vietnamese and English)
     */
    public OffsetDateTime extractDateTime(String query) {
        String lowerQuery = query.toLowerCase();
        ZoneId venueZone = DateTimeUtils.getVenueZoneId();
        LocalDate date = null;
        LocalTime time = null;
        boolean foundDate = false;
        boolean foundTime = false;

        // Parse date expressions
        if (lowerQuery.matches(".*(today|hôm nay|bữa nay|hơm nay).*")) {
            date = LocalDate.now();
            foundDate = true;
        } else if (lowerQuery.matches(".*(tomorrow|ngày mai|mai).*")) {
            date = LocalDate.now().plusDays(1);
            foundDate = true;
        } else if (lowerQuery.matches(".*(day after tomorrow|ngày kia|ngày mốt|mốt).*")) {
            date = LocalDate.now().plusDays(2);
            foundDate = true;
        } else if (lowerQuery.matches(".*(next week|tuần sau|tuần tới).*")) {
            date = LocalDate.now().plusWeeks(1);
            foundDate = true;
        } else if (lowerQuery.matches(".*(this week|tuần này).*")) {
            date = LocalDate.now();
            foundDate = true;
        } else {
            // Try to match Vietnamese weekday patterns: "thứ hai tuần sau", "thứ 6 này"
            Pattern viWeekdayPattern = Pattern.compile(
                "(thứ\\s*(hai|ba|tư|năm|sáu|bảy|2|3|4|5|6|7)|chủ nhật|chúa nhật)\\s*(tuần\\s*(sau|tới|này))?",
                Pattern.CASE_INSENSITIVE
            );
            Matcher viWeekdayMatcher = viWeekdayPattern.matcher(lowerQuery);

            if (viWeekdayMatcher.find()) {
                try {
                    String dayStr = viWeekdayMatcher.group(2) != null ? viWeekdayMatcher.group(2) : viWeekdayMatcher.group(1);
                    String weekRef = viWeekdayMatcher.group(4); // "sau", "tới", "này", or null

                    // Map Vietnamese weekday to DayOfWeek
                    java.time.DayOfWeek targetDay = switch (dayStr.toLowerCase()) {
                        case "hai", "2" -> java.time.DayOfWeek.MONDAY;
                        case "ba", "3" -> java.time.DayOfWeek.TUESDAY;
                        case "tư", "4" -> java.time.DayOfWeek.WEDNESDAY;
                        case "năm", "5" -> java.time.DayOfWeek.THURSDAY;
                        case "sáu", "6" -> java.time.DayOfWeek.FRIDAY;
                        case "bảy", "7" -> java.time.DayOfWeek.SATURDAY;
                        case "chủ nhật", "chúa nhật" -> java.time.DayOfWeek.SUNDAY;
                        default -> null;
                    };

                    if (targetDay != null) {
                        LocalDate today = LocalDate.now();
                        LocalDate targetDate = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay));

                        // If weekRef is "sau" or "tới", add a week
                        if (weekRef != null && (weekRef.equals("sau") || weekRef.equals("tới"))) {
                            if (targetDate.equals(today) || targetDate.isBefore(today.plusDays(7))) {
                                targetDate = targetDate.plusWeeks(1);
                            }
                        }
                        // If "này" (this week) and the day already passed, use next week
                        else if (targetDate.isBefore(today)) {
                            targetDate = targetDate.plusWeeks(1);
                        }

                        date = targetDate;
                        foundDate = true;
                        logger.debug("Parsed Vietnamese weekday: {}", date);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse Vietnamese weekday: {}", lowerQuery);
                }
            }

            // Try to match English weekday patterns: "next friday", "this monday"
            if (!foundDate) {
                Pattern enWeekdayPattern = Pattern.compile(
                    "(next|this)\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed|thu|fri|sat|sun)",
                    Pattern.CASE_INSENSITIVE
                );
                Matcher enWeekdayMatcher = enWeekdayPattern.matcher(lowerQuery);

                if (enWeekdayMatcher.find()) {
                    try {
                        String weekRef = enWeekdayMatcher.group(1).toLowerCase(); // "next" or "this"
                        String dayStr = enWeekdayMatcher.group(2).toLowerCase();

                        // Map English weekday to DayOfWeek
                        java.time.DayOfWeek targetDay = switch (dayStr) {
                            case "monday", "mon" -> java.time.DayOfWeek.MONDAY;
                            case "tuesday", "tue" -> java.time.DayOfWeek.TUESDAY;
                            case "wednesday", "wed" -> java.time.DayOfWeek.WEDNESDAY;
                            case "thursday", "thu" -> java.time.DayOfWeek.THURSDAY;
                            case "friday", "fri" -> java.time.DayOfWeek.FRIDAY;
                            case "saturday", "sat" -> java.time.DayOfWeek.SATURDAY;
                            case "sunday", "sun" -> java.time.DayOfWeek.SUNDAY;
                            default -> null;
                        };

                        if (targetDay != null) {
                            LocalDate today = LocalDate.now();
                            LocalDate targetDate = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay));

                            // If "next", always go to next week's occurrence
                            if (weekRef.equals("next")) {
                                if (targetDate.equals(today) || targetDate.isBefore(today.plusDays(7))) {
                                    targetDate = targetDate.plusWeeks(1);
                                }
                            }
                            // If "this" and the day already passed, use next week
                            else if (targetDate.isBefore(today)) {
                                targetDate = targetDate.plusWeeks(1);
                            }

                            date = targetDate;
                            foundDate = true;
                            logger.debug("Parsed English weekday: {}", date);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse English weekday: {}", lowerQuery);
                    }
                }
            }
            // Try to extract month name + day (e.g., "November 5th", "December 25th", "ngày 5 tháng 11")
            // English pattern
            Pattern monthDayPattern = Pattern.compile(
                "(january|february|march|april|may|june|july|august|september|october|november|december|" +
                "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+" +
                "(\\d{1,2})(?:st|nd|rd|th)?",
                Pattern.CASE_INSENSITIVE
            );
            Matcher monthDayMatcher = monthDayPattern.matcher(lowerQuery);

            // Vietnamese pattern: "ngày 5 tháng 11" or "5 tháng 11" or "tháng 11 ngày 5"
            Pattern viMonthDayPattern = Pattern.compile(
                "(?:ngày\\s+)?(\\d{1,2})\\s+tháng\\s+(\\d{1,2})|" +
                "tháng\\s+(\\d{1,2})\\s+(?:ngày\\s+)?(\\d{1,2})",
                Pattern.CASE_INSENSITIVE
            );

            // Try Vietnamese pattern first
            Matcher viMatcher = viMonthDayPattern.matcher(lowerQuery);
            if (viMatcher.find()) {
                try {
                    int day, month;
                    if (viMatcher.group(1) != null) {
                        // Pattern: ngày X tháng Y
                        day = Integer.parseInt(viMatcher.group(1));
                        month = Integer.parseInt(viMatcher.group(2));
                    } else {
                        // Pattern: tháng Y ngày X
                        month = Integer.parseInt(viMatcher.group(3));
                        day = Integer.parseInt(viMatcher.group(4));
                    }

                    // Determine year - if date is in the past this year, use next year
                    int year = LocalDate.now().getYear();
                    LocalDate tentativeDate = LocalDate.of(year, month, day);

                    if (tentativeDate.isBefore(LocalDate.now())) {
                        year++;
                        tentativeDate = LocalDate.of(year, month, day);
                    }

                    date = tentativeDate;
                    foundDate = true;
                    logger.debug("Parsed Vietnamese date: {}", date);
                } catch (Exception e) {
                    logger.warn("Failed to parse Vietnamese date from query: {}", query);
                }
            }
            // Try English pattern
            else if (monthDayMatcher.find()) {
                try {
                    String monthStr = monthDayMatcher.group(1).toLowerCase();
                    int day = Integer.parseInt(monthDayMatcher.group(2));

                    // Map month name to number
                    int month = switch (monthStr) {
                        case "january", "jan" -> 1;
                        case "february", "feb" -> 2;
                        case "march", "mar" -> 3;
                        case "april", "apr" -> 4;
                        case "may" -> 5;
                        case "june", "jun" -> 6;
                        case "july", "jul" -> 7;
                        case "august", "aug" -> 8;
                        case "september", "sep" -> 9;
                        case "october", "oct" -> 10;
                        case "november", "nov" -> 11;
                        case "december", "dec" -> 12;
                        default -> 0;
                    };

                    if (month > 0) {
                        // Determine year - if date is in the past this year, use next year
                        int year = LocalDate.now().getYear();
                        LocalDate tentativeDate = LocalDate.of(year, month, day);

                        if (tentativeDate.isBefore(LocalDate.now())) {
                            // Date already passed this year, use next year
                            year++;
                            tentativeDate = LocalDate.of(year, month, day);
                        }

                        date = tentativeDate;
                        foundDate = true;
                        logger.debug("Parsed English date from month name: {}", date);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse month/day from query: {}", query);
                }
            }

            // Try to extract specific date (dd/mm/yyyy or yyyy-mm-dd) if month name parsing failed
            if (!foundDate) {
                Pattern datePattern = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})");
                Matcher dateMatcher = datePattern.matcher(query);
                if (dateMatcher.find()) {
                    try {
                        int day = Integer.parseInt(dateMatcher.group(1));
                        int month = Integer.parseInt(dateMatcher.group(2));
                        int year = Integer.parseInt(dateMatcher.group(3));
                        date = LocalDate.of(year, month, day);
                        foundDate = true;
                    } catch (Exception e) {
                        logger.warn("Failed to parse date from query: {}", query);
                    }
                }
            }
        }

        // Parse time expressions
        // 7 PM, 7PM, 19:00, 7h, 7 giờ tối, lúc 7 giờ, vào 6h30, etc.
        // Priority: patterns with "giờ" or time-of-day keywords first to avoid matching date numbers
        Pattern timePatternWithKeyword = Pattern.compile(
            "(lúc|vào|vài|khoảng|around|at)?\\s*(\\d{1,2})(?::(\\d{2})|h(\\d{2}))?\\s*(?:([ap]m)|h|giờ)\\s*(sáng|trưa|chiều|tối|đêm)?",
            Pattern.CASE_INSENSITIVE
        );
        Matcher timeMatcherWithKeyword = timePatternWithKeyword.matcher(lowerQuery);
        
        // Also try pattern without "giờ" but with time-of-day keyword
        Pattern timePatternWithTOD = Pattern.compile(
            "(lúc|vào|vài|khoảng|around|at)?\\s*(\\d{1,2})(?::(\\d{2})|h(\\d{2}))?\\s*(sáng|trưa|chiều|tối|đêm)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher timeMatcherWithTOD = timePatternWithTOD.matcher(lowerQuery);
        
        // General time pattern (lower priority, may match date numbers)
        Pattern timePattern = Pattern.compile(
            "(lúc|vào|vài|khoảng|around|at)?\\s*(\\d{1,2})(?::(\\d{2})|h(\\d{2}))?\\s*(?:([ap]m)|h|giờ)?\\s*(sáng|trưa|chiều|tối|đêm)?",
            Pattern.CASE_INSENSITIVE
        );
        Matcher timeMatcher = timePattern.matcher(lowerQuery);

        Matcher selectedMatcher = null;
        // Priority 1: Pattern with "giờ" keyword
        if (timeMatcherWithKeyword.find()) {
            selectedMatcher = timeMatcherWithKeyword;
        }
        // Priority 2: Pattern with time-of-day keyword (sáng/trưa/chiều/tối/đêm)
        else if (timeMatcherWithTOD.find()) {
            selectedMatcher = timeMatcherWithTOD;
        }
        // Priority 3: General pattern (but only if it has AM/PM or is clearly a time)
        else if (timeMatcher.find()) {
            String ampm = timeMatcher.group(5);
            String viTimeOfDay = timeMatcher.group(6);
            // Only use general pattern if it has AM/PM or time-of-day keyword
            // This prevents matching date numbers like "20" from "20/11/2025"
            if (ampm != null || viTimeOfDay != null || 
                (timeMatcher.group(1) != null && (timeMatcher.group(3) != null || timeMatcher.group(4) != null))) {
                selectedMatcher = timeMatcher;
            }
        }

        if (selectedMatcher != null) {
            try {
                int hour = Integer.parseInt(selectedMatcher.group(2));
                // Support both "7:30" and "7h30" formats
                String minuteStr = selectedMatcher.group(3) != null ? selectedMatcher.group(3) : selectedMatcher.group(4);
                int minute = minuteStr != null ? Integer.parseInt(minuteStr) : 0;
                
                // Extract AM/PM and Vietnamese time-of-day based on which pattern matched
                String ampm = null;
                String viTimeOfDay = null;
                
                if (selectedMatcher == timeMatcherWithKeyword) {
                    // Pattern: (lúc|vào|...)?\s*(\d{1,2})(?::(\d{2})|h(\d{2}))?\s*(?:([ap]m)|h|giờ)\s*(sáng|trưa|chiều|tối|đêm)?
                    // Groups: 1=prefix, 2=hour, 3=minute:, 4=minuteh, 5=ampm, 6=tod
                    ampm = selectedMatcher.group(5);
                    viTimeOfDay = selectedMatcher.group(6);
                } else if (selectedMatcher == timeMatcherWithTOD) {
                    // Pattern: (lúc|vào|...)?\s*(\d{1,2})(?::(\d{2})|h(\d{2}))?\s*(sáng|trưa|chiều|tối|đêm)
                    // Groups: 1=prefix, 2=hour, 3=minute:, 4=minuteh, 5=tod
                    viTimeOfDay = selectedMatcher.group(5);
                } else {
                    // Pattern: (lúc|vào|...)?\s*(\d{1,2})(?::(\d{2})|h(\d{2}))?\s*(?:([ap]m)|h|giờ)?\s*(sáng|trưa|chiều|tối|đêm)?
                    // Groups: 1=prefix, 2=hour, 3=minute:, 4=minuteh, 5=ampm, 6=tod
                    ampm = selectedMatcher.group(5);
                    viTimeOfDay = selectedMatcher.group(6);
                }

                // Handle AM/PM
                if (ampm != null && ampm.equalsIgnoreCase("pm") && hour < 12) {
                    hour += 12;
                } else if (ampm != null && ampm.equalsIgnoreCase("am") && hour == 12) {
                    hour = 0;
                }

                // Handle Vietnamese time of day keywords
                if (viTimeOfDay != null) {
                    String tod = viTimeOfDay.toLowerCase();
                    if (tod.equals("sáng")) {
                        // Morning: 6 AM - 11 AM
                        if (hour >= 1 && hour <= 11) {
                            // Keep as is (already AM)
                        } else if (hour == 12) {
                            hour = 0; // 12 sáng = midnight
                        }
                    } else if (tod.equals("trưa")) {
                        // Noon: 12 PM - 1 PM
                        if (hour >= 1 && hour <= 11) {
                            hour += 12;
                        } else if (hour == 12) {
                            // 12 trưa = noon, keep as 12
                        }
                    } else if (tod.equals("chiều")) {
                        // Afternoon: 1 PM - 5 PM
                        if (hour >= 1 && hour <= 11) {
                            hour += 12;
                        } else if (hour == 12) {
                            hour = 12; // 12 chiều = noon
                        }
                    } else if (tod.equals("tối")) {
                        // Evening: 6 PM - 10 PM
                        // For "tối", always convert to PM if hour is 1-11
                        if (hour >= 1 && hour <= 11) {
                            hour += 12;
                        } else if (hour == 12) {
                            // 12 tối = midnight, but usually means 12 PM (noon) in Vietnamese
                            // However, "12 giờ tối" typically means midnight (0:00)
                            hour = 0;
                        }
                    } else if (tod.equals("đêm")) {
                        // Night: 11 PM - 5 AM
                        if (hour >= 1 && hour < 12) {
                            hour += 12;
                        } else if (hour == 12) {
                            hour = 0; // 12 đêm = midnight
                        }
                    }
                }

                // Validate hour and minute ranges
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    time = LocalTime.of(hour, minute);
                    foundTime = true;
                    logger.debug("Parsed time: {}:{} from query: {}", hour, minute, query);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse time from query: {}", query, e);
            }
        }

        // If neither date nor time was found, return null (let caller ask for details)
        if (!foundDate && !foundTime) {
            return null;
        }

        // If only time found, use today's date; if only date found and no time, default to 12:00
        if (date == null) {
            date = LocalDate.now();
        }
        if (time == null) {
            time = LocalTime.of(12, 0);
        }

        return LocalDateTime.of(date, time).atZone(venueZone).toOffsetDateTime();
    }

    /**
     * Extract number of guests from query (supports English and Vietnamese)
     */
    public Integer extractGuestCount(String query) {
        String lowerQuery = query.toLowerCase();

        // Pattern for "2 people", "2 guests", "4 người", "cho 4 khách", "bàn 4 người"
        Pattern guestPattern = Pattern.compile("(cho\\s+|bàn\\s+)?(\\d+)\\s*(people|guests|person|persons|người|khách|pax|ngườithì|khách)");
        Matcher matcher = guestPattern.matcher(lowerQuery);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse guest count: {}", matcher.group(2));
            }
        }

        // Try Vietnamese word numbers: "một người", "hai người", etc.
        java.util.Map<String, Integer> viNumbers = java.util.Map.ofEntries(
            java.util.Map.entry("một", 1),
            java.util.Map.entry("hai", 2),
            java.util.Map.entry("ba", 3),
            java.util.Map.entry("bốn", 4),
            java.util.Map.entry("tư", 4),
            java.util.Map.entry("năm", 5),
            java.util.Map.entry("sáu", 6),
            java.util.Map.entry("bảy", 7),
            java.util.Map.entry("bẩy", 7),
            java.util.Map.entry("tám", 8),
            java.util.Map.entry("chín", 9),
            java.util.Map.entry("mười", 10)
        );

        for (java.util.Map.Entry<String, Integer> entry : viNumbers.entrySet()) {
            Pattern viNumberPattern = Pattern.compile(
                "\\b" + entry.getKey() + "\\s+(người|khách|pax)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher viMatcher = viNumberPattern.matcher(lowerQuery);
            if (viMatcher.find()) {
                return entry.getValue();
            }
        }

        // Try English word numbers: "one person", "two guests", etc.
        java.util.Map<String, Integer> enNumbers = java.util.Map.ofEntries(
            java.util.Map.entry("one", 1),
            java.util.Map.entry("two", 2),
            java.util.Map.entry("three", 3),
            java.util.Map.entry("four", 4),
            java.util.Map.entry("five", 5),
            java.util.Map.entry("six", 6),
            java.util.Map.entry("seven", 7),
            java.util.Map.entry("eight", 8),
            java.util.Map.entry("nine", 9),
            java.util.Map.entry("ten", 10)
        );

        for (java.util.Map.Entry<String, Integer> entry : enNumbers.entrySet()) {
            Pattern enNumberPattern = Pattern.compile(
                "\\b" + entry.getKey() + "\\s+(people|guests?|persons?|pax)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher enMatcher = enNumberPattern.matcher(lowerQuery);
            if (enMatcher.find()) {
                return entry.getValue();
            }
        }

        // Try "for X" pattern: "for 4", "cho 4"
        Pattern forPattern = Pattern.compile("(for|cho)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher forMatcher = forPattern.matcher(lowerQuery);
        if (forMatcher.find()) {
            try {
                int num = Integer.parseInt(forMatcher.group(2));
                if (num >= 1 && num <= 20) {
                    return num;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Try standalone numbers if context suggests booking
        if (lowerQuery.matches(".*(book|reserve|đặt|bàn|table).*")) {
            Pattern numberPattern = Pattern.compile("\\b(\\d+)\\b");
            Matcher numberMatcher = numberPattern.matcher(lowerQuery);
            if (numberMatcher.find()) {
                try {
                    int num = Integer.parseInt(numberMatcher.group(1));
                    if (num >= 1 && num <= 20) { // Reasonable guest count
                        return num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        return null;
    }

    /**
     * Extract order or reservation ID from query
     */
    public Long extractOrderId(String query) {
        // Pattern for "order #123", "reservation 456", "đơn hàng 789"
        Pattern idPattern = Pattern.compile("(?:order|reservation|đơn hàng|đặt chỗ|mã)\\s*[#:]?\\s*(\\d+)");
        Matcher matcher = idPattern.matcher(query.toLowerCase());

        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse order ID: {}", matcher.group(1));
            }
        }

        return null;
    }

    /**
     * Extract reservation code from query
     */
    public String extractReservationCode(String query) {
        // Pattern for reservation codes (e.g., "RES-20250103-001")
        Pattern codePattern = Pattern.compile("(RES-\\d{8}-\\d{3})");
        Matcher matcher = codePattern.matcher(query.toUpperCase());

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extract allergen mentions from query
     */
    public List<String> extractAllergens(String query) {
        List<String> allergens = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // Common allergens in English and Vietnamese
        Map<String, String> allergenKeywords = Map.ofEntries(
            Map.entry("peanut", "Peanuts"),
            Map.entry("đậu phộng", "Peanuts"),
            Map.entry("shellfish", "Shellfish"),
            Map.entry("hải sản", "Shellfish"),
            Map.entry("tôm", "Shellfish"),
            Map.entry("cua", "Shellfish"),
            Map.entry("dairy", "Dairy"),
            Map.entry("milk", "Dairy"),
            Map.entry("sữa", "Dairy"),
            Map.entry("gluten", "Gluten"),
            Map.entry("wheat", "Gluten"),
            Map.entry("lúa mì", "Gluten"),
            Map.entry("egg", "Eggs"),
            Map.entry("trứng", "Eggs"),
            Map.entry("soy", "Soy"),
            Map.entry("đậu nành", "Soy"),
            Map.entry("fish", "Fish"),
            Map.entry("cá", "Fish"),
            Map.entry("tree nut", "Tree Nuts"),
            Map.entry("hạt", "Tree Nuts")
        );

        for (Map.Entry<String, String> entry : allergenKeywords.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                allergens.add(entry.getValue());
            }
        }

        return allergens.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Check table availability for given date/time and guests
     */
    public List<Map<String, Object>> checkTableAvailability(OffsetDateTime dateTime, Integer numberOfGuests) {
        try {
            if (dateTime == null || numberOfGuests == null || numberOfGuests <= 0) {
                return List.of();
            }

            // Check operating hours
            LocalTime time = dateTime.toLocalTime();
            if (!DateTimeUtils.isWithinOperatingHours(time)) {
                return List.of();
            }

            // Window for checking conflicts: ±120 minutes
            final int WINDOW_MINUTES = 120;
            OffsetDateTime start = dateTime.minusMinutes(WINDOW_MINUTES);
            OffsetDateTime end = dateTime.plusMinutes(WINDOW_MINUTES);

            // Get candidate tables: AVAILABLE + RESERVED with capacity >= guests
            List<Tables> available = tableService.findByStatus(TableStatus.AVAILABLE);
            List<Tables> reserved = tableService.findByStatus(TableStatus.RESERVED);

            List<Tables> candidates = Stream.concat(available.stream(), reserved.stream())
                    .filter(t -> t.getCapacity() >= numberOfGuests)
                    .toList();

            // Filter out tables with conflicts
            return candidates.stream()
                    .filter(t -> {
                        List<Reservations> conflicts =
                                customerReservationService.findConflictsByTableIdAndWindow(t.getId(), start, end);
                        return conflicts.isEmpty();
                    })
                    .map(t -> {
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("id", t.getId());
                        tableInfo.put("tableName", t.getTableName());
                        tableInfo.put("capacity", t.getCapacity());
                        tableInfo.put("depositAmount", t.getDepositAmount());
                        tableInfo.put("tableType", t.getTableType());
                        tableInfo.put("areaName", t.getArea() != null ? t.getArea().getName() : "Unknown");
                        return tableInfo;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error checking table availability", e);
            return List.of();
        }
    }

    /**
     * Get reservation by ID
     */
    public Reservations getReservationById(Long id) {
        try {
            return reservationService.getById(id);
        } catch (Exception e) {
            logger.error("Error getting reservation by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Get reservation by code
     */
    public Reservations getReservationByCode(String code) {
        try {
            // Note: ReservationService might need a method to get by code
            // For now, return null - this can be implemented later
            logger.warn("Get reservation by code not implemented yet: {}", code);
            return null;
        } catch (Exception e) {
            logger.error("Error getting reservation by code: {}", code, e);
            return null;
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", userId, e);
            return null;
        }
    }

    /**
     * Extract price range from query
     */
    public static class PriceRange {
        private Double min;
        private Double max;
        private boolean isValid;

        public PriceRange(Double min, Double max, boolean isValid) {
            this.min = min;
            this.max = max;
            this.isValid = isValid;
        }

        public Double getMin() { return min; }
        public Double getMax() { return max; }
        public boolean isValid() { return isValid; }
    }

    /**
     * Extract price range from query
     */
    public PriceRange extractPriceRange(String query) {
        String lowerQuery = query.toLowerCase();

        // Pattern for "under 100k", "dưới 100k", "below $50"
        Pattern underPattern = Pattern.compile("(?:under|dưới|below|less than)\\s*\\$?(\\d+)(?:k|000)?");
        Matcher underMatcher = underPattern.matcher(lowerQuery);
        if (underMatcher.find()) {
            double max = Double.parseDouble(underMatcher.group(1));
            if (lowerQuery.contains("k") || lowerQuery.contains("000")) {
                max *= 1000;
            }
            return new PriceRange(0.0, max, true);
        }

        // Pattern for "over 100k", "trên 100k", "above $50"
        Pattern overPattern = Pattern.compile("(?:over|trên|above|more than)\\s*\\$?(\\d+)(?:k|000)?");
        Matcher overMatcher = overPattern.matcher(lowerQuery);
        if (overMatcher.find()) {
            double min = Double.parseDouble(overMatcher.group(1));
            if (lowerQuery.contains("k") || lowerQuery.contains("000")) {
                min *= 1000;
            }
            return new PriceRange(min, Double.MAX_VALUE, true);
        }

        // Pattern for "between 50k and 100k", "từ 50k đến 100k"
        Pattern rangePattern = Pattern.compile("(?:between|từ)\\s*\\$?(\\d+)(?:k|000)?\\s*(?:and|đến|-)\\s*\\$?(\\d+)(?:k|000)?");
        Matcher rangeMatcher = rangePattern.matcher(lowerQuery);
        if (rangeMatcher.find()) {
            double min = Double.parseDouble(rangeMatcher.group(1));
            double max = Double.parseDouble(rangeMatcher.group(2));
            if (lowerQuery.contains("k") || lowerQuery.contains("000")) {
                min *= 1000;
                max *= 1000;
            }
            return new PriceRange(min, max, true);
        }

        return new PriceRange(null, null, false);
    }
}
