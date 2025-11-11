package luxdine.example.luxdine.controller.page.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.admin.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String employeesPage(
            @RequestParam(value = "search", required = false) String searchTerm,
            Model model) {
        var staff = (searchTerm != null && !searchTerm.trim().isEmpty())
                ? adminService.searchStaffByName(searchTerm)
                : adminService.getAllStaff();

        model.addAttribute("staffList", staff);
        model.addAttribute("currentPage", "staff");
        model.addAttribute("searchTerm", searchTerm != null ? searchTerm : "");
        return "admin/employees";
    }

    /*
    Thêm Mapping cho admin-menu
    Tác giả: Kiên
     */
     @GetMapping("/menu")
     @PreAuthorize("hasRole('ADMIN')")
    public String showMenuPage(Model model) {
        model.addAttribute("currentPage", "menu"); 
        return "admin/admin-menu";
    }

}

