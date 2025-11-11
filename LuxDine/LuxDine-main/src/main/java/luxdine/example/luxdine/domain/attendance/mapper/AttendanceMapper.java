package luxdine.example.luxdine.domain.attendance.mapper;

import luxdine.example.luxdine.domain.attendance.dto.response.AttendanceHistoryResponse;
import luxdine.example.luxdine.domain.attendance.dto.response.AttendanceResponse;
import luxdine.example.luxdine.domain.attendance.entity.Attendance;
import luxdine.example.luxdine.domain.attendance.entity.AttendanceHistory;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {
    
    public AttendanceResponse toResponse(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        
        User employee = attendance.getEmployee();
        ShiftType shiftType = attendance.getShiftType();
        
        // Fix: Validate employee is not null to prevent NPE
        if (employee == null) {
            throw new IllegalStateException(
                String.format("Attendance ID %d has null employee. Data integrity issue.", attendance.getId()));
        }
        
        // Fix: Validate required fields
        if (shiftType == null) {
            throw new IllegalStateException(
                String.format("Attendance ID %d has null shiftType. Data integrity issue.", attendance.getId()));
        }
        
        if (attendance.getStatus() == null) {
            throw new IllegalStateException(
                String.format("Attendance ID %d has null status. Data integrity issue.", attendance.getId()));
        }
        
        if (attendance.getLeaveType() == null) {
            throw new IllegalStateException(
                String.format("Attendance ID %d has null leaveType. Data integrity issue.", attendance.getId()));
        }
        
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getUsername())
                .employeeName(getFullName(employee))
                .workDate(attendance.getWorkDate())
                .shiftType(shiftType.name())
                .shiftDisplayName(shiftType.getDisplayName())
                .scheduledStartTime(attendance.getScheduledStartTime())
                .scheduledEndTime(attendance.getScheduledEndTime())
                .actualCheckInTime(attendance.getActualCheckInTime())
                .actualCheckOutTime(attendance.getActualCheckOutTime())
                .status(attendance.getStatus().name())
                .statusDisplayName(attendance.getStatus().getDisplayName())
                .leaveType(attendance.getLeaveType().name())
                .leaveTypeDisplayName(attendance.getLeaveType().getDisplayName())
                .notes(attendance.getNotes())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
    
    public AttendanceHistoryResponse toHistoryResponse(AttendanceHistory history) {
        if (history == null) {
            return null;
        }
        
        return AttendanceHistoryResponse.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus() != null ? history.getOldStatus().getDisplayName() : null)
                .newStatus(history.getNewStatus().getDisplayName())
                .actionType(history.getActionType())
                .notes(history.getNotes())
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    private String getFullName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        if (user.getFirstName() != null) {
            return user.getFirstName();
        }
        if (user.getLastName() != null) {
            return user.getLastName();
        }
        return user.getUsername();
    }
}

