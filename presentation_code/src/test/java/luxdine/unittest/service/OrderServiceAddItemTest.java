package luxdine.unittest.service;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static luxdine.example.luxdine.service.OrderServiceTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService.addItem — Unit tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderServiceAddItemTest {

    @Mock OrdersRepository ordersRepository;
    @Mock OrderItemsRepository orderItemsRepository;
    @Mock ItemsRepository itemsRepository;
    @Mock TableRepository tableRepository;
    @Mock ReservationRepository reservationsRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    // ===========================================================
    // UTCID01
    // ===========================================================
    @Test
    @DisplayName("UTCID01 — orderId không tồn tại ⇒ trả 'Order not found'")
    void orderNotFound_returnsMessage() {
        // Arrange
        when(ordersRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        String msg = orderService.addItem(999L, 1L, 1);

        // Assert
        assertEquals("Order not found", msg, "Phải trả đúng thông báo khi order không tồn tại");
        verifyNoInteractions(itemsRepository, orderItemsRepository);
    }

    // ===========================================================
    // UTCID02
    // ===========================================================
    @Test
    @DisplayName("UTCID02 — order tồn tại nhưng status ≠ IN_PROGRESS ⇒ trả 'Only IN_PROGRESS orders can be modified'")
    void orderNotInProgress_returnsMessage() {
        // Arrange
        Tables t = table(10L, TableStatus.OCCUPIED);
        Orders o = order(1L, reservation(5L, t, "R-3"), OrderStatus.CANCELLED, 0.0);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(o));

        // Act
        String msg = orderService.addItem(1L, 1L, 1);

        // Assert
        assertEquals("Only IN_PROGRESS orders can be modified", msg,
                "Khi order không phải IN_PROGRESS thì không được phép thêm món");
        verifyNoInteractions(itemsRepository, orderItemsRepository);
    }

    // ===========================================================
    // UTCID03
    // ===========================================================
    @Test
    @DisplayName("UTCID03 — itemId không tồn tại ⇒ trả 'Item not found'")
    void itemNotFound_returnsMessage() {
        // Arrange
        Tables t = table(10L, TableStatus.OCCUPIED);
        Orders o = order(2L, reservation(6L, t, "R-4"), OrderStatus.IN_PROGRESS, 0.0);
        when(ordersRepository.findById(2L)).thenReturn(Optional.of(o));
        when(itemsRepository.findById(111L)).thenReturn(Optional.empty());

        // Act
        String msg = orderService.addItem(2L, 111L, 2);

        // Assert
        assertEquals("Item not found", msg, "Phải trả đúng thông báo khi item không tồn tại");
        verifyNoInteractions(orderItemsRepository);
    }

    // ===========================================================
    // UTCID04
    // ===========================================================
    @Test
    @DisplayName("UTCID04 — quantity ≤ 0 ⇒ trả 'Quantity must be positive'")
    void quantityNonPositive_returnsMessage() {
        // Arrange
        Tables t = table(10L, TableStatus.OCCUPIED);
        Orders o = order(3L, reservation(7L, t, "R-5"), OrderStatus.IN_PROGRESS, 0.0);
        when(ordersRepository.findById(3L)).thenReturn(Optional.of(o));

        Items it = item(1L, "Mì bò", 40_000.0);
        when(itemsRepository.findById(1L)).thenReturn(Optional.of(it));

        // Act + Assert
        String msg0 = orderService.addItem(3L, 1L, 0);
        String msgNeg = orderService.addItem(3L, 1L, -5);

        assertAll("Phải chặn các giá trị quantity không dương",
                () -> assertEquals("Quantity must be positive", msg0),
                () -> assertEquals("Quantity must be positive", msgNeg)
        );
        verify(orderItemsRepository, never()).save(any());
    }

    // ===========================================================
    // UTCID05 (Happy path)
    // ===========================================================
    @Test
    @DisplayName("UTCID05 — Happy path: thêm 3 món (Trà đá) ⇒ cập nhật subtotal & tạo 3 OrderItems trạng thái QUEUED")
    void success_updatesSubtotal_andCreatesOrderItems() {
        // Arrange
        Tables t = table(10L, TableStatus.OCCUPIED);
        Orders o = order(4L, reservation(8L, t, "R-6"), OrderStatus.IN_PROGRESS, 15_000.0);
        when(ordersRepository.findById(4L)).thenReturn(Optional.of(o));

        Items it = item(2L, "Trà đá", 5_000.0);
        when(itemsRepository.findById(2L)).thenReturn(Optional.of(it));

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<OrderItems> itemCaptor = ArgumentCaptor.forClass(OrderItems.class);

        // Act
        String result = orderService.addItem(4L, 2L, 3); // +3 * 5k

        // Assert
        assertNull(result, "Happy path phải trả null (success)");

        Orders savedOrder = orderCaptor.getValue();
        assertEquals(30_000.0, savedOrder.getSubTotal(), 1e-6,
                "Subtotal phải tăng từ 15,000 lên 30,000 khi thêm 3 món * 5,000");

        verify(orderItemsRepository, times(3)).save(itemCaptor.capture());
        List<OrderItems> savedItems = itemCaptor.getAllValues();

        assertAll("Kiểm tra snapshot và trạng thái của các OrderItems vừa tạo",
                () -> assertEquals(List.of("Trà đá", "Trà đá", "Trà đá"),
                        savedItems.stream().map(OrderItems::getNameSnapshot).collect(Collectors.toList()),
                        "Tên snapshot phải là 'Trà đá' cho cả 3 dòng"),
                () -> assertTrue(savedItems.stream().allMatch(oi -> oi.getPriceSnapshot() == 5_000.0),
                        "Giá snapshot phải là 5,000 cho cả 3 dòng"),
                () -> assertTrue(savedItems.stream().allMatch(oi -> oi.getStatus() == OrderItemStatus.QUEUED),
                        "Trạng thái mặc định phải là QUEUED"),
                () -> assertTrue(savedItems.stream().allMatch(oi -> oi.getOrder() == o),
                        "Mỗi OrderItems phải tham chiếu về Order hiện tại"),
                () -> assertTrue(savedItems.stream().allMatch(oi -> oi.getItem() == it),
                        "Mỗi OrderItems phải tham chiếu đúng Items vừa thêm")
        );
    }
}
