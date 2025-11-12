package luxdine.unittest.service;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.dto.request.OrderCreateRequest;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.mapper.OrderMapper;
import luxdine.example.luxdine.service.order.OrderService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static luxdine.example.luxdine.service.OrderServiceTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests cho OrderService.createOrder(...) */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService.createOrder — Unit tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderServiceCreateOrderTest {

    @Mock OrdersRepository ordersRepository;
    @Mock OrderItemsRepository orderItemsRepository;
    @Mock ItemsRepository itemsRepository;
    @Mock TableRepository tableRepository;
    @Mock ReservationRepository reservationsRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks OrderService orderService;

    private Tables availableTable;
    private Items itemA, itemB;

    @BeforeEach
    void init() {
        availableTable = defaultAvailableTable();
        itemA = defaultItemA();
        itemB = defaultItemB();
    }

    @Test
    @DisplayName("UTCID01 — Happy path: tableId=10; items=(1×2, 2×1) ⇒ subtotal=85,000; amountDue=85,000; bàn → OCCUPIED")
    void success_multipleQuantities() {
        // Arrange
        when(tableRepository.findById(10L)).thenReturn(Optional.of(availableTable));
        when(itemsRepository.findById(1L)).thenReturn(Optional.of(itemA));
        when(itemsRepository.findById(2L)).thenReturn(Optional.of(itemB));
        when(reservationsRepository.save(any(Reservations.class))).thenAnswer(inv -> {
            Reservations r = inv.getArgument(0);
            setField(r, "id", 501L);
            assertAll("Reservation phải được khởi tạo đầy đủ",
                    () -> assertEquals(ReservationStatus.CONFIRMED, r.getStatus()),
                    () -> assertEquals(ReservationOrigin.WALK_IN, r.getOrigin()),
                    () -> assertNotNull(r.getReservationCode()),
                    () -> assertNotNull(r.getReservationDate()),
                    () -> assertNotNull(r.getActualArrivalTime()),
                    () -> assertEquals(availableTable, r.getTable())
            );
            return r;
        });
        ArgumentCaptor<Orders> orderCap = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCap.capture())).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            if (o.getId() == null) setField(o, "id", 999L);
            return o;
        });

        // Act
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(10L)
                .notes("near window")
                .items(List.of(
                        new OrderCreateRequest.OrderCreateItem(1L, 2),
                        new OrderCreateRequest.OrderCreateItem(2L, 1)
                ))
                .build();
        String returnedId = orderService.createOrder(req);

        // Assert
        assertEquals("999", returnedId, "Service phải trả về id order dạng chuỗi");
        Orders built = orderCap.getValue();
        assertAll("Order được build đúng",
                () -> assertEquals(OrderStatus.IN_PROGRESS, built.getStatus()),
                () -> assertEquals("near window", built.getNotes()),
                () -> assertNotNull(built.getReservation()),
                () -> assertEquals(85_000.0, built.getSubTotal(), 1e-6),
                () -> assertEquals(0.0, built.getDiscountTotal(), 1e-6),
                () -> assertEquals(0.0, built.getTax(), 1e-6),
                () -> assertEquals(0.0, built.getServiceCharge(), 1e-6),
                () -> assertEquals(0.0, built.getDepositApplied(), 1e-6),
                () -> assertEquals(85_000.0, built.getAmountDue(), 1e-6)
        );
        List<OrderItems> lines = built.getOrderItems();
        assertAll("OrderItems tạo đúng",
                () -> assertEquals(3, lines.size()),
                () -> assertTrue(lines.stream().allMatch(oi -> oi.getStatus() == OrderItemStatus.QUEUED)),
                () -> assertEquals(2, lines.stream().filter(oi -> "Mì bò".equals(oi.getNameSnapshot())).count()),
                () -> assertEquals(1, lines.stream().filter(oi -> "Trà đá".equals(oi.getNameSnapshot())).count())
        );
        assertAll("Bàn được cập nhật",
                () -> assertEquals(TableStatus.OCCUPIED, availableTable.getStatus()),
                () -> verify(tableRepository).save(availableTable)
        );
    }

    @Test
    @DisplayName("UTCID02 — tableId=404 (không tồn tại) ⇒ ném IllegalArgumentException")
    void tableNotFound_throwsIllegalArgument() {
        // Arrange
        when(tableRepository.findById(404L)).thenReturn(Optional.empty());
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(404L)
                .items(List.of(new OrderCreateRequest.OrderCreateItem(1L, 1)))
                .build();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(req),
                "Khi bàn không tồn tại, service phải throw IllegalArgumentException");
        verifyNoInteractions(reservationsRepository, ordersRepository, itemsRepository);
    }

    @Test
    @DisplayName("UTCID03 — tableId=77 (OUT_OF_SERVICE) ⇒ ném IllegalStateException")
    void tableOutOfService_throwsIllegalState() {
        // Arrange
        Tables broken = table(77L, TableStatus.OUT_OF_SERVICE);
        when(tableRepository.findById(77L)).thenReturn(Optional.of(broken));
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(77L)
                .items(List.of(new OrderCreateRequest.OrderCreateItem(1L, 1)))
                .build();

        // Act + Assert
        assertThrows(IllegalStateException.class, () -> orderService.createOrder(req),
                "Khi bàn OUT_OF_SERVICE, service phải throw IllegalStateException");
        verifyNoInteractions(reservationsRepository, ordersRepository, itemsRepository);
    }

    @Test
    @DisplayName("UTCID04 — items=null ⇒ tạo order rỗng; subtotal=0; amountDue=0; bàn → OCCUPIED")
    void itemsNull_createsEmptyOrderWithZeroAmount() {
        // Arrange
        when(tableRepository.findById(10L)).thenReturn(Optional.of(availableTable));
        when(reservationsRepository.save(any(Reservations.class))).thenAnswer(inv -> {
            Reservations r = inv.getArgument(0);
            setField(r, "id", 601L);
            return r;
        });
        ArgumentCaptor<Orders> orderCap = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCap.capture())).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            if (o.getId() == null) setField(o, "id", 1001L);
            return o;
        });

        // Act
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(10L)
                .notes("no items")
                .items(null)
                .build();
        String id = orderService.createOrder(req);

        // Assert
        assertEquals("1001", id);
        Orders built = orderCap.getValue();
        assertAll("Order rỗng hợp lệ",
                () -> assertNotNull(built.getOrderItems()),
                () -> assertTrue(built.getOrderItems().isEmpty()),
                () -> assertEquals(0.0, built.getSubTotal(), 1e-6),
                () -> assertEquals(0.0, built.getAmountDue(), 1e-6)
        );
        assertAll("Bàn được cập nhật",
                () -> assertEquals(TableStatus.OCCUPIED, availableTable.getStatus()),
                () -> verify(tableRepository).save(availableTable)
        );
        verifyNoInteractions(itemsRepository);
    }

    @Test
    @DisplayName("UTCID05 — danh sách items có phần tử null ⇒ bỏ qua phần tử null, vẫn tạo dòng hợp lệ")
    void containsNullLine_isIgnored() {
        // Arrange
        when(tableRepository.findById(10L)).thenReturn(Optional.of(availableTable));
        when(itemsRepository.findById(2L)).thenReturn(Optional.of(itemB));
        when(reservationsRepository.save(any(Reservations.class))).thenAnswer(inv -> {
            Reservations r = inv.getArgument(0);
            setField(r, "id", 701L);
            return r;
        });
        ArgumentCaptor<Orders> orderCap = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCap.capture())).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            if (o.getId() == null) setField(o, "id", 1002L);
            return o;
        });
        ArrayList<OrderCreateRequest.OrderCreateItem> list = new ArrayList<>(Arrays.asList(
                null,
                new OrderCreateRequest.OrderCreateItem(2L, 2)
        ));
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(10L)
                .items(list)
                .build();

        // Act
        String id = orderService.createOrder(req);

        // Assert
        assertEquals("1002", id);
        Orders built = orderCap.getValue();
        assertAll("Bỏ qua phần tử null nhưng vẫn build dòng hợp lệ",
                () -> assertEquals(2, built.getOrderItems().size()),
                () -> assertEquals(10_000.0, built.getSubTotal(), 1e-6),
                () -> assertTrue(built.getOrderItems().stream().allMatch(oi -> oi.getStatus() == OrderItemStatus.QUEUED))
        );
    }

    @Test
    @DisplayName("UTCID06 — itemId=999 không tồn tại ⇒ bỏ qua; chỉ tạo dòng còn lại hợp lệ")
    void unknownItem_isIgnored() {
        // Arrange
        when(tableRepository.findById(10L)).thenReturn(Optional.of(availableTable));
        when(itemsRepository.findById(999L)).thenReturn(Optional.empty());
        when(itemsRepository.findById(2L)).thenReturn(Optional.of(itemB));
        when(reservationsRepository.save(any(Reservations.class))).thenAnswer(inv -> {
            Reservations r = inv.getArgument(0);
            setField(r, "id", 801L);
            return r;
        });
        ArgumentCaptor<Orders> orderCap = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCap.capture())).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            if (o.getId() == null) setField(o, "id", 1003L);
            return o;
        });
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(10L)
                .items(List.of(
                        new OrderCreateRequest.OrderCreateItem(999L, 3),
                        new OrderCreateRequest.OrderCreateItem(2L, 2)
                ))
                .build();

        // Act
        String id = orderService.createOrder(req);

        // Assert
        assertEquals("1003", id);
        Orders built = orderCap.getValue();
        assertAll("Chỉ tạo từ item hợp lệ",
                () -> assertEquals(2, built.getOrderItems().size()),
                () -> assertEquals(10_000.0, built.getSubTotal(), 1e-6)
        );
    }

    @Test
    @DisplayName("UTCID07 — quantity=0 hoặc âm ⇒ Math.max(0,q) → không thêm dòng; subtotal=0; amountDue=0")
    void quantityZeroOrNegative_ignoredNoLinesAdded() {
        // Arrange
        when(tableRepository.findById(10L)).thenReturn(Optional.of(availableTable));
        when(itemsRepository.findById(1L)).thenReturn(Optional.of(itemA));
        when(reservationsRepository.save(any(Reservations.class))).thenAnswer(inv -> {
            Reservations r = inv.getArgument(0);
            setField(r, "id", 901L);
            return r;
        });
        ArgumentCaptor<Orders> orderCap = ArgumentCaptor.forClass(Orders.class);
        when(ordersRepository.save(orderCap.capture())).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            if (o.getId() == null) setField(o, "id", 1004L);
            return o;
        });
        OrderCreateRequest req = OrderCreateRequest.builder()
                .tableId(10L)
                .items(List.of(
                        new OrderCreateRequest.OrderCreateItem(1L, 0),
                        new OrderCreateRequest.OrderCreateItem(1L, -3)
                ))
                .build();

        // Act
        String id = orderService.createOrder(req);

        // Assert
        assertEquals("1004", id);
        Orders built = orderCap.getValue();
        assertAll("Không thêm dòng nào khi quantity <= 0",
                () -> assertTrue(built.getOrderItems().isEmpty()),
                () -> assertEquals(0.0, built.getSubTotal(), 1e-6),
                () -> assertEquals(0.0, built.getAmountDue(), 1e-6)
        );
    }
}
