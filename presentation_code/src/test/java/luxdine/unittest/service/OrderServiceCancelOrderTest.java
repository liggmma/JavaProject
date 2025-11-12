package luxdine.unittest.service;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.mapper.OrderMapper;
import luxdine.example.luxdine.service.order.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static luxdine.example.luxdine.service.OrderServiceTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Unit tests cho OrderService.cancelOrder(...) */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService.cancelOrder — Unit tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderServiceCancelOrderTest {

    @Mock OrdersRepository ordersRepository;
    @Mock OrderItemsRepository orderItemsRepository;
    @Mock TableRepository tableRepository;
    @Mock ReservationRepository reservationsRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("UTCID01 — orderId không tồn tại ⇒ trả 'Order not found' và không thao tác với table")
    void orderNotFound_returnsMessage() {
        // Arrange
        when(ordersRepository.findById(404L)).thenReturn(Optional.empty());

        // Act
        String msg = orderService.cancelOrder(404L);

        // Assert
        assertEquals("Order not found", msg, "Khi order không tồn tại phải trả đúng thông báo");
        verifyNoInteractions(tableRepository);
        verify(ordersRepository, never()).save(any());
    }

    @Test
    @DisplayName("UTCID02 — Happy path: có table ⇒ set AVAILABLE; order & toàn bộ items ⇒ CANCELLED")
    void success_tableExists_setsAvailable_andCancelsItems() {
        // Arrange
        Tables t = table(55L, TableStatus.OCCUPIED);
        Reservations r = reservation(77L, t, "R-1");
        Orders o = order(99L, r, OrderStatus.IN_PROGRESS, 120_000.0);

        Items it = item(1L, "Mì bò", 40_000.0);
        OrderItems i1 = orderItem(1L, o, it, "Mì bò", 40_000.0, OrderItemStatus.QUEUED);
        OrderItems i2 = orderItem(2L, o, it, "Mì bò", 40_000.0, OrderItemStatus.PREPARING);
        OrderItems i3 = orderItem(3L, o, it, "Mì bò", 40_000.0, OrderItemStatus.SERVED);
        addOrderItems(o, List.of(i1, i2, i3));

        when(ordersRepository.findById(99L)).thenReturn(Optional.of(o));
        when(tableRepository.findById(55L)).thenReturn(Optional.of(t));

        // Act
        String result = orderService.cancelOrder(99L);

        // Assert
        assertNull(result, "Happy path phải trả null (success)");
        assertAll("Kiểm tra side-effects khi hủy order có table",
                () -> assertEquals(TableStatus.AVAILABLE, t.getStatus(), "Bàn phải chuyển AVAILABLE"),
                () -> verify(tableRepository).save(t),
                () -> assertEquals(OrderStatus.CANCELLED, o.getStatus(), "Order phải chuyển CANCELLED"),
                () -> assertTrue(o.getOrderItems().stream().allMatch(oi -> oi.getStatus() == OrderItemStatus.CANCELLED),
                        "Mọi OrderItems phải chuyển CANCELLED"),
                () -> verify(ordersRepository).save(o)
        );
    }

    @Test
    @DisplayName("UTCID03 — Không tìm thấy table ⇒ vẫn CANCELLED order/items; bỏ qua save(table)")
    void success_tableMissing_stillCancelsOrderAndItems() {
        // Arrange
        Tables t = table(55L, TableStatus.OCCUPIED);
        Reservations r = reservation(77L, t, "R-2");
        Orders o = order(100L, r, OrderStatus.IN_PROGRESS, 50_000.0);

        Items it = item(2L, "Trà đá", 5_000.0);
        addOrderItems(o, List.of(orderItem(10L, o, it, "Trà đá", 5_000.0, OrderItemStatus.READY)));

        when(ordersRepository.findById(100L)).thenReturn(Optional.of(o));
        when(tableRepository.findById(55L)).thenReturn(Optional.empty()); // table == null branch

        // Act
        String ret = orderService.cancelOrder(100L);

        // Assert
        assertNull(ret, "Happy path (không có table) vẫn trả null");
        assertAll("Kiểm tra side-effects khi table không tồn tại",
                () -> verify(tableRepository, never()).save(any()),
                () -> assertEquals(OrderStatus.CANCELLED, o.getStatus(), "Order phải chuyển CANCELLED"),
                () -> assertTrue(o.getOrderItems().stream().allMatch(oi -> oi.getStatus() == OrderItemStatus.CANCELLED),
                        "Mọi OrderItems phải chuyển CANCELLED"),
                () -> verify(ordersRepository).save(o)
        );
    }
}
