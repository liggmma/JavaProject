package luxdine.example.luxdine.controller.page.admin;

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
@RequestMapping("/admin/work-schedule")
public class WorkScheduleController {

    WorkScheduleService workScheduleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String workSchedulePage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            Model model) {
        
        LocalDate requestedDate = weekStart != null ? weekStart : LocalDate.now();
        LocalDate mondayOfWeek = requestedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        var weekSchedule = workScheduleService.getWeekSchedule(mondayOfWeek);
        var employees = workScheduleService.getAllEmployees();
        
        model.addAttribute("weekSchedule", weekSchedule);
        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", "work-schedule");
        
        return "admin/work-schedule";
    }
}

