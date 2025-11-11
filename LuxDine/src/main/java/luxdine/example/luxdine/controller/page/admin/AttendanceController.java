package luxdine.example.luxdine.controller.page.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/admin/attendance")
public class AttendanceController {
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String attendancePage(Model model) {
        model.addAttribute("currentPage", "attendance");
        model.addAttribute("today", LocalDate.now());
        return "admin/attendance";
    }
}

