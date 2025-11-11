package luxdine.example.luxdine.controller.page.staff;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/staff")
public class FloorPlanController {

    final TableService tableService;
    final AreasService areasService;

    /**
     * Display floor plan page
     * @param model Model to add attributes
     * @param area Optional area filter
     * @return floor-plan template
     */
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @GetMapping("/floor-plan")
    public String floorPlan(Model model, @RequestParam(required = false) String area) {
        List<TableResponse> tables = tableService.getAllTables(area);
        List<AreaResponse> areas = areasService.getAllAreas();
        var summary = tableService.getTableStatusSummary(); // Map<TableStatus, Long>
        var summaryStr = summary.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        model.addAttribute("statusSummary", summaryStr);
        
        model.addAttribute("tables", tables);
        model.addAttribute("areas", areas);
        model.addAttribute("statusSummary", summaryStr);
        model.addAttribute("selectedArea", area != null ? area : "All Areas");
        model.addAttribute("currentPage", "floor-plan");
        return "staff/table/floor-plan";
    }
}
