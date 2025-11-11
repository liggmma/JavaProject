package luxdine.example.luxdine.domain.work_schedule.enums;

public enum ShiftType {
    MORNING("Ca sáng", "08:00", "12:00"),
    AFTERNOON("Ca chiều", "13:00", "17:00"),
    EVENING("Ca tối", "18:00", "22:00"),
    CUSTOM("Ca tùy chỉnh", null, null);

    private final String displayName;
    private final String startTime;
    private final String endTime;

    ShiftType(String displayName, String startTime, String endTime) {
        this.displayName = displayName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
    
    public boolean isCustom() {
        return this == CUSTOM;
    }
}

