package luxdine.example.luxdine.service.ai;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.service.catalog.ItemsService;
import luxdine.example.luxdine.service.config.RestaurantConfigService;
import luxdine.example.luxdine.service.seating.TableService;
import luxdine.example.luxdine.service.user.UserService;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced service for building context data for the AI chatbot
 * Provides comprehensive context for 12 different query categories
 */
@Service
public class ChatbotContextService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotContextService.class);
    private static final NumberFormat vndFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final ItemsService itemsService;
    private final TableService tableService;
    private final RestaurantConfigService restaurantConfig;
    private final QueryHandlerService queryHandler;
    private final UserService userService;
    private final DatabaseAccessService databaseAccess;
    private final DatabaseQueryAnalyzer queryAnalyzer;

    // In-memory storage for reservation chat states (with TTL)
    private final Map<String, ReservationChatState> reservationStates = new ConcurrentHashMap<>();

    @Autowired
    public ChatbotContextService(ItemsService itemsService,
                                  TableService tableService,
                                  RestaurantConfigService restaurantConfig,
                                  QueryHandlerService queryHandler,
                                  UserService userService,
                                  DatabaseAccessService databaseAccess,
                                  DatabaseQueryAnalyzer queryAnalyzer) {
        this.itemsService = itemsService;
        this.tableService = tableService;
        this.restaurantConfig = restaurantConfig;
        this.queryHandler = queryHandler;
        this.userService = userService;
        this.databaseAccess = databaseAccess;
        this.queryAnalyzer = queryAnalyzer;
    }

    /**
     * Format price to VND with thousand separators
     */
    private String formatPriceVND(double price) {
        return vndFormatter.format(Math.round(price)) + " VND";
    }

    // ==================== RESERVATION CHAT STATE MANAGEMENT ====================

    /**
     * Create or get a reservation chat state for a session
     */
    public ReservationChatState getOrCreateReservationState(String sessionId, Long userId, String username) {
        // Clean up expired states
        cleanupExpiredReservationStates();

        return reservationStates.computeIfAbsent(sessionId, id -> {
            logger.info("Creating new reservation chat state for session: {}", id);
            ReservationChatState state = new ReservationChatState(id);
            state.setUserId(userId);
            state.setUsername(username);
            state.setLoggedIn(userId != null);
            return state;
        });
    }

    /**
     * Get an existing reservation state
     */
    public ReservationChatState getReservationState(String sessionId) {
        return reservationStates.get(sessionId);
    }

    /**
     * Update a reservation state
     */
    public void updateReservationState(String sessionId, ReservationChatState state) {
        if (state != null) {
            state.touch();
            reservationStates.put(sessionId, state);
        }
    }

    /**
     * Clear a reservation state (when booking is completed or cancelled)
     */
    public void clearReservationState(String sessionId) {
        ReservationChatState removed = reservationStates.remove(sessionId);
        if (removed != null) {
            logger.info("Cleared reservation state for session: {}", sessionId);
        }
    }

    /**
     * Clean up expired reservation states (older than 15 minutes)
     */
    public void cleanupExpiredReservationStates() {
        reservationStates.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                logger.info("Removing expired reservation state for session: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Check if a session has an active reservation booking in progress
     */
    public boolean hasActiveReservationBooking(String sessionId) {
        ReservationChatState state = reservationStates.get(sessionId);
        return state != null && !state.isExpired() &&
               state.getCurrentStep() != ReservationChatState.ConversationStep.COMPLETED;
    }

    /**
     * Build context for specific query type with optional user context
     *
     * @param queryType Type of query
     * @param userId Optional user ID for personalized context
     * @param userQuery Original user query for parameter extraction
     * @return Formatted context string
     */
    @Transactional(readOnly = true)
    public String buildContextForQuery(String queryType, Long userId, String userQuery) {
        StringBuilder context = new StringBuilder();

        // Add user-specific context if available
        if (userId != null) {
            context.append(buildUserContext(userId)).append("\n");
        }

        // Analyze query to determine what data to access
        DatabaseQueryAnalyzer.QueryIntent queryIntent = queryAnalyzer.analyzeQuery(userQuery, userId);

        // Add query-specific context
        context.append(switch (queryType.toLowerCase()) {
            case "restaurant_info" -> buildRestaurantInfoContext();
            case "menu" -> buildMenuDetailedContext(userQuery);
            case "reservations" -> buildReservationContext(userQuery);
            case "orders" -> buildOrdersFullContext(userId, queryIntent);
            case "payment", "payments" -> buildPaymentsFullContext(userId);
            case "allergies" -> buildAllergyContext(userQuery, userId);
            case "suggestions" -> buildSuggestionContext(userQuery);
            case "service" -> buildServiceContext();
            case "pricing" -> buildPricingContext(userQuery);
            case "schedule" -> buildScheduleFullContext(userId, queryIntent);
            case "support" -> buildSupportContext();
            case "feedback" -> buildFeedbackFullContext(userId);
            case "bundles" -> buildBundlesFullContext();
            case "attendance" -> buildAttendanceFullContext(userId);
            case "vip_tiers", "vip" -> buildVipTiersFullContext();
            case "banners" -> buildBannersFullContext();
            case "chat" -> buildChatHistoryFullContext(userId);
            case "categories" -> buildCategoriesFullContext();
            case "work_schedule" -> buildScheduleFullContext(userId, queryIntent);
            default -> buildDynamicContext(queryIntent, userId, userQuery);
        });

        return context.toString();
    }

    /**
     * Build context for specific query type (backward compatibility)
     */
    public String buildContextForQuery(String queryType) {
        return buildContextForQuery(queryType, null, "");
    }

    /**
     * 1. Restaurant Info Context - hours, address, phone, parking, WiFi
     */
    private String buildRestaurantInfoContext() {
        return restaurantConfig.getBasicInfo() + "\n" +
               restaurantConfig.getFacilitiesInfo() + "\n" +
               "DIRECTIONS: Easy access from District 1 main roads, near Ben Thanh Market\n";
    }

    /**
     * 2. Menu Context - detailed menu with prices, descriptions, categories, bestsellers
     * Enhanced menu context with search capability
     */
    private String buildMenuDetailedContext(String userQuery) {
        try {
            StringBuilder menuContext = new StringBuilder();
            menuContext.append("COMPLETE MENU INFORMATION:\n\n");

            // Get all public items
            List<Items> allItems = itemsService.getAllPublicItems();
            if (allItems == null || allItems.isEmpty()) {
                return "MENU INFORMATION: No menu items available at this time.\n";
            }

            // Calculate price statistics
            double minPrice = allItems.stream().mapToDouble(Items::getPrice).min().orElse(0);
            double maxPrice = allItems.stream().mapToDouble(Items::getPrice).max().orElse(0);
            double avgPrice = allItems.stream().mapToDouble(Items::getPrice).average().orElse(0);

            menuContext.append(String.format("Total menu items: %d\n", allItems.size()));
            menuContext.append(String.format("Price range: %s - %s\n", formatPriceVND(minPrice), formatPriceVND(maxPrice)));
            menuContext.append(String.format("Average price: %s\n\n", formatPriceVND(avgPrice)));

            // Check if user is searching for specific dish
            String searchTerm = extractDishSearchTerm(userQuery);
            if (searchTerm != null && !searchTerm.isEmpty()) {
                List<Items> matchingItems = allItems.stream()
                        .filter(item -> item.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                        .toList();

                if (!matchingItems.isEmpty()) {
                    menuContext.append(String.format("SEARCH RESULTS for '%s' (%d found):\n", searchTerm, matchingItems.size()));
                    for (Items item : matchingItems) {
                        menuContext.append(String.format("- %s: %s\n",
                                item.getName(),
                                formatPriceVND(item.getPrice())));
                        if (item.getDescription() != null) {
                            menuContext.append(String.format("  Description: %s\n", item.getDescription()));
                        }
                        if (item.getCategory() != null) {
                            menuContext.append(String.format("  Category: %s\n", item.getCategory().getName()));
                        }
                    }
                    menuContext.append("\n");
                } else {
                    menuContext.append(String.format("No dishes found matching '%s'\n\n", searchTerm));
                }
            }

            // Show all items organized by category
            menuContext.append("ALL MENU ITEMS (by category):\n");
            Map<String, List<Items>> itemsByCategory = allItems.stream()
                    .collect(Collectors.groupingBy(item ->
                            item.getCategory() != null ? item.getCategory().getName() : "Other"));

            for (Map.Entry<String, List<Items>> entry : itemsByCategory.entrySet()) {
                menuContext.append(String.format("\n%s:\n", entry.getKey().toUpperCase()));
                for (Items item : entry.getValue().stream().limit(20).toList()) {
                    menuContext.append(String.format("  - %s: %s",
                            item.getName(),
                            formatPriceVND(item.getPrice())));
                    if (item.getDescription() != null && item.getDescription().length() < 50) {
                        menuContext.append(String.format(" - %s", item.getDescription()));
                    }
                    menuContext.append("\n");
                }
                if (entry.getValue().size() > 20) {
                    menuContext.append(String.format("  ... and %d more items\n", entry.getValue().size() - 20));
                }
            }

            // Add best sellers
            List<Items> bestSellers = itemsService.getAllBestSellerItems();
            if (bestSellers != null && !bestSellers.isEmpty()) {
                menuContext.append("\n\nBEST SELLERS (Most Popular):\n");
                for (Items item : bestSellers) {
                    menuContext.append(String.format("- %s (%s) - Sold %d times\n",
                            item.getName(),
                            formatPriceVND(item.getPrice()),
                            item.getSoldCount()));
                }
            }

            // Show cheapest and most expensive items
            Items cheapest = allItems.stream().min(Comparator.comparingDouble(Items::getPrice)).orElse(null);
            Items mostExpensive = allItems.stream().max(Comparator.comparingDouble(Items::getPrice)).orElse(null);

            menuContext.append("\n\nPRICE HIGHLIGHTS:\n");
            if (cheapest != null) {
                menuContext.append(String.format("Cheapest: %s (%s)\n", cheapest.getName(), formatPriceVND(cheapest.getPrice())));
            }
            if (mostExpensive != null) {
                menuContext.append(String.format("Most Expensive: %s (%s)\n", mostExpensive.getName(), formatPriceVND(mostExpensive.getPrice())));
            }

            menuContext.append("\nSPECIAL NOTES:\n");
            menuContext.append("- All dishes are prepared fresh daily\n");
            menuContext.append("- We can accommodate dietary restrictions\n");
            menuContext.append("- Ask about daily specials and combo meals\n");

            return menuContext.toString();

        } catch (Exception e) {
            logger.error("Error building menu context", e);
            return "MENU INFORMATION: Available upon request. Please ask staff for details.\n";
        }
    }

    /**
     * Extract dish name from search query
     */
    private String extractDishSearchTerm(String query) {
        if (query == null || query.isEmpty()) return null;

        String lowerQuery = query.toLowerCase();

        // Patterns to extract dish name
        String[] searchPatterns = {
                "do you have (\\w+)",
                "have you got (\\w+)",
                "show me (\\w+)",
                "what's.*?(\\w+) cost",
                "price of (\\w+)",
                "how much.*?(\\w+)",
                "i want (\\w+)",
                "có (\\w+) không",
                "món (\\w+)",
                "giá (\\w+)"
        };

        for (String pattern : searchPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(lowerQuery);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }

    /**
     * 3. Reservation Context - availability, policies, how to book
     */
    private String buildReservationContext(String userQuery) {
        StringBuilder context = new StringBuilder();
        context.append(restaurantConfig.getReservationPolicies()).append("\n");

        boolean foundSpecificAvailability = false;

        // Try to extract reservation parameters and check availability
        if (userQuery != null && !userQuery.isEmpty()) {
            QueryHandlerService.ReservationParams params = queryHandler.extractReservationParams(userQuery);
            if (params.isValid()) {
                foundSpecificAvailability = true;
                List<Map<String, Object>> availableTables =
                    queryHandler.checkTableAvailability(params.getDateTime(), params.getNumberOfGuests());

                if (!availableTables.isEmpty()) {
                    context.append(String.format("\nAVAILABLE TABLES for %s with %d guests:\n",
                            params.getDateTime().toString(), params.getNumberOfGuests()));
                    for (Map<String, Object> table : availableTables) {
                        context.append(String.format("- %s (Capacity: %d, Area: %s)\n",
                                table.get("tableName"),
                                table.get("capacity"),
                                table.get("areaName")));
                    }
                } else {
                    context.append("\nNo tables available for the requested time. Suggest alternative times.\n");
                }
            }
        }

        // If no specific time requested, show general table availability
        if (!foundSpecificAvailability) {
            try {
                List<TableResponse> allTables = tableService.getAllTables(null);
                if (allTables != null && !allTables.isEmpty()) {
                    List<TableResponse> availableTables = allTables.stream()
                            .filter(table -> "AVAILABLE".equals(table.getStatus()))
                            .toList();

                    context.append(String.format("\nCURRENT TABLE AVAILABILITY:\n"));
                    context.append(String.format("- Total tables: %d\n", allTables.size()));
                    context.append(String.format("- Available now: %d tables\n", availableTables.size()));

                    if (!availableTables.isEmpty()) {
                        context.append("\nAVAILABLE TABLES RIGHT NOW:\n");
                        for (TableResponse table : availableTables.stream().limit(10).toList()) {
                            context.append(String.format("- %s: Capacity %d people, Type: %s, Area: %s\n",
                                    table.getTableName(),
                                    table.getCapacity(),
                                    table.getTableType() != null ? table.getTableType() : "Standard",
                                    table.getAreaName() != null ? table.getAreaName() : "Main"));
                        }
                        if (availableTables.size() > 10) {
                            context.append(String.format("... and %d more tables available\n",
                                    availableTables.size() - 10));
                        }
                    } else {
                        context.append("\nAll tables are currently reserved. Please specify a future date/time to check availability.\n");
                    }
                }
            } catch (Exception e) {
                logger.error("Error checking general table availability", e);
            }
        }

        context.append("\nHOW TO BOOK:\n");
        context.append("- Use our online booking system\n");
        context.append("- Call us at ").append(restaurantConfig.getRestaurantPhone()).append("\n");
        context.append("- You will receive a confirmation email with reservation code\n");

        return context.toString();
    }


    /**
     * 6. Allergy Context - filter items by allergen, dietary restrictions
     */
    private String buildAllergyContext(String userQuery, Long userId) {
        StringBuilder context = new StringBuilder();
        context.append("ALLERGEN & DIETARY INFORMATION:\n\n");

        // Check user's registered allergens
        if (userId != null) {
            try {
                User user = userService.getUserById(userId);
                if (user != null && user.getAllergens() != null && !user.getAllergens().isEmpty()) {
                    context.append("YOUR REGISTERED ALLERGENS:\n");
                    for (String allergen : user.getAllergens()) {
                        context.append("- ").append(allergen).append("\n");
                    }
                    context.append("\n");
                }
            } catch (Exception e) {
                logger.error("Error fetching user allergens", e);
            }
        }

        // Extract allergens from query
        List<String> mentionedAllergens = queryHandler.extractAllergens(userQuery);
        if (!mentionedAllergens.isEmpty()) {
            context.append("ALLERGENS MENTIONED IN QUERY:\n");
            for (String allergen : mentionedAllergens) {
                context.append("- ").append(allergen).append("\n");
            }
            context.append("\n");
        }

        context.append("COMMON ALLERGENS WE TRACK:\n");
        context.append("- Peanuts, Tree nuts\n");
        context.append("- Shellfish, Fish\n");
        context.append("- Dairy, Eggs\n");
        context.append("- Gluten, Soy\n\n");

        context.append("DIETARY OPTIONS:\n");
        context.append("- Vegetarian dishes available\n");
        context.append("- Vegan options available\n");
        context.append("- Gluten-free alternatives available\n");
        context.append("- Please inform staff of any allergies when ordering\n");

        return context.toString();
    }

    /**
     * 7. Suggestion Context - recommendations, combos, pairings
     */
    private String buildSuggestionContext(String userQuery) {
        StringBuilder context = new StringBuilder();
        context.append("RECOMMENDATIONS & SUGGESTIONS:\n\n");

        // Get best sellers
        try {
            List<Items> bestSellers = itemsService.getAllBestSellerItems();
            if (bestSellers != null && !bestSellers.isEmpty()) {
                context.append("TOP RECOMMENDATIONS (Customer Favorites):\n");
                for (int i = 0; i < Math.min(5, bestSellers.size()); i++) {
                    Items item = bestSellers.get(i);
                    context.append(String.format("%d. %s (%s) - Highly popular!\n",
                            i + 1, item.getName(), formatPriceVND(item.getPrice())));
                }
                context.append("\n");
            }
        } catch (Exception e) {
            logger.error("Error fetching best sellers", e);
        }

        // Get combo deals from database if available
        try {
            var bundles = databaseAccess.getActiveBundles();
            if (bundles != null && !bundles.isEmpty()) {
                context.append("COMBO DEALS:\n");
                for (var bundle : bundles.stream().limit(5).toList()) {
                    context.append(String.format("- %s: %s\n",
                            bundle.getName() != null ? bundle.getName() : "Special Combo",
                            formatPriceVND(bundle.getPrice())));
                }
                context.append("\n");
            } else {
                context.append("Ask about our special combo deals and packages!\n\n");
            }
        } catch (Exception e) {
            logger.error("Error fetching bundles for suggestion context", e);
            context.append("Ask about our special combo deals!\n\n");
        }

        context.append("PAIRING SUGGESTIONS:\n");
        context.append("- Seafood dishes: Pair with white wine or light beer\n");
        context.append("- Grilled meats: Red wine or craft beer\n");
        context.append("- Spicy dishes: Fresh lime juice or iced tea\n\n");

        context.append("OCCASION-BASED RECOMMENDATIONS:\n");
        context.append("- Date night: Private dining room + Chef's special tasting menu\n");
        context.append("- Family gathering: Family-style sharing platters\n");
        context.append("- Business lunch: Quick lunch sets (ready in 15 mins)\n");
        context.append("- Celebration: Birthday/Anniversary packages available\n");

        return context.toString();
    }

    /**
     * 8. Service Context - takeout, delivery, wait times
     */
    private String buildServiceContext() {
        return restaurantConfig.getServicesInfo() + "\n" +
               "SERVICE DETAILS:\n" +
               "- Dine-in: Full service, average wait time 10-15 minutes\n" +
               "- Takeout: Call ahead for faster pickup (ready in 15-20 mins)\n" +
               "- Delivery: Available within 5km radius, 30-45 mins delivery time\n" +
               "- Catering: Minimum 20 people, order 48 hours in advance\n\n" +
               "WAIT TIMES:\n" +
               "- Peak hours (12-2 PM, 6-8 PM): 15-20 minute wait for tables\n" +
               "- Off-peak hours: Immediate seating usually available\n" +
               "- Reservations: Priority seating, minimal wait\n";
    }

    /**
     * 9. Pricing Context - combos, average cost, bundles
     */
    private String buildPricingContext(String userQuery) {
        StringBuilder context = new StringBuilder();
        context.append("PRICING INFORMATION:\n\n");

        try {
            List<Items> allItems = itemsService.getAllPublicItems();
            if (allItems != null && !allItems.isEmpty()) {
                double avgPrice = allItems.stream()
                        .mapToDouble(Items::getPrice)
                        .average()
                        .orElse(0.0);
                double minPrice = allItems.stream()
                        .mapToDouble(Items::getPrice)
                        .min()
                        .orElse(0.0);
                double maxPrice = allItems.stream()
                        .mapToDouble(Items::getPrice)
                        .max()
                        .orElse(0.0);

                context.append(String.format("MENU PRICE RANGE: %s - %s\n", formatPriceVND(minPrice), formatPriceVND(maxPrice)));
                context.append(String.format("AVERAGE DISH PRICE: %s\n\n", formatPriceVND(avgPrice)));

                // Check if user asking for price range
                QueryHandlerService.PriceRange priceRange = queryHandler.extractPriceRange(userQuery);
                if (priceRange != null && priceRange.isValid()) {
                    double min = priceRange.getMin() != null ? priceRange.getMin() : 0;
                    double max = priceRange.getMax() != null ? priceRange.getMax() : Double.MAX_VALUE;

                    List<Items> itemsInRange = allItems.stream()
                            .filter(item -> item.getPrice() >= min && item.getPrice() <= max)
                            .toList();

                    if (!itemsInRange.isEmpty()) {
                        context.append(String.format("DISHES IN PRICE RANGE %s - %s (%d items):\n",
                                formatPriceVND(min), formatPriceVND(max < Double.MAX_VALUE ? max : maxPrice), itemsInRange.size()));
                        for (Items item : itemsInRange.stream().limit(15).toList()) {
                            context.append(String.format("- %s: %s\n",
                                    item.getName(), formatPriceVND(item.getPrice())));
                        }
                        if (itemsInRange.size() > 15) {
                            context.append(String.format("... and %d more items\n", itemsInRange.size() - 15));
                        }
                        context.append("\n");
                    }
                }

                // Show cheapest items
                List<Items> cheapest = allItems.stream()
                        .sorted(Comparator.comparingDouble(Items::getPrice))
                        .limit(5)
                        .toList();
                context.append("CHEAPEST ITEMS:\n");
                for (Items item : cheapest) {
                    context.append(String.format("- %s: %s\n", item.getName(), formatPriceVND(item.getPrice())));
                }
                context.append("\n");
            }
        } catch (Exception e) {
            logger.error("Error calculating prices", e);
        }

        // Calculate estimated costs based on average menu prices
        context.append("ESTIMATED COSTS:\n");
        try {
            List<Items> allItems = itemsService.getAllPublicItems();
            if (allItems != null && !allItems.isEmpty()) {
                double avgPrice = allItems.stream().mapToDouble(Items::getPrice).average().orElse(20000.0);
                context.append(String.format("- Solo meal: %s-%s per person\n", formatPriceVND(avgPrice * 0.8), formatPriceVND(avgPrice * 1.5)));
                context.append(String.format("- Couple dining: %s-%s total\n", formatPriceVND(avgPrice * 1.5), formatPriceVND(avgPrice * 3)));
                context.append(String.format("- Family of 4: %s-%s total\n", formatPriceVND(avgPrice * 3), formatPriceVND(avgPrice * 6)));
                context.append(String.format("- Large group (10+): %s+ (group discounts available)\n\n", formatPriceVND(avgPrice * 10)));
            } else {
                context.append("- Prices vary by selection. Please check our menu for details.\n\n");
            }
        } catch (Exception e) {
            logger.error("Error calculating estimated costs", e);
            context.append("- Prices vary by selection. Please check our menu for details.\n\n");
        }

        // Combo deals already handled above
        context.append("For special packages and group dining, please ask for more details!\n\n");

        context.append("ADDITIONAL COSTS:\n");
        context.append("- Service charge: Included in prices\n");
        context.append("- Tax: Included in menu prices\n");
        context.append("- Delivery fee: 30,000 - 50,000 VND depending on distance\n");

        return context.toString();
    }


    /**
     * 11. Support Context - help, contact, common issues
     */
    private String buildSupportContext() {
        return "CUSTOMER SUPPORT:\n\n" +
               "CONTACT INFORMATION:\n" +
               "- Phone: " + restaurantConfig.getRestaurantPhone() + "\n" +
               "- Email: " + restaurantConfig.getRestaurantEmail() + "\n" +
               "- Address: " + restaurantConfig.getRestaurantAddress() + "\n" +
               "- Live Chat: Available on website during business hours\n\n" +
               "COMMON INQUIRIES:\n" +
               "- Reservation changes: Call at least 2 hours before\n" +
               "- Order modifications: Contact us immediately\n" +
               "- Complaints: Ask for manager on duty\n" +
               "- Feedback: Email or fill out feedback form\n\n" +
               "EMERGENCY CONTACT:\n" +
               "- For urgent issues during your visit, ask any staff member\n" +
               "- For food allergies/reactions, inform staff immediately\n\n" +
               "FOLLOW US:\n" +
               "- Facebook, Instagram, Twitter: @LuxDine\n" +
               "- Website: www.luxdine.com\n";
    }

    /**
     * 12. User Context - personal info, reservations, points, VIP tier
     */
    private String buildUserContext(Long userId) {
        if (userId == null) return "";

        try {
            User user = userService.getUserById(userId);
            if (user == null) return "";

            StringBuilder context = new StringBuilder();
            context.append("USER PROFILE:\n");
            context.append(String.format("Name: %s %s\n", user.getFirstName(), user.getLastName()));
            context.append(String.format("Email: %s\n", user.getEmail()));
            context.append(String.format("Phone: %s\n", user.getPhoneNumber()));
            context.append(String.format("Reward Points: %d\n", user.getRewardPoints()));

            if (user.getVipTier() != null) {
                context.append(String.format("VIP Tier: %s\n", user.getVipTier().getTierName()));
            }

            // Safely access allergens collection - handle LazyInitializationException
            try {
                if (user.getAllergens() != null && !user.getAllergens().isEmpty()) {
                    context.append("Registered Allergens: ").append(String.join(", ", user.getAllergens())).append("\n");
                }
            } catch (LazyInitializationException e) {
                // If allergens collection is not initialized, just skip it
                logger.debug("Allergens collection not initialized for user: {}", userId);
            }

            context.append("\n");
            return context.toString();

        } catch (Exception e) {
            logger.error("Error building user context for userId: {}", userId, e);
            return "";
        }
    }

    /**
     * Build general restaurant context (backward compatibility)
     */
    public String buildRestaurantContext() {
        return buildRestaurantInfoContext() + "\n" +
               buildMenuContext() + "\n" +
               buildTableContext() + "\n" +
               buildPoliciesContext();
    }

    /**
     * Legacy methods for backward compatibility
     */
    private String buildMenuContext() {
        try {
            StringBuilder menuContext = new StringBuilder();
            menuContext.append("MENU INFORMATION:\n");

            List<Items> allItems = itemsService.getAllPublicItems();
            if (allItems != null && !allItems.isEmpty()) {
                menuContext.append(String.format("- Total menu items: %d\n", allItems.size()));
                menuContext.append("- Available dishes:\n");
                for (Items item : allItems.stream().limit(10).collect(Collectors.toList())) {
                    menuContext.append(String.format("  * %s - %s: %s\n",
                            item.getName(),
                            formatPriceVND(item.getPrice()),
                            item.getDescription() != null ? item.getDescription() : "Delicious dish"));
                }
                if (allItems.size() > 10) {
                    menuContext.append(String.format("  ... and %d more items\n", allItems.size() - 10));
                }
            }

            List<Items> bestSellers = itemsService.getAllBestSellerItems();
            if (bestSellers != null && !bestSellers.isEmpty()) {
                menuContext.append("\n- BEST SELLERS:\n");
                for (Items item : bestSellers.stream().limit(5).collect(Collectors.toList())) {
                    menuContext.append(String.format("  * %s - %s (Sold: %d times)\n",
                            item.getName(),
                            formatPriceVND(item.getPrice()),
                            item.getSoldCount()));
                }
            }

            menuContext.append("\n");
            return menuContext.toString();

        } catch (Exception e) {
            logger.error("Error building menu context", e);
            return "MENU INFORMATION: Available upon request\n\n";
        }
    }

    private String buildTableContext() {
        try {
            StringBuilder tableContext = new StringBuilder();
            tableContext.append("TABLE INFORMATION:\n");

            List<TableResponse> allTables = tableService.getAllTables(null);
            if (allTables != null && !allTables.isEmpty()) {
                long availableTables = allTables.stream()
                        .filter(table -> "AVAILABLE".equals(table.getStatus()))
                        .count();

                tableContext.append(String.format("- Total tables: %d\n", allTables.size()));
                tableContext.append(String.format("- Currently available: %d\n", availableTables));

                tableContext.append("- Table capacities available:\n");
                allTables.stream()
                        .map(TableResponse::getCapacity)
                        .filter(capacity -> capacity != null)
                        .distinct()
                        .sorted()
                        .forEach(capacity -> {
                            long count = allTables.stream()
                                    .filter(t -> t.getCapacity() != null && t.getCapacity().equals(capacity))
                                    .count();
                            tableContext.append(String.format("  * %d-person tables: %d available\n",
                                    capacity, count));
                        });
            }

            tableContext.append("\n");
            return tableContext.toString();

        } catch (Exception e) {
            logger.error("Error building table context", e);
            return "TABLE INFORMATION: Please contact staff for availability\n\n";
        }
    }

    private String buildPoliciesContext() {
        return restaurantConfig.getReservationPolicies() + "\n" +
               restaurantConfig.getPaymentMethodsInfo() + "\n" +
               restaurantConfig.getDiningPolicies();
    }

    // ==================== NEW COMPREHENSIVE CONTEXT BUILDERS ====================

    /**
     * Build comprehensive orders context
     */
    private String buildOrdersFullContext(Long userId, DatabaseQueryAnalyzer.QueryIntent intent) {
        StringBuilder context = new StringBuilder();
        context.append("ORDERS INFORMATION:\n\n");

        try {
            if (intent.needsUserData() && userId != null) {
                var userOrders = databaseAccess.getOrdersByUser(userId);
                if (userOrders.isEmpty()) {
                    context.append("Chưa có đơn hàng nào / No orders yet\n");
                } else {
                    context.append(String.format("Your orders (%d total):\n", userOrders.size()));
                    for (var order : userOrders.stream().limit(5).toList()) {
                        context.append(String.format("- Order #%d: Status %s, Total %s, Date %s\n",
                                order.getId(),
                                order.getStatus(),
                                formatPriceVND(order.getSubTotal()),
                                order.getCreatedDate()));
                    }
                }
            } else if (intent.needsStatistics()) {
                var stats = databaseAccess.getOrderStatsByStatus();
                context.append("Order statistics by status:\n");
                stats.forEach((status, count) ->
                        context.append(String.format("- %s: %d orders\n", status, count)));
            } else {
                context.append("Total orders: ").append(databaseAccess.getAllOrders().size()).append("\n");
            }
        } catch (Exception e) {
            logger.error("Error building orders context", e);
            context.append("Unable to retrieve order information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build comprehensive payments context
     */
    private String buildPaymentsFullContext(Long userId) {
        StringBuilder context = new StringBuilder();
        context.append("PAYMENT INFORMATION:\n\n");

        try {
            var allPayments = databaseAccess.getAllPayments();
            if (allPayments.isEmpty()) {
                context.append("Chưa có dữ liệu thanh toán / No payment data available\n");
            } else {
                context.append(String.format("Total payments: %d\n", allPayments.size()));
                context.append("For reservation deposits: QR Code only\n");
                context.append("For other payments: Cash, Card, QR Code, Bank Transfer\n");
            }
        } catch (Exception e) {
            logger.error("Error building payments context", e);
            context.append("Unable to retrieve payment information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build feedback context
     */
    private String buildFeedbackFullContext(Long userId) {
        StringBuilder context = new StringBuilder();
        context.append("FEEDBACK & RATINGS:\n\n");

        try {
            Double avgRating = databaseAccess.getAverageRating();
            if (avgRating != null) {
                context.append(String.format("Average rating: %.1f/5.0 stars\n", avgRating));
            }

            if (userId != null) {
                var userFeedback = databaseAccess.getFeedbackByUser(userId);
                if (!userFeedback.isEmpty()) {
                    context.append(String.format("\nYour feedback (%d total):\n", userFeedback.size()));
                    for (var feedback : userFeedback.stream().limit(3).toList()) {
                        context.append(String.format("- Rating: %.1f, Comment: %s\n",
                                feedback.getRating(),
                                feedback.getComments() != null ? feedback.getComments() : "No comment"));
                    }
                }
            }

            var allFeedback = databaseAccess.getAllFeedback();
            context.append(String.format("\nTotal feedback received: %d\n", allFeedback.size()));
        } catch (Exception e) {
            logger.error("Error building feedback context", e);
            context.append("Chưa có dữ liệu đánh giá / No feedback data available\n");
        }

        return context.toString();
    }

    /**
     * Build bundles context
     */
    private String buildBundlesFullContext() {
        StringBuilder context = new StringBuilder();
        context.append("COMBO & BUNDLE DEALS:\n\n");

        try {
            var activeBundles = databaseAccess.getActiveBundles();
            if (activeBundles.isEmpty()) {
                context.append("Chưa có combo nào / No bundles available at this time\n");
            } else {
                context.append(String.format("Available bundles (%d):\n", activeBundles.size()));
                for (var bundle : activeBundles) {
                    context.append(String.format("- %s: %s\n",
                            bundle.getName() != null ? bundle.getName() : "Bundle",
                            formatPriceVND(bundle.getPrice())));
                }
            }
        } catch (Exception e) {
            logger.error("Error building bundles context", e);
            context.append("Unable to retrieve bundle information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build work schedule context
     */
    private String buildScheduleFullContext(Long userId, DatabaseQueryAnalyzer.QueryIntent intent) {
        StringBuilder context = new StringBuilder();
        context.append("WORK SCHEDULE INFORMATION:\n\n");

        try {
            if (intent.needsUserData() && userId != null) {
                var employeeSchedule = databaseAccess.getWorkScheduleByEmployee(userId);
                if (employeeSchedule.isEmpty()) {
                    context.append("Chưa có lịch làm việc / No work schedule assigned\n");
                } else {
                    context.append(String.format("Your schedule (%d shifts):\n", employeeSchedule.size()));
                    for (var schedule : employeeSchedule.stream().limit(5).toList()) {
                        context.append(String.format("- Date: %s, Shift: %s, Time: %s-%s\n",
                                schedule.getWorkDate(),
                                schedule.getShiftType(),
                                schedule.getStartTime(),
                                schedule.getEndTime()));
                    }
                }
            } else {
                var allSchedules = databaseAccess.getAllWorkSchedules();
                context.append(String.format("Total scheduled shifts: %d\n", allSchedules.size()));
            }
        } catch (Exception e) {
            logger.error("Error building schedule context", e);
            context.append("Unable to retrieve schedule information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build attendance context
     */
    private String buildAttendanceFullContext(Long userId) {
        StringBuilder context = new StringBuilder();
        context.append("ATTENDANCE INFORMATION:\n\n");

        try {
            if (userId != null) {
                var employeeAttendance = databaseAccess.getAttendanceByEmployee(userId);
                if (employeeAttendance.isEmpty()) {
                    context.append("Chưa có dữ liệu chấm công / No attendance records\n");
                } else {
                    context.append(String.format("Your attendance records (%d days):\n", employeeAttendance.size()));
                    for (var attendance : employeeAttendance.stream().limit(5).toList()) {
                        context.append(String.format("- Date: %s, Status: %s\n",
                                attendance.getWorkDate(),
                                attendance.getStatus() != null ? attendance.getStatus().name() : "UNKNOWN"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error building attendance context", e);
            context.append("Unable to retrieve attendance information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build VIP tiers context
     */
    private String buildVipTiersFullContext() {
        StringBuilder context = new StringBuilder();
        context.append("VIP MEMBERSHIP TIERS:\n\n");

        try {
            var vipTiers = databaseAccess.getAllVipTiers();
            if (vipTiers.isEmpty()) {
                context.append("Chưa có chương trình VIP / No VIP program available\n");
            } else {
                for (var tier : vipTiers) {
                    context.append(String.format("- %s: %.0f%% discount, Requires %d points\n",
                            tier.getTierName(),
                            tier.getDiscountRate() * 100,
                            tier.getRequiredPoints()));
                    if (tier.getBenefits() != null) {
                        context.append(String.format("  Benefits: %s\n", tier.getBenefits()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error building VIP tiers context", e);
            context.append("Unable to retrieve VIP tier information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build banners context
     */
    private String buildBannersFullContext() {
        StringBuilder context = new StringBuilder();
        context.append("CURRENT PROMOTIONS & BANNERS:\n\n");

        try {
            var activeBanners = databaseAccess.getActiveBanners();
            if (activeBanners.isEmpty()) {
                context.append("Chưa có khuyến mãi nào / No active promotions\n");
            } else {
                context.append(String.format("Active promotions (%d):\n", activeBanners.size()));
                for (var banner : activeBanners) {
                    context.append(String.format("- %s\n", banner.getTitle() != null ? banner.getTitle() : "Promotion"));
                }
            }
        } catch (Exception e) {
            logger.error("Error building banners context", e);
            context.append("Unable to retrieve promotion information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build chat history context
     */
    private String buildChatHistoryFullContext(Long userId) {
        StringBuilder context = new StringBuilder();
        context.append("CHAT HISTORY:\n\n");

        try {
            if (userId != null) {
                var userSessions = databaseAccess.getChatSessionsByUser(userId);
                if (userSessions.isEmpty()) {
                    context.append("Chưa có lịch sử chat / No chat history\n");
                } else {
                    context.append(String.format("Your chat sessions (%d total)\n", userSessions.size()));
                    context.append("Use this information to reference previous conversations\n");
                }
            }
        } catch (Exception e) {
            logger.error("Error building chat history context", e);
            context.append("Unable to retrieve chat history at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build categories context
     */
    private String buildCategoriesFullContext() {
        StringBuilder context = new StringBuilder();
        context.append("MENU CATEGORIES:\n\n");

        try {
            var categories = databaseAccess.getAllCategories();
            if (categories.isEmpty()) {
                context.append("Chưa có danh mục / No categories available\n");
            } else {
                context.append(String.format("Available categories (%d):\n", categories.size()));
                for (var category : categories) {
                    context.append(String.format("- %s\n", category.getName()));
                }
            }
        } catch (Exception e) {
            logger.error("Error building categories context", e);
            context.append("Unable to retrieve category information at this time.\n");
        }

        return context.toString();
    }

    /**
     * Build dynamic context based on query intent analysis
     */
    private String buildDynamicContext(DatabaseQueryAnalyzer.QueryIntent intent, Long userId, String userQuery) {
        StringBuilder context = new StringBuilder();

        // Build context for all relevant entity types
        for (String entityType : intent.getEntityTypes()) {
            switch (entityType) {
                case "orders", "order_items" -> context.append(buildOrdersFullContext(userId, intent));
                case "payments" -> context.append(buildPaymentsFullContext(userId));
                case "feedback" -> context.append(buildFeedbackFullContext(userId));
                case "bundles" -> context.append(buildBundlesFullContext());
                case "work_schedule" -> context.append(buildScheduleFullContext(userId, intent));
                case "attendance" -> context.append(buildAttendanceFullContext(userId));
                case "vip_tiers" -> context.append(buildVipTiersFullContext());
                case "banners" -> context.append(buildBannersFullContext());
                case "chat" -> context.append(buildChatHistoryFullContext(userId));
                case "categories" -> context.append(buildCategoriesFullContext());
                default -> context.append(buildRestaurantContext());
            }
            context.append("\n");
        }

        if (context.length() == 0) {
            context.append(buildRestaurantContext());
        }

        return context.toString();
    }
}
