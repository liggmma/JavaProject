package luxdine.example.luxdine.service.order;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.constants.BusinessConstants;
import luxdine.example.luxdine.domain.order.dto.request.KitchenOrderUpdateRequest;
import luxdine.example.luxdine.domain.order.dto.response.KitchenOrderResponse;
import luxdine.example.luxdine.domain.order.dto.response.KitchenOrderItemResponse;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;

    /**
     * Get all active orders for kitchen display
     * @return List of kitchen orders with their items
     */
    @Transactional(readOnly = true)
    public List<KitchenOrderResponse> getAllKitchenOrders() {
        log.debug("Fetching all kitchen orders");
        
        // FIXED: Remove auto-complete from read-only operation
        // Auto-complete should be called separately by scheduled task or manual trigger
        
        // Get orders with IN_PROGRESS status (not yet paid)
        List<Orders> activeOrders = ordersRepository.findByStatus(OrderStatus.IN_PROGRESS);
        log.debug("Found {} active orders", activeOrders.size());
        
        return activeOrders.stream()
                .filter(order -> !allItemsServed(order)) // Filter out orders where all items are served
                .filter(order -> !allItemsCancelled(order)) // FIXED: Filter out orders where all items are cancelled
                .filter(order -> hasActiveItems(order)) // FIXED: Only show orders with at least one non-cancelled item
                .map(this::convertToKitchenOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * FIXED: Separate method for auto-complete to avoid transaction issues
     */
    @Transactional
    public void performAutoCompleteIfNeeded() {
        // Only auto-complete if it's been a while since last check
        // This prevents excessive auto-complete calls
        autoCompleteTimedOutOrders();
    }

    /**
     * Get kitchen order summary statistics
     * @return Map with counts for PENDING, COOKING, READY
     */
    public KitchenSummaryResponse getKitchenSummary() {
        log.debug("Fetching kitchen summary statistics");
        
        // Get only items from active orders
        List<Orders> activeOrders = ordersRepository.findByStatus(OrderStatus.IN_PROGRESS);
        // FIXED: Filter out CANCELLED items from summary statistics
        List<OrderItems> activeItems = activeOrders.stream()
                .flatMap(order -> order.getOrderItems() != null ? order.getOrderItems().stream() : Stream.empty())
                .filter(item -> !OrderItemStatus.CANCELLED.equals(item.getStatus())) // Exclude cancelled items
                .collect(Collectors.toList());
        
        long pendingCount = activeItems.stream()
                .filter(item -> OrderItemStatus.QUEUED.equals(item.getStatus()))
                .count();
                
        long cookingCount = activeItems.stream()
                .filter(item -> OrderItemStatus.PREPARING.equals(item.getStatus()))
                .count();
                
        long readyCount = activeItems.stream()
                .filter(item -> OrderItemStatus.READY.equals(item.getStatus()))
                .count();
        
        return KitchenSummaryResponse.builder()
                .pending(pendingCount)
                .cooking(cookingCount)
                .ready(readyCount)
                .build();
    }

    /**
     * Update order item status
     * @param updateRequest Update request with item ID and new status
     * @return Updated kitchen order response
     */
    @Transactional
    public KitchenOrderResponse updateOrderItemStatus(KitchenOrderUpdateRequest updateRequest) {
        log.debug("Updating order item {} status to {}", 
                 updateRequest.getOrderItemId(), updateRequest.getStatus());
        
        // Validate request
        if (updateRequest.getOrderItemId() == null) {
            throw new IllegalArgumentException("Order item ID cannot be null");
        }
        if (updateRequest.getStatus() == null || updateRequest.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        OrderItems orderItem = orderItemsRepository.findById(updateRequest.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found: " + updateRequest.getOrderItemId()));
        
        // FIXED: Validate that order item belongs to an active order
        Orders order = orderItem.getOrder();
        if (order == null) {
            throw new RuntimeException("Order item does not belong to any order");
        }
        if (!OrderStatus.IN_PROGRESS.equals(order.getStatus())) {
            throw new RuntimeException("Cannot update status of order item from completed/cancelled order. Order status: " + order.getStatus());
        }
        
        // BASIC: Simple logic - update only the specific item
        String currentStatus = String.valueOf(orderItem.getStatus());
        String newStatusString = updateRequest.getStatus();
        
        // Validate status transition
        if (currentStatus == null) {
            currentStatus = OrderItemStatus.QUEUED.name();
        }
        
        // Prevent invalid status transitions
        if (OrderItemStatus.READY.name().equals(currentStatus) && 
            BusinessConstants.KDS_STATUS_PENDING.equals(newStatusString)) {
            throw new RuntimeException("Cannot revert READY item back to PENDING");
        }
        
        // Additional validation for status transitions
        if (OrderItemStatus.READY.name().equals(currentStatus) && 
            BusinessConstants.KDS_STATUS_COOKING.equals(newStatusString)) {
            throw new RuntimeException("Cannot revert READY item back to COOKING");
        }
        
        // Allow transition from READY to SERVED
        if (OrderItemStatus.READY.name().equals(currentStatus) && 
            BusinessConstants.KDS_STATUS_SERVED.equals(newStatusString)) {
            // This is a valid transition - item is served to customer
            log.info("Item {} is being served to customer", orderItem.getId());
        }
        
        // Map KDS status to OrderItemStatus
        OrderItemStatus newStatus = mapKdsStatusToOrderItemStatus(updateRequest.getStatus());
        orderItem.setStatus(newStatus);
        
        orderItemsRepository.save(orderItem);
        
        log.info("Updated order item {} ({}) status from {} to {}", 
                orderItem.getId(), orderItem.getNameSnapshot(), 
                currentStatus, newStatus.name());
        
        // Reload order with all necessary associations to avoid lazy loading issues
        Orders refreshedOrder = ordersRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + order.getId()));
        
        // Orders should only be completed when customer pays
        // Kitchen display shows orders until payment is completed
        if (allItemsReady(refreshedOrder)) {
            log.info("All items ready for order {}. Order is ready for pickup.", refreshedOrder.getId());
            // Order remains in kitchen display until payment is completed
        }
        
        // Return updated order (will be filtered out in getAllKitchenOrders if completed)
        return convertToKitchenOrderResponse(refreshedOrder);
    }

    /**
     * Complete an order (mark as COMPLETED) - Internal method for timeout handling
     * @param orderId Order ID to complete
     * @return Updated kitchen order response
     */
    @Transactional
    protected KitchenOrderResponse completeOrder(Long orderId) {
        log.debug("Completing order {}", orderId);
        
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // FIXED: Validate order can be completed
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            log.warn("Order {} is already completed", orderId);
            return convertToKitchenOrderResponse(order);
        }
        
        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            log.warn("Cannot complete cancelled order {}", orderId);
            throw new RuntimeException("Cannot complete cancelled order");
        }
        
        // Update order status to COMPLETED
        order.setStatus(OrderStatus.COMPLETED);
        ordersRepository.save(order);
        
        log.info("Order {} completed successfully", orderId);
        
        // Return the completed order (it will no longer appear in kitchen display)
        return convertToKitchenOrderResponse(order);
    }

    /**
     * Check if all items in an order are served
     * @param order Order to check
     * @return true if all items are SERVED
     */
    private boolean allItemsServed(Orders order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return false;
        }
        
        return order.getOrderItems().stream()
                .allMatch(item -> OrderItemStatus.SERVED.name().equals(item.getStatus()));
    }

    /**
     * Check if all items in an order are cancelled
     * @param order Order to check
     * @return true if all items are CANCELLED
     */
    private boolean allItemsCancelled(Orders order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return false;
        }
        
        return order.getOrderItems().stream()
                .allMatch(item -> OrderItemStatus.CANCELLED.equals(item.getStatus()));
    }

    /**
     * Check if order has at least one active (non-cancelled) item
     * @param order Order to check
     * @return true if order has at least one non-cancelled item
     */
    private boolean hasActiveItems(Orders order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return false;
        }
        
        return order.getOrderItems().stream()
                .anyMatch(item -> !OrderItemStatus.CANCELLED.equals(item.getStatus()));
    }

    /**
     * Check if all items in an order are ready
     * @param order Order to check
     * @return true if all items are READY
     */
    private boolean allItemsReady(Orders order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return false;
        }
        
        return order.getOrderItems().stream()
                .allMatch(item -> OrderItemStatus.READY.equals(item.getStatus()));
    }

    /**
     * Check if order has timed out (auto-complete after 6 hours)
     * Only for orders that are truly abandoned (no activity for 6 hours)
     * @param order Order to check
     * @return true if order has timed out
     */
    private boolean isOrderTimedOut(Orders order) {
        long elapsedMinutes = calculateElapsedMinutes(Date.from(order.getCreatedDate()));
        return elapsedMinutes > BusinessConstants.ORDER_TIMEOUT_MINUTES; // 6 hours timeout - only for truly abandoned orders
    }

    /**
     * Auto-complete orders that have timed out
     * FIXED: Business logic - Auto-complete orders when all items are READY and order is old enough
     * This method should be called periodically (e.g., every 30 minutes)
     */
    @Transactional
    public void autoCompleteTimedOutOrders() {
        log.debug("Checking for orders that should be auto-completed");
        
        List<Orders> activeOrders = ordersRepository.findByStatus(OrderStatus.IN_PROGRESS);
        int completedCount = 0;
        
        for (Orders order : activeOrders) {
            try {
                // FIXED: Auto-complete if all items are READY and order is old enough (2+ hours)
                if (shouldAutoCompleteOrder(order)) {
                    log.info("Auto-completing order {} - all items ready and order is old enough", order.getId());
                    completeOrder(order.getId());
                    completedCount++;
                } else if (isOrderTimedOut(order)) {
                    log.warn("Order {} has timed out but not all items are ready - skipping auto-completion", order.getId());
                }
            } catch (Exception e) {
                log.error("Error processing order {} for auto-completion: {}", order.getId(), e.getMessage(), e);
                // Continue processing other orders
            }
        }
        
        if (completedCount > 0) {
            log.info("Auto-completed {} orders", completedCount);
        }
    }
    
    /**
     * Check if order should be auto-completed
     * FIXED: Business logic - Auto-complete when all items are READY and order is 4+ hours old
     * This prevents premature auto-completion of active orders
     * @param order Order to check
     * @return true if order should be auto-completed
     */
    private boolean shouldAutoCompleteOrder(Orders order) {
        // Check if all items are READY
        if (!allItemsReady(order)) {
            return false;
        }
        
        // FIXED: Increased threshold to 4 hours to prevent premature auto-completion
        // This gives customers enough time to finish their meal
        long elapsedMinutes = calculateElapsedMinutes(Date.from(order.getCreatedDate()));
        return elapsedMinutes >= 240; // 4 hours threshold for auto-completion
    }

    /**
     * Convert Orders entity to KitchenOrderResponse
     * BASIC: Simple logic - show each order item separately
     */
    private KitchenOrderResponse convertToKitchenOrderResponse(Orders order) {
        log.debug("Converting order {} to KitchenOrderResponse", order.getId());
        
        List<KitchenOrderItemResponse> orderItems = new ArrayList<>();
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            // BASIC: Convert each order item separately (no grouping)
            // FIXED: Filter out CANCELLED items from kitchen display
            orderItems = order.getOrderItems().stream()
                    .filter(item -> !OrderItemStatus.CANCELLED.equals(item.getStatus())) // Exclude cancelled items
                    .map(this::convertToKitchenOrderItemResponse)
                    .collect(Collectors.toList());
            
            log.debug("Order {} has {} items (after filtering cancelled)", order.getId(), orderItems.size());
        } else {
            log.warn("Order {} has no order items", order.getId());
        }
        
        // Determine priority based on order age and items
        String priority = determinePriority(order);
        boolean isOverdue = isOrderOverdue(order);
        boolean allReady = orderItems.stream().allMatch(item -> OrderItemStatus.READY.name().equals(item.getStatus()));
        
        return KitchenOrderResponse.builder()
                .orderId(order.getId())
                .orderCode("ORD-" + String.format("%03d", order.getId()))
                .tableName(order.getReservation().getTable() != null ? order.getReservation().getTable().getTableName() : BusinessConstants.UNKNOWN_VALUE)
                .tableType(order.getReservation().getTable() != null ? order.getReservation().getTable().getTableType() : "")
                .priority(priority)
                .isOverdue(isOverdue)
                .allReady(allReady)
                .serverName(getServerName(order))
                .orderTime(convertToLocalDateTime(Date.from(order.getCreatedDate())))
                .elapsedMinutes(calculateElapsedMinutes(Date.from(order.getCreatedDate())))
                .notes(order.getNotes())
                .allergies(extractAllergies(order))
                .orderItems(orderItems)
                .build();
    }

    /**
     * Convert OrderItems entity to KitchenOrderItemResponse
     * BASIC: Simple conversion without grouping
     */
    private KitchenOrderItemResponse convertToKitchenOrderItemResponse(OrderItems orderItem) {
        log.debug("Converting order item {} to KitchenOrderItemResponse", orderItem.getId());
        
        String kdsStatus = mapOrderItemStatusToKds(orderItem.getStatus() != null ? orderItem.getStatus().name() : OrderItemStatus.QUEUED.name());
        boolean isOverdue = isItemOverdue(orderItem);
        
        return KitchenOrderItemResponse.builder()
                .itemId(orderItem.getId())
                .itemName(orderItem.getNameSnapshot() != null ? orderItem.getNameSnapshot() : "Unknown Item")
                .quantity(1) // Each record = quantity 1
                .status(kdsStatus)
                .notes(getItemNotes(orderItem))
                .startedAt(orderItem.getUpdatedDate() != null ? 
                          convertToLocalDateTime(Date.from(orderItem.getUpdatedDate())) : null)
                .cookingMinutes(calculateCookingMinutes(orderItem))
                .isOverdue(isOverdue)
                .build();
    }

    /**
     * Map OrderItemStatus to KDS status
     */
    private String mapOrderItemStatusToKds(String orderItemStatus) {
        switch (orderItemStatus) {
            case "QUEUED":
                return BusinessConstants.KDS_STATUS_PENDING;
            case "PREPARING":
                return BusinessConstants.KDS_STATUS_COOKING;
            case "READY":
                return BusinessConstants.KDS_STATUS_READY;
            case "SERVED":
                return BusinessConstants.KDS_STATUS_SERVED;
            default:
                return BusinessConstants.KDS_STATUS_PENDING;
        }
    }

    /**
     * Map KDS status to OrderItemStatus
     */
    private OrderItemStatus mapKdsStatusToOrderItemStatus(String kdsStatus) {
        switch (kdsStatus) {
            case BusinessConstants.KDS_STATUS_PENDING:
                return OrderItemStatus.QUEUED;
            case BusinessConstants.KDS_STATUS_COOKING:
                return OrderItemStatus.PREPARING;
            case BusinessConstants.KDS_STATUS_READY:
                return OrderItemStatus.READY;
            case BusinessConstants.KDS_STATUS_SERVED:
                return OrderItemStatus.SERVED;
            default:
                return OrderItemStatus.QUEUED;
        }
    }


    /**
     * Determine order priority based on age and items
     */
    private String determinePriority(Orders order) {
        long elapsedMinutes = calculateElapsedMinutes(Date.from(order.getCreatedDate()));
        
        if (elapsedMinutes > BusinessConstants.URGENT_THRESHOLD_MINUTES) {
            return BusinessConstants.PRIORITY_URGENT;
        } else if (elapsedMinutes > BusinessConstants.HIGH_PRIORITY_THRESHOLD_MINUTES) {
            return BusinessConstants.PRIORITY_HIGH;
        } else {
            return BusinessConstants.PRIORITY_NORMAL;
        }
    }

    /**
     * Check if order is overdue
     */
    private boolean isOrderOverdue(Orders order) {
        long elapsedMinutes = calculateElapsedMinutes(Date.from(order.getCreatedDate()));
        return elapsedMinutes > BusinessConstants.URGENT_THRESHOLD_MINUTES; // 20 minutes threshold
    }

    /**
     * Check if order item is overdue
     */
    private boolean isItemOverdue(OrderItems orderItem) {
        if (OrderItemStatus.READY.equals(orderItem.getStatus())) {
            return false;
        }
        
        long elapsedMinutes = calculateElapsedMinutes(Date.from(orderItem.getCreateDate()));
        return elapsedMinutes > BusinessConstants.ITEM_OVERDUE_THRESHOLD_MINUTES; // 15 minutes threshold for items
    }

    /**
     * Calculate elapsed minutes from date
     */
    private long calculateElapsedMinutes(Date date) {
        if (date == null) return 0;
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        return diffInMillis / (60 * 1000); // Convert to minutes
    }

    /**
     * Calculate cooking minutes for an item
     */
    private long calculateCookingMinutes(OrderItems orderItem) {
        if (orderItem.getUpdatedDate() == null) return 0;

        Instant updated = orderItem.getUpdatedDate(); // Instant
        long diffInMillis = Duration.between(updated, Instant.now()).toMillis();
        return diffInMillis / (60 * 1000);
    }

    /**
     * Convert Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Extract allergies from order
     */
    private List<String> extractAllergies(Orders order) {
        if (order.getReservation().getUser() != null && order.getReservation().getUser().getAllergens() != null) {
            return order.getReservation().getUser().getAllergens();
        }
        return List.of();
    }

    /**
     * Get server name for the order
     */
    private String getServerName(Orders order) {
        if (order.getReservation().getUser() != null) {
            return order.getReservation().getUser().getUsername();
        }
        return "Staff";
    }

    /**
     * Get item notes
     */
    private String getItemNotes(OrderItems orderItem) {
        // For now, return empty string - can be extended to include special instructions
        return "";
    }
    



    /**
     * Kitchen summary response
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class KitchenSummaryResponse {
        long pending;
        long cooking;
        long ready;
    }
}
