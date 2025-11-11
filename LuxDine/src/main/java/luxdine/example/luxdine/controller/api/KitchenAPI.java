package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.order.dto.request.KitchenOrderUpdateRequest;
import luxdine.example.luxdine.domain.order.dto.response.KitchenOrderResponse;
import luxdine.example.luxdine.service.order.KitchenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/kitchen")
public class KitchenAPI {

    final KitchenService kitchenService;

    /**
     * API endpoint to get all kitchen orders
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<KitchenOrderResponse>> getKitchenOrders() {
        log.debug("API: Fetching all kitchen orders");
        
        List<KitchenOrderResponse> orders = kitchenService.getAllKitchenOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * API endpoint to get kitchen summary
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<KitchenService.KitchenSummaryResponse> getKitchenSummary() {
        log.debug("API: Fetching kitchen summary");
        
        KitchenService.KitchenSummaryResponse summary = kitchenService.getKitchenSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * API endpoint to update order item status
     */
    @PutMapping("/orders/{orderId}/items/{itemId}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> updateOrderItemStatus(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody KitchenOrderUpdateRequest updateRequest) {
        
        log.debug("API: Updating order item {} status to {}", itemId, updateRequest.getStatus());
        
        try {
            // Ensure the item ID matches the path variable
            updateRequest.setOrderItemId(itemId);
            
            KitchenOrderResponse updatedOrder = kitchenService.updateOrderItemStatus(updateRequest);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for order item {}: {}", itemId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error updating order item {}: {}", itemId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating order item {}: {}", itemId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}

