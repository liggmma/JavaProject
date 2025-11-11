package luxdine.example.luxdine.domain.attendance.enums;

public enum AttendanceStatus {
    ON_TIME("Đúng giờ"),
    LATE_OR_EARLY_LEAVE("Đi muộn / Về sớm"),
    INCOMPLETE("Chấm công thiếu"),
    NOT_CHECKED_IN("Chưa chấm công"),
    ON_LEAVE("Nghỉ làm");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

