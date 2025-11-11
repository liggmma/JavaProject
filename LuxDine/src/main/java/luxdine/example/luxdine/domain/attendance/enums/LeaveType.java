package luxdine.example.luxdine.domain.attendance.enums;

public enum LeaveType {
    APPROVED("Nghỉ có phép"),
    UNAPPROVED("Nghỉ không phép"),
    NONE("Không");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

