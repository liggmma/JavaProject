package luxdine.unittest.service;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Fixtures & helpers dùng chung cho OrderService tests. */
final class OrderServiceTestData {

    private OrderServiceTestData() {}

    /** Reflection helper: set nhanh field private (vd: id). Chỉ dùng trong test. */
    static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ignore) {}
    }

    // ===== Builders/Factories tổng quát =====
    static Tables table(Long id, TableStatus status) {
        Tables t = new Tables();
        if (id != null) setField(t, "id", id);
        if (status != null) t.setStatus(status);
        return t;
    }

    static Items item(Long id, String name, double price) {
        Items it = new Items();
        if (id != null) setField(it, "id", id);
        it.setName(name);
        it.setPrice(price);
        return it;
    }

    static Reservations reservation(Long id, Tables table, String code) {
        Reservations r = new Reservations();
        if (id != null) setField(r, "id", id);
        r.setTable(table);
        r.setReservationCode(code != null ? code : "R-TEST");
        r.setReservationDate(OffsetDateTime.now());
        r.setActualArrivalTime(OffsetDateTime.now());
        return r;
    }

    static Orders order(Long id, Reservations res, OrderStatus status, double subTotal) {
        Orders o = new Orders();
        if (id != null) setField(o, "id", id);
        o.setReservation(res);
        o.setStatus(status != null ? status : OrderStatus.IN_PROGRESS);
        o.setSubTotal(subTotal);
        o.setDiscountTotal(0.0);
        o.setTax(0.0);
        o.setServiceCharge(0.0);
        o.setDepositApplied(0.0);
        o.setAmountDue(subTotal); // createOrder hiện tính amountDue = subTotal (tax/fee=0)
        o.setOrderItems(new ArrayList<>());
        return o;
    }

    static OrderItems orderItem(Long id, Orders order, Items item,
                                String nameSnapshot, double priceSnapshot,
                                OrderItemStatus status) {
        OrderItems oi = new OrderItems();
        if (id != null) setField(oi, "id", id);
        oi.setOrder(order);
        oi.setItem(item);
        oi.setNameSnapshot(nameSnapshot);
        oi.setPriceSnapshot(priceSnapshot);
        oi.setStatus(status != null ? status : OrderItemStatus.QUEUED);
        return oi;
    }

    static void addOrderItems(Orders order, List<OrderItems> items) {
        if (order.getOrderItems() == null) order.setOrderItems(new ArrayList<>());
        order.getOrderItems().addAll(items);
    }

    // ===== Defaults (mỗi lần gọi -> instance mới) =====
    static Tables defaultAvailableTable() {
        return table(10L, TableStatus.AVAILABLE);
    }

    static Items defaultItemA() {
        return item(1L, "Mì bò", 40_000.0);
    }

    static Items defaultItemB() {
        return item(2L, "Trà đá", 5_000.0);
    }

    /** Gói defaults tiện dùng. */
    static Defaults defaults() {
        return new Defaults(defaultAvailableTable(), defaultItemA(), defaultItemB());
    }

    record Defaults(Tables table, Items itemA, Items itemB) {}
}
