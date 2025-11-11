package luxdine.example.luxdine.service.ai;

import luxdine.example.luxdine.domain.attendance.entity.Attendance;
import luxdine.example.luxdine.domain.attendance.entity.AttendanceHistory;
import luxdine.example.luxdine.domain.attendance.repository.AttendanceHistoryRepository;
import luxdine.example.luxdine.domain.attendance.repository.AttendanceRepository;
import luxdine.example.luxdine.domain.catalog.entity.BundleItems;
import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.BundleItemsRepository;
import luxdine.example.luxdine.domain.catalog.repository.BundlesRepository;
import luxdine.example.luxdine.domain.catalog.repository.CategoriesRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.chat.entity.ChatMessage;
import luxdine.example.luxdine.domain.chat.entity.ChatSession;
import luxdine.example.luxdine.domain.chat.repository.ChatMessageRepository;
import luxdine.example.luxdine.domain.chat.repository.ChatSessionRepository;
import luxdine.example.luxdine.domain.content.entity.Banner;
import luxdine.example.luxdine.domain.content.repository.BannerRepository;
import luxdine.example.luxdine.domain.feedback.entity.FeedBacks;
import luxdine.example.luxdine.domain.feedback.repository.FeedbackRepository;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.repository.PaymentRepository;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.entity.Areas;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.repository.AreasRepository;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.entity.VipTiers;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import luxdine.example.luxdine.domain.user.repository.VipTierRepository;
import luxdine.example.luxdine.domain.work_schedule.entity.WorkSchedule;
import luxdine.example.luxdine.domain.work_schedule.repository.WorkScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comprehensive database access service for AI Chatbot
 * Provides access to all 22+ entities in the database
 */
@Service
public class DatabaseAccessService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAccessService.class);

    // Catalog repositories
    private final ItemsRepository itemsRepository;
    private final BundlesRepository bundlesRepository;
    private final BundleItemsRepository bundleItemsRepository;
    private final CategoriesRepository categoriesRepository;

    // Order repositories
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;

    // Payment repository
    private final PaymentRepository paymentRepository;

    // Feedback repository
    private final FeedbackRepository feedbackRepository;

    // Table repositories
    private final TableRepository tableRepository;
    private final AreasRepository areasRepository;

    // Reservation repository
    private final ReservationRepository reservationRepository;

    // User repositories
    private final UserRepository userRepository;
    private final VipTierRepository vipTierRepository;

    // Work schedule repository
    private final WorkScheduleRepository workScheduleRepository;

    // Attendance repositories
    private final AttendanceRepository attendanceRepository;
    private final AttendanceHistoryRepository attendanceHistoryRepository;

    // Chat repositories
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    // Content repository
    private final BannerRepository bannerRepository;

    @Autowired
    public DatabaseAccessService(
            ItemsRepository itemsRepository,
            BundlesRepository bundlesRepository,
            BundleItemsRepository bundleItemsRepository,
            CategoriesRepository categoriesRepository,
            OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            PaymentRepository paymentRepository,
            FeedbackRepository feedbackRepository,
            TableRepository tableRepository,
            AreasRepository areasRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            VipTierRepository vipTierRepository,
            WorkScheduleRepository workScheduleRepository,
            AttendanceRepository attendanceRepository,
            AttendanceHistoryRepository attendanceHistoryRepository,
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            BannerRepository bannerRepository
    ) {
        this.itemsRepository = itemsRepository;
        this.bundlesRepository = bundlesRepository;
        this.bundleItemsRepository = bundleItemsRepository;
        this.categoriesRepository = categoriesRepository;
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.paymentRepository = paymentRepository;
        this.feedbackRepository = feedbackRepository;
        this.tableRepository = tableRepository;
        this.areasRepository = areasRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.vipTierRepository = vipTierRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceHistoryRepository = attendanceHistoryRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.bannerRepository = bannerRepository;
    }

    // ==================== ORDERS ====================

    public List<Orders> getAllOrders() {
        try {
            return ordersRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all orders", e);
            return new ArrayList<>();
        }
    }

    public List<Orders> getOrdersByUser(Long userId) {
        try {
            return ordersRepository.findAll().stream()
                    .filter(order -> order.getReservation() != null &&
                            order.getReservation().getUser() != null &&
                            order.getReservation().getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public Orders getOrderById(Long orderId) {
        try {
            return ordersRepository.findById(orderId).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching order by id: {}", orderId, e);
            return null;
        }
    }

    public List<OrderItems> getOrderItemsByOrderId(Long orderId) {
        try {
            return orderItemsRepository.findAll().stream()
                    .filter(item -> item.getOrder() != null &&
                            item.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching order items for order: {}", orderId, e);
            return new ArrayList<>();
        }
    }

    public Map<String, Long> getOrderStatsByStatus() {
        try {
            return ordersRepository.findAll().stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                            Collectors.counting()
                    ));
        } catch (Exception e) {
            logger.error("Error fetching order stats", e);
            return Map.of();
        }
    }

    // ==================== PAYMENTS ====================

    public List<Payments> getAllPayments() {
        try {
            return paymentRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all payments", e);
            return new ArrayList<>();
        }
    }

    public List<Payments> getPaymentsByOrder(Long orderId) {
        try {
            return paymentRepository.findAll().stream()
                    .filter(payment -> payment.getOrder() != null &&
                            payment.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching payments for order: {}", orderId, e);
            return new ArrayList<>();
        }
    }

    public Payments getPaymentById(Long paymentId) {
        try {
            return paymentRepository.findById(paymentId).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching payment by id: {}", paymentId, e);
            return null;
        }
    }

    // ==================== FEEDBACK ====================

    public List<FeedBacks> getAllFeedback() {
        try {
            return feedbackRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all feedback", e);
            return new ArrayList<>();
        }
    }

    public List<FeedBacks> getFeedbackByUser(Long userId) {
        try {
            // Note: FeedBacks entity doesn't have a user relationship currently
            // Return all feedback for now - this can be enhanced when user relationship is added
            return feedbackRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching feedback for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public Double getAverageRating() {
        try {
            List<FeedBacks> allFeedback = feedbackRepository.findAll();
            if (allFeedback.isEmpty()) return null;
            return allFeedback.stream()
                    .mapToDouble(FeedBacks::getRating)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            logger.error("Error calculating average rating", e);
            return null;
        }
    }

    // ==================== BUNDLES ====================

    public List<Bundles> getAllBundles() {
        try {
            return bundlesRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all bundles", e);
            return new ArrayList<>();
        }
    }

    public List<Bundles> getActiveBundles() {
        try {
            return bundlesRepository.findAll().stream()
                    .filter(Bundles::isActive)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching active bundles", e);
            return new ArrayList<>();
        }
    }

    public List<BundleItems> getBundleItems(Long bundleId) {
        try {
            return bundleItemsRepository.findAll().stream()
                    .filter(item -> item.getBundle() != null &&
                            item.getBundle().getId().equals(bundleId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching bundle items for bundle: {}", bundleId, e);
            return new ArrayList<>();
        }
    }

    // ==================== CATEGORIES ====================

    public List<Categories> getAllCategories() {
        try {
            return categoriesRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all categories", e);
            return new ArrayList<>();
        }
    }

    public Categories getCategoryById(Long categoryId) {
        try {
            return categoriesRepository.findById(categoryId).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching category by id: {}", categoryId, e);
            return null;
        }
    }

    // ==================== WORK SCHEDULE ====================

    public List<WorkSchedule> getAllWorkSchedules() {
        try {
            return workScheduleRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all work schedules", e);
            return new ArrayList<>();
        }
    }

    public List<WorkSchedule> getWorkScheduleByEmployee(Long employeeId) {
        try {
            return workScheduleRepository.findAll().stream()
                    .filter(schedule -> schedule.getEmployee() != null &&
                            schedule.getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching work schedule for employee: {}", employeeId, e);
            return new ArrayList<>();
        }
    }

    public List<WorkSchedule> getWorkScheduleByDate(LocalDate date) {
        try {
            return workScheduleRepository.findAll().stream()
                    .filter(schedule -> schedule.getWorkDate() != null &&
                            schedule.getWorkDate().equals(date))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching work schedule for date: {}", date, e);
            return new ArrayList<>();
        }
    }

    // ==================== ATTENDANCE ====================

    public List<Attendance> getAllAttendance() {
        try {
            return attendanceRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all attendance", e);
            return new ArrayList<>();
        }
    }

    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        try {
            return attendanceRepository.findAll().stream()
                    .filter(attendance -> attendance.getEmployee() != null &&
                            attendance.getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching attendance for employee: {}", employeeId, e);
            return new ArrayList<>();
        }
    }

    public List<AttendanceHistory> getAttendanceHistory(Long employeeId) {
        try {
            return attendanceHistoryRepository.findAll().stream()
                    .filter(history -> history.getAttendance() != null &&
                            history.getAttendance().getEmployee() != null &&
                            history.getAttendance().getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching attendance history for employee: {}", employeeId, e);
            return new ArrayList<>();
        }
    }

    // ==================== CHAT HISTORY ====================

    public List<ChatSession> getAllChatSessions() {
        try {
            return chatSessionRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all chat sessions", e);
            return new ArrayList<>();
        }
    }

    public List<ChatSession> getChatSessionsByUser(Long userId) {
        try {
            return chatSessionRepository.findAll().stream()
                    .filter(session -> session.getCustomerId() != null &&
                            session.getCustomerId().equals(userId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching chat sessions for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public List<ChatMessage> getChatMessagesBySession(Long sessionId) {
        try {
            return chatMessageRepository.findAll().stream()
                    .filter(message -> message.getSession() != null &&
                            message.getSession().getId().equals(sessionId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching chat messages for session: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    // ==================== VIP TIERS ====================

    public List<VipTiers> getAllVipTiers() {
        try {
            return vipTierRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all VIP tiers", e);
            return new ArrayList<>();
        }
    }

    public VipTiers getVipTierById(Long tierId) {
        try {
            return vipTierRepository.findById(tierId).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching VIP tier by id: {}", tierId, e);
            return null;
        }
    }

    // ==================== BANNERS ====================

    public List<Banner> getAllBanners() {
        try {
            return bannerRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all banners", e);
            return new ArrayList<>();
        }
    }

    public List<Banner> getActiveBanners() {
        try {
            return bannerRepository.findAll().stream()
                    .filter(Banner::isActive)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching active banners", e);
            return new ArrayList<>();
        }
    }

    // ==================== AREAS ====================

    public List<Areas> getAllAreas() {
        try {
            return areasRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all areas", e);
            return new ArrayList<>();
        }
    }

    // ==================== RESERVATIONS ====================

    public List<Reservations> getReservationsByUser(Long userId) {
        try {
            return reservationRepository.findAll().stream()
                    .filter(reservation -> reservation.getUser() != null &&
                            reservation.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching reservations for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public List<Reservations> getReservationsByDateRange(OffsetDateTime start, OffsetDateTime end) {
        try {
            return reservationRepository.findAll().stream()
                    .filter(reservation -> reservation.getReservationDate() != null &&
                            !reservation.getReservationDate().isBefore(start) &&
                            !reservation.getReservationDate().isAfter(end))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching reservations for date range", e);
            return new ArrayList<>();
        }
    }

    // ==================== USERS ====================

    public List<User> getAllCustomers() {
        try {
            return userRepository.findAll().stream()
                    .filter(user -> user.getRole() != null &&
                            user.getRole().name().equals("CUSTOMER"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all customers", e);
            return new ArrayList<>();
        }
    }

    public List<User> getAllStaff() {
        try {
            return userRepository.findAll().stream()
                    .filter(user -> user.getRole() != null &&
                            (user.getRole().name().equals("STAFF") ||
                             user.getRole().name().equals("ADMIN")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all staff", e);
            return new ArrayList<>();
        }
    }

    // ==================== STATISTICS ====================

    public Map<String, Object> getDatabaseStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", ordersRepository.count());
            stats.put("totalPayments", paymentRepository.count());
            stats.put("totalFeedback", feedbackRepository.count());
            stats.put("totalBundles", bundlesRepository.count());
            stats.put("totalCategories", categoriesRepository.count());
            stats.put("totalReservations", reservationRepository.count());
            stats.put("totalUsers", userRepository.count());
            stats.put("totalTables", tableRepository.count());
            stats.put("totalAreas", areasRepository.count());
            stats.put("totalVipTiers", vipTierRepository.count());
            stats.put("totalBanners", bannerRepository.count());
            stats.put("averageRating", getAverageRating() != null ? getAverageRating() : 0.0);
            return stats;
        } catch (Exception e) {
            logger.error("Error fetching database statistics", e);
            return new HashMap<>();
        }
    }
}
