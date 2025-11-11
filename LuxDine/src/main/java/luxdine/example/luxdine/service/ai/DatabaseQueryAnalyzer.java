package luxdine.example.luxdine.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes user queries to determine which database entities need to be accessed
 * Supports both Vietnamese and English keywords
 */
@Service
public class DatabaseQueryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryAnalyzer.class);

    /**
     * Query intent result containing entities to query and parameters
     */
    public static class QueryIntent {
        private List<String> entityTypes;
        private boolean needsUserData;
        private boolean needsStatistics;
        private String specificEntityId;

        public QueryIntent() {
            this.entityTypes = new ArrayList<>();
            this.needsUserData = false;
            this.needsStatistics = false;
        }

        public List<String> getEntityTypes() { return entityTypes; }
        public void addEntityType(String type) { this.entityTypes.add(type); }
        public boolean needsUserData() { return needsUserData; }
        public void setNeedsUserData(boolean needsUserData) { this.needsUserData = needsUserData; }
        public boolean needsStatistics() { return needsStatistics; }
        public void setNeedsStatistics(boolean needsStatistics) { this.needsStatistics = needsStatistics; }
        public String getSpecificEntityId() { return specificEntityId; }
        public void setSpecificEntityId(String specificEntityId) { this.specificEntityId = specificEntityId; }
    }

    /**
     * Analyze user query to determine data access requirements
     */
    public QueryIntent analyzeQuery(String userQuery, Long userId) {
        QueryIntent intent = new QueryIntent();
        String lowerQuery = userQuery.toLowerCase();

        // Check if query is about user's own data
        if (lowerQuery.matches(".*(my|của tôi|tôi đã|tôi có|của mình).*")) {
            intent.setNeedsUserData(true);
        }

        // Check for statistics queries
        if (lowerQuery.matches(".*(statistics|stats|total|count|average|tổng|số lượng|trung bình|thống kê).*")) {
            intent.setNeedsStatistics(true);
        }

        // Orders & OrderItems
        if (lowerQuery.matches(".*(order|đơn hàng|đơn|mua|purchase|bought).*")) {
            intent.addEntityType("orders");
            if (lowerQuery.matches(".*(item|món|sản phẩm|product).*")) {
                intent.addEntityType("order_items");
            }
        }

        // Payments
        if (lowerQuery.matches(".*(payment|thanh toán|trả tiền|paid|bill|hóa đơn|receipt|biên lai).*")) {
            intent.addEntityType("payments");
        }

        // Feedback & Ratings
        if (lowerQuery.matches(".*(feedback|đánh giá|review|rating|comment|phản hồi|nhận xét).*")) {
            intent.addEntityType("feedback");
        }

        // Bundles & Combos
        if (lowerQuery.matches(".*(bundle|combo|set|gói|deal|promotion|khuyến mãi).*")) {
            intent.addEntityType("bundles");
        }

        // Categories
        if (lowerQuery.matches(".*(category|danh mục|loại|type|nhóm).*")) {
            intent.addEntityType("categories");
        }

        // Work Schedule
        if (lowerQuery.matches(".*(schedule|lịch|làm việc|shift|ca|work schedule).*")) {
            intent.addEntityType("work_schedule");
        }

        // Attendance
        if (lowerQuery.matches(".*(attendance|điểm danh|chấm công|present|absent).*")) {
            intent.addEntityType("attendance");
        }

        // Chat History (Staff/Customer chat)
        if (lowerQuery.matches(".*(chat|message|tin nhắn|conversation|hội thoại|support chat).*") &&
            !lowerQuery.matches(".*(ai|chatbot|bot).*")) {
            intent.addEntityType("chat");
        }

        // VIP Tiers
        if (lowerQuery.matches(".*(vip|tier|hạng|membership|thành viên|loyalty).*")) {
            intent.addEntityType("vip_tiers");
        }

        // Banners & Promotions
        if (lowerQuery.matches(".*(banner|quảng cáo|advertisement|promotion|khuyến mãi).*")) {
            intent.addEntityType("banners");
        }

        // Areas & Table Locations
        if (lowerQuery.matches(".*(area|khu vực|zone|floor|tầng|location|vị trí).*") &&
            lowerQuery.matches(".*(table|bàn).*")) {
            intent.addEntityType("areas");
        }

        // Reservations (already handled but ensure it's here)
        if (lowerQuery.matches(".*(reservation|đặt bàn|đặt chỗ|booking).*")) {
            intent.addEntityType("reservations");
        }

        // Tables
        if (lowerQuery.matches(".*(table|bàn|seat|chỗ ngồi).*") &&
            !lowerQuery.matches(".*(reservation|book|đặt).*")) {
            intent.addEntityType("tables");
        }

        // Menu Items
        if (lowerQuery.matches(".*(menu|food|dish|món|thực đơn|đồ ăn).*")) {
            intent.addEntityType("items");
        }

        // Users/Customers
        if (lowerQuery.matches(".*(customer|khách hàng|user|người dùng|member).*")) {
            intent.addEntityType("users");
        }

        // Staff/Employees
        if (lowerQuery.matches(".*(staff|nhân viên|employee|worker).*")) {
            intent.addEntityType("staff");
        }

        // If no specific entity detected, include general entities
        if (intent.getEntityTypes().isEmpty()) {
            intent.addEntityType("general");
        }

        logger.debug("Query analysis for '{}': entities={}, needsUserData={}, needsStats={}",
                userQuery, intent.getEntityTypes(), intent.needsUserData(), intent.needsStatistics());

        return intent;
    }

    /**
     * Check if query asks about specific entity by ID
     */
    public Long extractEntityId(String query) {
        // Pattern for extracting numbers that might be IDs
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(id|#|số)\\s*(\\d+)\\b");
        java.util.regex.Matcher matcher = pattern.matcher(query.toLowerCase());

        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(2));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Determine if query is asking about historical data
     */
    public boolean isHistoricalQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.matches(".*(history|lịch sử|past|trước đây|previous|đã|from|since|trong).*");
    }

    /**
     * Check if query requires date filtering
     */
    public boolean requiresDateFilter(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.matches(".*(today|hôm nay|yesterday|hôm qua|this week|tuần này|" +
                "this month|tháng này|last|recent|gần đây).*");
    }

    /**
     * Get priority entity type (the most relevant one)
     */
    public String getPrimaryEntityType(QueryIntent intent) {
        if (intent.getEntityTypes().isEmpty()) {
            return "general";
        }

        // Priority order for entity types
        String[] priorityOrder = {
                "orders", "payments", "reservations", "feedback",
                "bundles", "work_schedule", "attendance", "vip_tiers",
                "items", "categories", "chat", "banners", "areas",
                "tables", "users", "staff", "general"
        };

        for (String priority : priorityOrder) {
            if (intent.getEntityTypes().contains(priority)) {
                return priority;
            }
        }

        return intent.getEntityTypes().get(0);
    }
}
