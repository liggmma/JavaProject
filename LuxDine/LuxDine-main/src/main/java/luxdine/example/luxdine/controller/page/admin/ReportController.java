package luxdine.example.luxdine.controller.page.admin;

import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.domain.catalog.dto.response.ReportResponse;
import luxdine.example.luxdine.service.admin.ReportExportService;
import luxdine.example.luxdine.service.admin.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    /**
     * Trang xem báo cáo & thống kê
     */
    @GetMapping("/admin/report")
    public String viewReports(
            @RequestParam(defaultValue = "week") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) Integer quarter,
            Model model
    ) {
        LocalDate today = LocalDate.now();
        year = (year == null) ? today.getYear() : year;
        month = (month == null) ? today.getMonthValue() : month;
        weekNumber = (weekNumber == null) ? getWeekOfMonth(today) : weekNumber;
        quarter = (quarter == null) ? getQuarterOfYear(today) : quarter;

        ReportResponse report = reportService.getReportByMode(mode, year, month, weekNumber, quarter);

        model.addAttribute("mode", mode);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("weekNumber", weekNumber);
        model.addAttribute("quarter", quarter);
        model.addAttribute("today", today);
        model.addAttribute("report", report);
        model.addAttribute("currentPage", "reports");

        return "admin/manage/report";
    }

    /**
     * API xuất báo cáo ra PDF hoặc Excel
     */
    @GetMapping("/admin/report/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "week") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(defaultValue = "pdf") String type
    ) throws IOException {

        LocalDate today = LocalDate.now();
        year = (year == null) ? today.getYear() : year;
        month = (month == null) ? today.getMonthValue() : month;
        weekNumber = (weekNumber == null) ? getWeekOfMonth(today) : weekNumber;
        quarter = (quarter == null) ? getQuarterOfYear(today) : quarter;

        byte[] fileBytes;
        String filename;
        String contentType;

        // Xác định loại file
        if ("excel".equalsIgnoreCase(type)) {
            fileBytes = reportExportService.exportExcel(mode, year, month, weekNumber, quarter);
            filename = generateFilename(mode, year, month, weekNumber, quarter, "xlsx");
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            fileBytes = reportExportService.exportPdf(mode, year, month, weekNumber, quarter);
            filename = generateFilename(mode, year, month, weekNumber, quarter, "pdf");
            contentType = MediaType.APPLICATION_PDF_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileBytes);
    }

    /**
     * Lấy số tuần trong tháng hiện tại
     */
    private int getWeekOfMonth(LocalDate date) {
        return (int) Math.ceil(date.getDayOfMonth() / 7.0);
    }

    /**
     * Lấy quý trong năm hiện tại
     */
    private int getQuarterOfYear(LocalDate date) {
        return ((date.getMonthValue() - 1) / 3) + 1;
    }

    /**
     * Sinh tên file xuất ra phù hợp với chế độ và loại file
     */
    private String generateFilename(String mode, int year, int month, int weekNumber, int quarter, String extension) {
        String baseName = "baocao_luxdine";
        return switch (mode.toLowerCase()) {
            case "week" -> String.format("%s_tuan%d_thang%d_%d.%s", baseName, weekNumber, month, year, extension);
            case "month" -> String.format("%s_thang%d_%d.%s", baseName, month, year, extension);
            case "quarter" -> String.format("%s_quy%d_%d.%s", baseName, quarter, year, extension);
            case "year" -> String.format("%s_nam%d.%s", baseName, year, extension);
            default -> baseName + "." + extension;
        };
    }
}
