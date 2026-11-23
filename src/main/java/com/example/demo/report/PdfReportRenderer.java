package com.example.demo.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

            PdfPTable summary = new PdfPTable(3);
            summary.setWidthPercentage(100);
            summary.setWidths(new float[]{1, 1, 1});
            addMetric(summary, "RECORDS", String.valueOf(data.recordCount()));
            addMetric(summary, job.reportType().equals("TRIP_SUMMARY") ? "ORDER VALUE" : "TOTAL AMOUNT",
                    "INR " + money(data.totalAmount()));
            addMetric(summary, job.reportType().equals("TRIP_SUMMARY") ? "ORDER QUANTITY" : "TOTAL QUANTITY",
                    money(data.totalQuantity()));
            summary.setSpacingAfter(16);
            document.add(summary);

            Paragraph detailHeading = new Paragraph("Trip Summary Details".equals(data.title())
                    ? "Trip Activity Details" : "Detailed Records", SECTION);
            detailHeading.setSpacingAfter(7);
            document.add(detailHeading);

            int columnCount = Math.max(1, data.columns().size());
            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            table.setSplitRows(true);
            for (String column : data.columns()) {
                PdfPCell cell = new PdfPCell(new Phrase(label(column), HEADER));
                cell.setBackgroundColor(new Color(42, 70, 98));
                cell.setPadding(6);
                table.addCell(cell);
            }
            for (Map<String, Object> row : data.rows()) {
                for (String column : data.columns()) {
                    PdfPCell cell = new PdfPCell(new Phrase(format(row.get(column)), BODY));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            if (data.rows().isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No records found for the selected filters.", BODY));
                empty.setColspan(columnCount);
                empty.setPadding(12);
                table.addCell(empty);
            }
            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to render PDF report", ex);
        }
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
