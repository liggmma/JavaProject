package luxdine.example.luxdine.controller.page.staff;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationSearchRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationUpdateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.service.reservation.ReservationService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/staff")
public class StaffReservationController {

    final ReservationService reservationService;
    final TableService tableService;

    /**
     * Display staff homepage with today's reservations
     * @param model Model to add attributes
     * @return redirect to reservations
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/home")
    public String staffHome(Model model) {
        // Redirect staff home to reservations page
        return "redirect:/staff/reservations";
    }

    /**
     * View all reservations with search and filter capabilities
     * @param model Model to add attributes
     * @param searchRequest Search and filter parameters
     * @return reservations template
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/reservations")
    public String viewReservations(Model model, ReservationSearchRequest searchRequest) {
        List<ReservationResponse> reservations = reservationService.getAllReservations(searchRequest);
        
        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationCount", reservations.size());
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("currentPage", "reservations");
        return "staff/reservation/reservations";
    }

    /**
     * Search reservations by term
     * @param model Model to add attributes
     * @param searchTerm Search term
     * @return reservations template with search results
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/reservations/search")
    public String searchReservations(Model model, @RequestParam String searchTerm) {
        try {
            // Validate search term
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Empty search term provided");
                model.addAttribute("reservations", List.of());
                model.addAttribute("reservationCount", 0);
                model.addAttribute("searchTerm", "");
                model.addAttribute("searchError", "Search term cannot be empty");
                return "staff/reservation/reservations";
            }
            
            // Sanitize search term
            String sanitizedSearchTerm = searchTerm.trim();
            if (sanitizedSearchTerm.length() > 100) {
                log.warn("Search term too long: {}", sanitizedSearchTerm.length());
                model.addAttribute("reservations", List.of());
                model.addAttribute("reservationCount", 0);
                model.addAttribute("searchTerm", sanitizedSearchTerm);
                model.addAttribute("searchError", "Search term cannot exceed 100 characters");
                return "staff/reservation/reservations";
            }
            
            // Check for potentially harmful characters
            if (sanitizedSearchTerm.matches(".*[<>\"'&].*")) {
                log.warn("Potentially harmful characters in search term: {}", sanitizedSearchTerm);
                model.addAttribute("reservations", List.of());
                model.addAttribute("reservationCount", 0);
                model.addAttribute("searchTerm", sanitizedSearchTerm);
                model.addAttribute("searchError", "Search term contains invalid characters");
                return "staff/reservation/reservations";
            }
            
            List<ReservationResponse> reservations = reservationService.searchReservations(sanitizedSearchTerm);
            
            model.addAttribute("reservations", reservations);
            model.addAttribute("reservationCount", reservations.size());
            model.addAttribute("searchTerm", sanitizedSearchTerm);
            return "staff/reservation/reservations";
        } catch (Exception e) {
            log.error("Error searching reservations: {}", e.getMessage());
            model.addAttribute("reservations", List.of());
            model.addAttribute("reservationCount", 0);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("searchError", "An error occurred while searching");
            return "staff/reservation/reservations";
        }
    }

    /**
     * Combined search and filter reservations
     * @param model Model to add attributes
     * @param searchRequest Combined search and filter parameters
     * @return reservations template with filtered results
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/reservations/search-filter")
    public String searchAndFilterReservations(Model model, ReservationSearchRequest searchRequest) {
        try {
            log.info("Combined search and filter request: {}", searchRequest);
            
            // Validate and sanitize inputs
            if (searchRequest.getSearchTerm() != null && searchRequest.getSearchTerm().trim().isEmpty()) {
                searchRequest.setSearchTerm(null);
            }
            
            if (searchRequest.getStatus() != null && searchRequest.getStatus().trim().isEmpty()) {
                searchRequest.setStatus(null);
            }
            
            if (searchRequest.getDateRange() != null && searchRequest.getDateRange().trim().isEmpty()) {
                searchRequest.setDateRange(null);
            }
            
            // Get filtered results using combined search and filter
            List<ReservationResponse> reservations = reservationService.getAllReservations(searchRequest);
            
            model.addAttribute("reservations", reservations);
            model.addAttribute("reservationCount", reservations.size());
            model.addAttribute("searchRequest", searchRequest);
            model.addAttribute("currentPage", "reservations");
            
            return "staff/reservation/reservations";
        } catch (Exception e) {
            log.error("Error in combined search and filter: {}", e.getMessage());
            model.addAttribute("reservations", List.of());
            model.addAttribute("reservationCount", 0);
            model.addAttribute("searchRequest", searchRequest);
            model.addAttribute("searchError", "An error occurred while searching and filtering");
            return "staff/reservation/reservations";
        }
    }

    /**
     * Filter reservations by status and date range
     * @param model Model to add attributes
     * @param status Reservation status
     * @param dateRange Date range
     * @return reservations template with filtered results
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/reservations/filter")
    public String filterReservations(Model model, 
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String dateRange) {
        try {
            // Validate status parameter
            if (status != null && !status.trim().isEmpty()) {
                String validStatuses = "PENDING,CONFIRMED,ARRIVED,CANCELLED,COMPLETED";
                String upperStatus = status.trim().toUpperCase();
                if (!validStatuses.contains(upperStatus)) {
                    log.warn("Invalid status filter: {}", status);
                    model.addAttribute("reservations", List.of());
                    model.addAttribute("reservationCount", 0);
                    model.addAttribute("filterStatus", status);
                    model.addAttribute("filterDateRange", dateRange);
                    model.addAttribute("filterError", "Invalid status filter");
                    return "staff/reservation/reservations";
                }
                status = upperStatus;
            }
            
            // Validate date range parameter
            if (dateRange != null && !dateRange.trim().isEmpty()) {
                String validDateRanges = "today,tomorrow,this_week,this_month";
                if (!validDateRanges.contains(dateRange.trim().toLowerCase())) {
                    log.warn("Invalid date range filter: {}", dateRange);
                    model.addAttribute("reservations", List.of());
                    model.addAttribute("reservationCount", 0);
                    model.addAttribute("filterStatus", status);
                    model.addAttribute("filterDateRange", dateRange);
                    model.addAttribute("filterError", "Invalid date range filter");
                    return "staff/reservation/reservations";
                }
            }
            
            List<ReservationResponse> reservations = reservationService.filterReservations(ReservationStatus.valueOf(status), dateRange);
            
            model.addAttribute("reservations", reservations);
            model.addAttribute("reservationCount", reservations.size());
            model.addAttribute("filterStatus", status);
            model.addAttribute("filterDateRange", dateRange);
            return "staff/reservation/reservations";
        } catch (Exception e) {
            log.error("Error filtering reservations: {}", e.getMessage());
            model.addAttribute("reservations", List.of());
            model.addAttribute("reservationCount", 0);
            model.addAttribute("filterStatus", status);
            model.addAttribute("filterDateRange", dateRange);
            model.addAttribute("filterError", "An error occurred while filtering");
            return "staff/reservation/reservations";
        }
    }

    /**
     * Update reservation status (quick action)
     * @param id Reservation ID
     * @param status New status
     * @return redirect to reservations list
     */
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/reservations/{id}/status")
    public String updateReservationStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            log.info("Attempting to update reservation status - ID: {}, Status: {}", id, status);
            
            if (id == null || id <= 0) {
                log.warn("Invalid reservation ID: {}", id);
                return "redirect:/staff/reservations?error=Invalid reservation ID";
            }
            
            if (status == null || status.trim().isEmpty()) {
                log.warn("Empty status provided for reservation ID: {}", id);
                return "redirect:/staff/reservations?error=Status is required";
            }
            
            String trimmedStatus = status.trim();
            log.info("Creating update request - ID: {}, Status: {}", id, trimmedStatus);
            
            ReservationUpdateRequest updateRequest = ReservationUpdateRequest.builder()
                    .id(id)
                    .build();
            
            log.info("Calling reservation service to update reservation: {}", updateRequest);
            reservationService.updateReservation(updateRequest);
            
            log.info("Reservation status updated successfully - ID: {}, Status: {}", id, trimmedStatus);
            return "redirect:/staff/reservations?success=true";
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating reservation status - ID: {}, Status: {}, Error: {}", id, status, e.getMessage());
            return "redirect:/staff/reservations?error=" + e.getMessage();
        } catch (RuntimeException e) {
            log.error("Runtime error updating reservation status - ID: {}, Status: {}, Error: {}", id, status, e.getMessage());
            return "redirect:/staff/reservations?error=Failed to update reservation status";
        } catch (Exception e) {
            log.error("Unexpected error updating reservation status - ID: {}, Status: {}, Error: {}", id, status, e.getMessage());
            return "redirect:/staff/reservations?error=An unexpected error occurred";
        }
    }

    /**
     * Force activate table for confirmed reservation
     * @param id Reservation ID
     * @return ResponseEntity with result
     */
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/reservations/{id}/activate-table")
    @ResponseBody
    public ResponseEntity<?> activateTableForReservation(@PathVariable Long id) {
        try {
            log.info("Attempting to force activate table for reservation - ID: {}", id);
            
            if (id == null || id <= 0) {
                log.warn("Invalid reservation ID: {}", id);
                return ResponseEntity.badRequest().body("Invalid reservation ID");
            }
            
            boolean success = reservationService.forceActivateTableForReservation(id);
            
            if (success) {
                log.info("Table activated successfully for reservation {}", id);
                return ResponseEntity.ok().body("Table activated successfully");
            } else {
                log.warn("Failed to activate table for reservation {}", id);
                return ResponseEntity.badRequest().body("Failed to activate table - check reservation status and table assignment");
            }
        } catch (Exception e) {
            log.error("Error activating table for reservation {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("An error occurred while activating table");
        }
    }

    /**
     * Accept reservation (change status from PENDING to CONFIRMED)
     * @param id Reservation ID
     * @return redirect to reservations list
     */
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/reservations/{id}/accept")
    public String acceptReservation(@PathVariable Long id) {
        try {
            log.info("Attempting to accept reservation - ID: {}", id);
            
            if (id == null || id <= 0) {
                log.warn("Invalid reservation ID: {}", id);
                return "redirect:/staff/reservations?error=Invalid reservation ID";
            }
            
            reservationService.acceptReservation(id);
            
            log.info("Reservation accepted successfully - ID: {}", id);
            return "redirect:/staff/reservations?success=Reservation accepted successfully";
        } catch (RuntimeException e) {
            log.error("Error accepting reservation - ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=" + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error accepting reservation - ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=An unexpected error occurred";
        }
    }



    /**
     * Mark reservation as arrived
     * @param id Reservation ID
     * @return redirect to reservations list
     */
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/reservations/{id}/mark-arrived")
    public String markReservationAsArrived(@PathVariable Long id) {
        try {
            log.info("Attempting to mark reservation as arrived - ID: {}", id);
            
            if (id == null || id <= 0) {
                log.warn("Invalid reservation ID: {}", id);
                return "redirect:/staff/reservations?error=Invalid reservation ID";
            }
            
            reservationService.markReservationAsArrived(id);
            
            log.info("Reservation marked as arrived successfully - ID: {}", id);
            return "redirect:/staff/reservations?success=Reservation marked as arrived";
        } catch (RuntimeException e) {
            log.error("Error marking reservation as arrived - ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=" + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error marking reservation as arrived - ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=An unexpected error occurred";
        }
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/reservations/{id}/order")
    public String createOrFindReservationOrder(@PathVariable Long id) {
        try {
            log.info("Create/Find order for reservation - ID: {}", id);

            if (id == null || id <= 0) {
                log.warn("Invalid reservation ID: {}", id);
                return "redirect:/staff/reservations?error=Invalid reservation ID";
            }

            // Service nên trả về orderId (Long)
            Long orderId = reservationService.createOrFindReservationOrder(id);

            if (orderId == null || orderId <= 0) {
                log.warn("Service returned empty orderId for reservation {}", id);
                return "redirect:/staff/reservations?error=Cannot create or find order for this reservation";
            }

            log.info("Order {} ready for reservation {}", orderId, id);
            // Điều hướng tới trang quản lý/chi tiết đơn của nhân viên
            return "redirect:/staff/orders/" + orderId;
        } catch (IllegalArgumentException e) {
            log.error("Validation error create/find order - Reservation ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=" + e.getMessage();
        } catch (RuntimeException e) {
            log.error("Runtime error create/find order - Reservation ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=Failed to create or find order";
        } catch (Exception e) {
            log.error("Unexpected error create/find order - Reservation ID: {}, Error: {}", id, e.getMessage());
            return "redirect:/staff/reservations?error=An unexpected error occurred";
        }
    }

}
