package com.example.demo.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PdfReportRenderer {
    private static final Font TITLE = new Font(Font.HELVETICA, 19, Font.BOLD, new Color(31, 55, 80));
    private static final Font SECTION = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(31, 55, 80));
    private static final Font BODY = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
    private static final Font HEADER = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] render(ReportJob job, ReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 32, 32, 30, 34);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new Footer());
            document.open();

            Paragraph brand = new Paragraph("POULTRY CRM", SECTION);
            brand.setSpacingAfter(5);
            document.add(brand);
            Paragraph title = new Paragraph(data.title(), TITLE);
            title.setSpacingAfter(5);
            document.add(title);

            String period = job.reportType().equals("OUTSTANDING")
                    ? "Outstanding as of " + job.toDate().format(DATE)
                    : job.reportType().equals("TRIP_SUMMARY")
                        ? "Trip ID: " + job.filters().get("tripId") + "     |     Trip date: " + job.fromDate().format(DATE)
                        : job.fromDate().format(DATE) + " - " + job.toDate().format(DATE);
            Paragraph metadata = new Paragraph("Reporting period: " + period
                    + "     |     Generated: " + LocalDate.now().format(DATE), BODY);
            metadata.setSpacingAfter(14);
            document.add(metadata);

            boolean tripSummary = job.reportType().equals("TRIP_SUMMARY");
            PdfPTable summary = new PdfPTable(tripSummary ? 4 : 3);
            summary.setWidthPercentage(100);
            summary.setWidths(tripSummary ? new float[]{1, 1, 1, 1} : new float[]{1, 1, 1});
            if (tripSummary) {
                addMetric(summary, "TOTAL SALES", "INR " + money(data.summaryTotals().get("SALES")));
                addMetric(summary, "TOTAL PURCHASE", "INR " + money(data.summaryTotals().get("PURCHASE")));
                addMetric(summary, "TOTAL EXPENSE", "INR " + money(data.summaryTotals().get("EXPENSE")));
                addMetric(summary, "PROFIT", "INR " + money(data.summaryTotals().get("PROFIT")));
            } else {
                addMetric(summary, "RECORDS", String.valueOf(data.recordCount()));
                addMetric(summary, "TOTAL AMOUNT", "INR " + money(data.totalAmount()));
                addMetric(summary, "TOTAL QUANTITY", money(data.totalQuantity()));
            }
            summary.setSpacingAfter(16);
            document.add(summary);

            if (tripSummary) {
                addTripSections(document, data.rows());
            } else {
                Paragraph detailHeading = new Paragraph("Detailed Records", SECTION);
                detailHeading.setSpacingAfter(7);
                document.add(detailHeading);
                addTable(document, data.columns(), data.rows());
            }
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to render PDF report", ex);
        }
    }

    private void addTripSections(Document document, List<Map<String, Object>> rows) throws DocumentException {
        addTripSection(document, "Trip Details", "TRIP", rows,
                List.of("report_date", "reference_id", "description", "status"));
        addTripSection(document, "Sales", "SALES", rows,
                List.of("report_date", "reference_id", "party_name", "quantity", "weight", "rate", "amount"));
        addTripSection(document, "Purchase", "PURCHASE", rows,
                List.of("report_date", "reference_id", "party_name", "quantity", "weight", "rate", "amount"));
        addTripSection(document, "Payments", "PAYMENT", rows,
                List.of("report_date", "reference_id", "party_name", "description", "amount", "status"));
        addTripSection(document, "Expenses", "EXPENSE", rows,
                List.of("report_date", "reference_id", "party_name", "description", "amount"));
    }

    private void addTripSection(Document document, String title, String type,
                                List<Map<String, Object>> rows, List<String> columns) throws DocumentException {
        List<Map<String, Object>> sectionRows = rows.stream()
                .filter(row -> type.equals(row.get("record_type")))
                .collect(Collectors.toList());
        Paragraph heading = new Paragraph(title, SECTION);
        heading.setSpacingBefore(8);
        heading.setSpacingAfter(5);
        document.add(heading);
        addTable(document, columns, sectionRows);
    }

    private void addTable(Document document, List<String> columns, List<Map<String, Object>> rows)
            throws DocumentException {
        int columnCount = Math.max(1, columns.size());
        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitRows(true);
        for (String column : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(label(column), HEADER));
            cell.setBackgroundColor(new Color(42, 70, 98));
            cell.setPadding(6);
            table.addCell(cell);
        }
        for (Map<String, Object> row : rows) {
            for (String column : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(format(row.get(column)), BODY));
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
        if (rows.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No records found.", BODY));
            empty.setColspan(columnCount);
            empty.setPadding(10);
            table.addCell(empty);
        }
        document.add(table);
    }

    private void addMetric(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(9);
        cell.setBorderColor(new Color(220, 226, 232));
        cell.addElement(new Paragraph(label, BODY));
        cell.addElement(new Paragraph(value, SECTION));
        table.addCell(cell);
    }

    private String label(String value) {
        String[] words = value.replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(' ');
            result.append(word.isEmpty() ? word : word.substring(0, 1).toUpperCase() + word.substring(1));
        }
        return result.toString();
    }

    private String format(Object value) {
        if (value == null) return "-";
        if (value instanceof java.sql.Date date) return date.toLocalDate().format(DATE);
        if (value instanceof java.time.LocalDate date) return date.format(DATE);
        if (value instanceof BigDecimal decimal) return money(decimal);
        return value.toString();
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static class Footer extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            Phrase footer = new Phrase("Poultry CRM | Confidential Business Report     Page " + writer.getPageNumber(),
                    new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY));
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, footer,
                    document.right(), document.bottom() - 18, 0);
        }
    }
}
