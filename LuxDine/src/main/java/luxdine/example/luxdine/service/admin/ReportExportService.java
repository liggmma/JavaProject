package luxdine.example.luxdine.service.admin;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.dto.response.ReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportExportService {

    private final ReportService reportService;

    // -------------------- PDF EXPORT ---------------------
    public byte[] exportPdf(String mode, int year, int month, int weekNumber, int quarter) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Font tiếng Việt (with fallback for cross-platform support)
            PdfFont fontNormal = loadFontWithFallback(
                "C:/Windows/Fonts/arial.ttf",                          // Windows
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",     // Linux
                "/System/Library/Fonts/Helvetica.ttc",                 // macOS
                "/Library/Fonts/Arial.ttf"                             // macOS alternative
            );
            
            PdfFont fontBold = loadFontWithFallback(
                "C:/Windows/Fonts/arialbd.ttf",                        // Windows
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",// Linux
                "/System/Library/Fonts/Helvetica.ttc",                 // macOS (same as normal)
                "/Library/Fonts/Arial Bold.ttf"                        // macOS alternative
            );

            // Màu thương hiệu
            Color primaryColor = new DeviceRgb(10, 10, 35);
            Color accentColor = new DeviceRgb(249, 115, 22);
            Color successColor = new DeviceRgb(34, 197, 94);
            Color errorColor = new DeviceRgb(239, 68, 68);

            addHeader(document, fontBold, fontNormal, primaryColor, mode, year, month, weekNumber, quarter);
            ReportResponse report = reportService.getReportByMode(mode, year, month, weekNumber, quarter);

            addStatistics(document, fontBold, fontNormal, report, accentColor, successColor, errorColor);
            addRevenue(document, fontBold, fontNormal, report, accentColor);
            addTopItems(document, fontBold, fontNormal, report, accentColor);
            addFooter(document, fontNormal, primaryColor);

            document.close();
        } catch (Exception e) {
            log.error("❌ Lỗi tạo PDF", e);
            throw new IOException("Không thể tạo PDF", e);
        }
        return baos.toByteArray();
    }

    private void addHeader(Document document, PdfFont bold, PdfFont normal, Color color, String mode, int y, int m, int w, int q) {
        document.add(new Paragraph("BÁO CÁO & THỐNG KÊ LUXDINE")
                .setFont(bold).setFontSize(20).setFontColor(color).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(getPeriodText(mode, y, m, w, q))
                .setFont(normal).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        document.add(new Paragraph("Ngày xuất: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(normal).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new LineSeparator(new SolidLine()).setMarginBottom(20));
    }

    private String getPeriodText(String mode, int y, int m, int w, int q) {
        return switch (mode.toLowerCase()) {
            case "week" -> String.format("Tuần %d - Tháng %d/%d", w, m, y);
            case "month" -> String.format("Tháng %d/%d", m, y);
            case "quarter" -> String.format("Quý %d - Năm %d", q, y);
            case "year" -> String.format("Năm %d", y);
            default -> "Không xác định";
        };
    }

    private void addStatistics(Document doc, PdfFont bold, PdfFont normal, ReportResponse r,
                               Color accent, Color success, Color error) {
        doc.add(new Paragraph("THỐNG KÊ TỔNG QUAN").setFont(bold).setFontSize(14).setFontColor(accent));
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
        // Fix: Use Locale.of() instead of deprecated new Locale()
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
        table.addCell(createPdfCell("Doanh thu", formatCurrency(r.getTotalRevenue()), bold, success));
        table.addCell(createPdfCell("Tổng đơn", nf.format(r.getTotalOrders()), bold, accent));
        table.addCell(createPdfCell("Đơn hủy", nf.format(r.getTotalCancelledOrders()), bold, error));
        table.addCell(createPdfCell("Hoàn tiền", nf.format(r.getTotalRefundedPayments()), bold, error));
        table.addCell(createPdfCell("Bàn đặt", nf.format(r.getTotalReservedTables()), bold, accent));
        table.addCell(createPdfCell("Nhân viên", nf.format(r.getTotalStaff()), bold, accent));
        table.addCell(createPdfCell("Người dùng", nf.format(r.getTotalUsers()), bold, accent));
        doc.add(table.setMarginBottom(20));
    }

    private com.itextpdf.layout.element.Cell createPdfCell(String label, String value, PdfFont bold, Color color) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(label).setFontSize(10))
                .add(new Paragraph(value).setFont(bold).setFontSize(14).setFontColor(color))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER);
    }

    private void addRevenue(Document doc, PdfFont bold, PdfFont normal, ReportResponse r, Color accent) {
        doc.add(new Paragraph("DOANH THU THEO THỜI GIAN").setFont(bold).setFontSize(14).setFontColor(accent));
        if (r.getRevenueByDate() == null || r.getRevenueByDate().isEmpty()) {
            doc.add(new Paragraph("Không có dữ liệu doanh thu").setFont(normal));
            return;
        }
        Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        t.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Thời gian").setFont(bold).setFontColor(new DeviceRgb(255,255,255)))
                .setBackgroundColor(new DeviceRgb(10, 10, 35)));
        t.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Doanh thu").setFont(bold).setFontColor(new DeviceRgb(255,255,255)))
                .setBackgroundColor(new DeviceRgb(10, 10, 35)));

        for (Map.Entry<String, Double> e : r.getRevenueByDate().entrySet()) {
            t.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(e.getKey()).setFont(normal)));
            t.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(e.getValue())).setFont(normal)));
        }
        doc.add(t.setMarginBottom(20));
    }

    private void addTopItems(Document doc, PdfFont bold, PdfFont normal, ReportResponse r, Color accent) {
        doc.add(new Paragraph("TOP 5 MÓN BÁN CHẠY").setFont(bold).setFontSize(14).setFontColor(accent));
        if (r.getTop5Items() == null || r.getTop5Items().isEmpty()) {
            doc.add(new Paragraph("Không có dữ liệu món bán chạy").setFont(normal));
            return;
        }
        Table t = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        t.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Tên món").setFont(bold).setFontColor(new DeviceRgb(255,255,255)))
                .setBackgroundColor(new DeviceRgb(10, 10, 35)));
        t.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Số lượng").setFont(bold).setFontColor(new DeviceRgb(255,255,255)))
                .setBackgroundColor(new DeviceRgb(10, 10, 35)));
        r.getTop5Items().forEach(i -> {
            t.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph((String) i.get("name")).setFont(normal)));
            t.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(i.get("totalSold"))).setFont(normal)));
        });
        doc.add(t);
    }

    private void addFooter(Document doc, PdfFont normal, Color color) {
        doc.add(new Paragraph("© " + LocalDate.now().getYear() + " LuxDine Restaurant. All rights reserved.")
                .setFont(normal).setFontSize(10).setFontColor(color).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
    }

    /**
     * Load font with fallback mechanism for cross-platform support
     */
    private PdfFont loadFontWithFallback(String... fontPaths) throws IOException {
        // Try each font path in order
        for (String path : fontPaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists() && fontFile.canRead()) {
                    log.info("Successfully loaded PDF font from: {}", path);
                    return PdfFontFactory.createFont(path, "Identity-H", 
                        EmbeddingStrategy.PREFER_EMBEDDED);
                }
            } catch (Exception e) {
                log.debug("Failed to load font from {}: {}", path, e.getMessage());
            }
        }
        
        // Fallback to standard PDF font (always available, but no Vietnamese support)
        log.warn("Could not load any custom fonts. Using Helvetica fallback. " +
                "Vietnamese characters may not display correctly.");
        return PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }
    
    private String formatCurrency(double amount) {
        // Fix: Use Locale.of() instead of deprecated new Locale()
        return NumberFormat.getCurrencyInstance(Locale.of("vi", "VN")).format(amount);
    }

    // -------------------- EXCEL EXPORT ---------------------
    public byte[] exportExcel(String mode, int year, int month, int weekNumber, int quarter) throws IOException {
        ReportResponse report = reportService.getReportByMode(mode, year, month, weekNumber, quarter);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Workbook wb = new XSSFWorkbook()) {

            // ---------- STYLE ----------
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // ---------------- SHEET 1: TỔNG QUAN ----------------
            Sheet overview = wb.createSheet("Tổng quan");
            int rowIdx = 0;

            Row title = overview.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = title.createCell(0);
            titleCell.setCellValue("BÁO CÁO & THỐNG KÊ LUXDINE");
            titleCell.setCellStyle(titleStyle);
            overview.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            Row period = overview.createRow(rowIdx++);
            period.createCell(0).setCellValue(getPeriodText(mode, year, month, weekNumber, quarter));
            overview.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 1));

            overview.createRow(rowIdx++).createCell(0).setCellValue("Ngày xuất: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            rowIdx++;

            Row header = overview.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell h1 = header.createCell(0);
            org.apache.poi.ss.usermodel.Cell h2 = header.createCell(1);
            h1.setCellValue("Chỉ số");
            h2.setCellValue("Giá trị");
            h1.setCellStyle(headerStyle);
            h2.setCellStyle(headerStyle);

            Object[][] stats = {
                    {"Doanh thu", formatCurrency(report.getTotalRevenue())},
                    {"Tổng đơn hàng", report.getTotalOrders()},
                    {"Đơn hủy", report.getTotalCancelledOrders()},
                    {"Hoàn tiền", report.getTotalRefundedPayments()},
                    {"Bàn đặt", report.getTotalReservedTables()},
                    {"Nhân viên", report.getTotalStaff()},
                    {"Người dùng", report.getTotalUsers()}
            };

            for (Object[] s : stats) {
                Row r = overview.createRow(rowIdx++);
                r.createCell(0).setCellValue(s[0].toString());
                r.createCell(1).setCellValue(s[1].toString());
            }

            overview.autoSizeColumn(0);
            overview.autoSizeColumn(1);

            // ---------------- SHEET 2: DOANH THU ----------------
            Sheet revenue = wb.createSheet("Doanh thu");
            int revRow = 0;
            Row revTitle = revenue.createRow(revRow++);
            org.apache.poi.ss.usermodel.Cell revCell = revTitle.createCell(0);
            revCell.setCellValue("DOANH THU THEO THỜI GIAN");
            revCell.setCellStyle(titleStyle);
            revenue.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            if (report.getRevenueByDate() != null && !report.getRevenueByDate().isEmpty()) {
                Row revHeader = revenue.createRow(revRow++);
                org.apache.poi.ss.usermodel.Cell c1 = revHeader.createCell(0);
                org.apache.poi.ss.usermodel.Cell c2 = revHeader.createCell(1);
                c1.setCellValue("Thời gian");
                c2.setCellValue("Doanh thu");
                c1.setCellStyle(headerStyle);
                c2.setCellStyle(headerStyle);

                for (Map.Entry<String, Double> e : report.getRevenueByDate().entrySet()) {
                    Row r = revenue.createRow(revRow++);
                    r.createCell(0).setCellValue(e.getKey());
                    r.createCell(1).setCellValue(formatCurrency(e.getValue()));
                }
            } else {
                revenue.createRow(revRow++).createCell(0).setCellValue("Không có dữ liệu doanh thu");
            }

            revenue.autoSizeColumn(0);
            revenue.autoSizeColumn(1);

            // ---------------- SHEET 3: TOP 5 MÓN ----------------
            Sheet topItems = wb.createSheet("Top 5 món");
            int topRow = 0;
            Row topTitle = topItems.createRow(topRow++);
            org.apache.poi.ss.usermodel.Cell topCell = topTitle.createCell(0);
            topCell.setCellValue("TOP 5 MÓN BÁN CHẠY");
            topCell.setCellStyle(titleStyle);
            topItems.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            if (report.getTop5Items() != null && !report.getTop5Items().isEmpty()) {
                Row headerTop = topItems.createRow(topRow++);
                org.apache.poi.ss.usermodel.Cell t1 = headerTop.createCell(0);
                org.apache.poi.ss.usermodel.Cell t2 = headerTop.createCell(1);
                t1.setCellValue("Tên món");
                t2.setCellValue("Số lượng");
                t1.setCellStyle(headerStyle);
                t2.setCellStyle(headerStyle);

                for (var i : report.getTop5Items()) {
                    Row r = topItems.createRow(topRow++);
                    r.createCell(0).setCellValue((String) i.get("name"));
                    // Fix: Cast directly instead of toString() + parseDouble()
                    r.createCell(1).setCellValue(((Number) i.get("totalSold")).doubleValue());
                }
            } else {
                topItems.createRow(topRow++).createCell(0).setCellValue("Không có dữ liệu món bán chạy");
            }

            topItems.autoSizeColumn(0);
            topItems.autoSizeColumn(1);

            wb.write(out);
        }
        return out.toByteArray();
    }
}
