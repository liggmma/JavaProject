package luxdine.example.luxdine.controller.page.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/admin")
public class AdminAreaLayoutPageController {

    /**
     * Display area and table layout management page
     * @return area-layout template
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/area-layout")
    public String areaLayoutPage(Model model) {
        log.info("Displaying area layout management page");
        model.addAttribute("currentPage", "area-layout");
        return "admin/area-layout";
    }

    /**
     * Display area layout editor page
     * @return area-layout-editor template
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/area-layout-editor")
    public String areaLayoutEditorPage(Model model) {
        log.info("Displaying area layout editor page");
        model.addAttribute("currentPage", "area-layout");
        return "admin/area-layout-editor";
    }
}


