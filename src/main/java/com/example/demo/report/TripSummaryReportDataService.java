package com.example.demo.report;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.dto.VendorDeliveryRetrieveResponse;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.service.TripDeliveryDetailsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TripSummaryReportDataService {
    private static final List<String> COLUMNS = List.of(
            "record_type", "report_date", "reference_id", "party_name", "description",
            "quantity", "weight", "rate", "amount", "status");

    private final TripDeliveryDetailsService tripDetails;

    public TripSummaryReportDataService(TripDeliveryDetailsService tripDetails) {
        this.tripDetails = tripDetails;
    }

    public ReportData load(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("A valid tripId is required for Trip Summary");
        }

        TripDeliveryDetailsResponse details = tripDetails.retrieve(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        TripRetrieveResponse trip = details.getTrip();
        List<Map<String, Object>> rows = new ArrayList<>();

        Map<String, Object> tripRow = row("TRIP");
        tripRow.put("report_date", trip.getDate());
        tripRow.put("reference_id", trip.getTripId());
        tripRow.put("description", String.join(" | ",
                "Route: " + value(trip.getRoute()),
                "Vehicle: " + (trip.getVehicle() == null ? "-" : value(trip.getVehicle().getName())),
                "Driver: " + (trip.getDriver() == null ? "-" : value(trip.getDriver().getName())),
                "Status: " + value(trip.getStatus())));
        tripRow.put("status", trip.getStatus());
        rows.add(tripRow);

        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal purchases = BigDecimal.ZERO;
        BigDecimal expensesTotal = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;

        for (DeliveryRetrieveResponse delivery : details.getDeliveries()) {
            if (delivery.getVendorDeliveries() == null) continue;
            for (VendorDeliveryRetrieveResponse vendorDelivery : delivery.getVendorDeliveries()) {
                if (vendorDelivery.getActualOrders() == null) continue;
                for (Order order : vendorDelivery.getActualOrders()) {
                    BigDecimal amount = orderAmount(order);
                    sales = sales.add(amount);
                    quantity = quantity.add(decimal(order.getQty()));
                    rows.add(orderRow("SALES", order, amount, vendorDelivery.getSellingParty() == null
                                    ? null : vendorDelivery.getSellingParty().getName(),
                            "Delivery " + value(delivery.getId())));
                }
            }
        }

        for (SupplierPickupRetrieveResponse pickup : details.getSupplierPickups()) {
            if (pickup.getActualOrders() == null) continue;
            for (Order order : pickup.getActualOrders()) {
                BigDecimal amount = orderAmount(order);
                purchases = purchases.add(amount);
                rows.add(orderRow("PURCHASE", order, amount,
                        pickup.getBuyingParty() == null ? null : pickup.getBuyingParty().getName(),
                        "Pickup " + value(pickup.getId())));
            }
        }

        for (Payment payment : details.getPayments()) {
            Map<String, Object> paymentRow = row("PAYMENT");
            paymentRow.put("report_date", payment.getPaymentDate());
            paymentRow.put("reference_id", payment.getId());
            paymentRow.put("party_name", payment.getBuyingPartyId() != null
                    ? "Buyer #" + payment.getBuyingPartyId()
                    : payment.getSellingPartyId() == null ? null : "Seller #" + payment.getSellingPartyId());
            paymentRow.put("description", String.join(" | ",
                    value(payment.getPaymentType()), value(payment.getPaymentMethod()),
                    value(payment.getExternalReference()), value(payment.getNote())));
            paymentRow.put("amount", payment.getAmount());
            paymentRow.put("status", payment.getStatus());
            rows.add(paymentRow);
        }

        for (Expenses expense : details.getExpenses()) {
            BigDecimal amount = decimal(expense.getAmount());
            expensesTotal = expensesTotal.add(amount);
            Map<String, Object> expenseRow = row("EXPENSE");
            expenseRow.put("report_date", expense.getExpenseDate());
            expenseRow.put("reference_id", expense.getId());
            expenseRow.put("party_name", expense.getSellingPartyId() == null
                    ? null : "Seller #" + expense.getSellingPartyId());
            expenseRow.put("description", String.join(" | ",
                    value(expense.getCategory()), value(expense.getNote())));
            expenseRow.put("amount", expense.getAmount());
            rows.add(expenseRow);
        }

        rows.sort((left, right) -> {
            Object leftDate = left.get("report_date");
            Object rightDate = right.get("report_date");
            if (leftDate == null) return rightDate == null ? 0 : 1;
            if (rightDate == null) return -1;
            return leftDate.toString().compareTo(rightDate.toString());
        });

        Map<String, BigDecimal> totals = Map.of(
                "SALES", sales,
                "PURCHASE", purchases,
                "EXPENSE", expensesTotal,
                "PROFIT", sales.subtract(purchases).subtract(expensesTotal));
        return new ReportData("Trip Summary Report", COLUMNS, rows, rows.size(),
                sales, quantity, totals);
    }

    private Map<String, Object> orderRow(String type, Order order, BigDecimal amount,
                                         String partyName, String source) {
        Map<String, Object> orderRow = row(type);
        orderRow.put("report_date", order.getCreateDate() == null ? null : order.getCreateDate().toLocalDate());
        orderRow.put("reference_id", order.getId());
        orderRow.put("party_name", partyName);
        orderRow.put("description", source);
        orderRow.put("quantity", order.getQty());
        orderRow.put("weight", order.getWeight());
        orderRow.put("rate", order.getRate());
        orderRow.put("amount", amount);
        return orderRow;
    }

    private Map<String, Object> row(String type) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String column : COLUMNS) row.put(column, null);
        row.put("record_type", type);
        return row;
    }

    private BigDecimal orderAmount(Order order) {
        if (order.getTotal() != null) return order.getTotal();
        if (order.getWeight() != null && order.getRate() != null) {
            return order.getWeight().multiply(order.getRate());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal decimal(Number value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }
}
