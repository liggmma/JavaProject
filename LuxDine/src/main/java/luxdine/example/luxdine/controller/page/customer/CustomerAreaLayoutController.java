package luxdine.example.luxdine.controller.page.customer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for customer area layout viewing
 * Allows customers to view restaurant floor plans and table layouts
 */
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping
public class CustomerAreaLayoutController {

    final TableService tableService;
    final AreasService areasService;

    /**
     * Display floor plan page for customers
     * @param model Model to add attributes
     * @param area Optional area filter
     * @return customer-layout template
     */
    @GetMapping("/layout")
    public String customerLayout(Model model, @RequestParam(required = false) String area) {
        log.info("Displaying customer layout page");
        List<TableResponse> tables = tableService.getAllTables(area);
        List<AreaResponse> areas = areasService.getAllAreas();
        var summary = tableService.getTableStatusSummary(); // Map<TableStatus, Long>
        var summaryStr = summary.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        
        model.addAttribute("tables", tables);
        model.addAttribute("areas", areas);
        model.addAttribute("statusSummary", summaryStr);
        model.addAttribute("selectedArea", area != null ? area : "All Areas");
        return "customer/area/customer-layout";
    }
}

