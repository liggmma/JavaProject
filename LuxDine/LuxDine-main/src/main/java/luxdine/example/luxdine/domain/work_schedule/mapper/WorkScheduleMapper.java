package luxdine.example.luxdine.domain.work_schedule.mapper;

import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.work_schedule.dto.response.EmployeeSimpleResponse;
import luxdine.example.luxdine.domain.work_schedule.dto.response.WorkScheduleResponse;
import luxdine.example.luxdine.domain.work_schedule.entity.WorkSchedule;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {
    
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.username")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "getFullName")
    @Mapping(target = "shiftDisplayName", source = "shiftType", qualifiedByName = "getShiftDisplayName")
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "username")
    @Mapping(target = "fullName", source = "user", qualifiedByName = "getFullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "isActive", source = "active")
    EmployeeSimpleResponse toEmployeeSimpleResponse(User user);
    
    @Named("getFullName")
    default String getFullName(User user) {
        if (user == null) return "";
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
    
    @Named("getShiftDisplayName")
    default String getShiftDisplayName(ShiftType shiftType) {
        return shiftType != null ? shiftType.getDisplayName() : "";
    }
}

