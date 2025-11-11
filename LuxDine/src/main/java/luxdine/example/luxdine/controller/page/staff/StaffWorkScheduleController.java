package luxdine.example.luxdine.controller.page.staff;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.work_schedule.WorkScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/staff/work-schedule")
public class StaffWorkScheduleController {

    WorkScheduleService workScheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String staffWorkSchedulePage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            Model model) {
        
        log.debug("Loading Staff Work Schedule page");
        
        LocalDate requestedDate = weekStart != null ? weekStart : LocalDate.now();
        LocalDate mondayOfWeek = requestedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Lấy lịch làm việc của nhân viên hiện tại
        var mySchedule = workScheduleService.getMyWeekSchedule(mondayOfWeek);
        
        log.debug("Week start: {}, Schedule count: {}", mondayOfWeek, mySchedule.size());
        
        model.addAttribute("mySchedule", mySchedule);
        model.addAttribute("weekStart", mondayOfWeek);
        model.addAttribute("currentPage", "work-schedule");
        
        return "staff/work-schedule/staff-work-schedule";
    }
}

