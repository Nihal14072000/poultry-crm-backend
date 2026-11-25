package com.example.demo.report;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.dto.VendorDeliveryRetrieveResponse;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SellingParty;
import com.example.demo.repository.BuyingPartyRepository;
import com.example.demo.repository.SellingPartyRepository;
import com.example.demo.service.TripDeliveryDetailsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripSummaryReportDataService {
    private static final List<String> COLUMNS = List.of(
            "record_type", "report_date", "reference_id", "party_name", "description",
            "quantity", "weight", "rate", "amount", "status");

    private final TripDeliveryDetailsService tripDetails;
    private final BuyingPartyRepository buyingPartyRepository;
    private final SellingPartyRepository sellingPartyRepository;

    public TripSummaryReportDataService(TripDeliveryDetailsService tripDetails,
                                       BuyingPartyRepository buyingPartyRepository,
                                       SellingPartyRepository sellingPartyRepository) {
        this.tripDetails = tripDetails;
        this.buyingPartyRepository = buyingPartyRepository;
        this.sellingPartyRepository = sellingPartyRepository;
    }

    public ReportData load(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("A valid tripId is required for Trip Summary");
        }

        TripDeliveryDetailsResponse details = tripDetails.retrieve(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        TripRetrieveResponse trip = details.getTrip();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<Integer, String> buyerNames = loadBuyerNames(details);
        Map<Integer, String> sellerNames = loadSellerNames(details);

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
        BigDecimal mortalityTotal = BigDecimal.ZERO;

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

        for (Mortality mortality : details.getMortalities()) {
            BigDecimal birds = decimal(mortality.getCombinedMortality());
            mortalityTotal = mortalityTotal.add(birds);
            Map<String, Object> mortalityRow = row("MORTALITY");
            mortalityRow.put("report_date", mortality.getCreateDate() == null
                    ? null : mortality.getCreateDate().toLocalDate());
            mortalityRow.put("reference_id", mortality.getId());
            mortalityRow.put("description", value(mortality.getReason()));
            mortalityRow.put("quantity", mortality.getCombinedMortality());
            rows.add(mortalityRow);
        }

        for (Payment payment : details.getPayments()) {
            Map<String, Object> paymentRow = row("PAYMENT");
            paymentRow.put("report_date", payment.getPaymentDate());
            paymentRow.put("reference_id", payment.getId());
            paymentRow.put("party_name", payment.getBuyingPartyId() != null
                    ? partyName(buyerNames, payment.getBuyingPartyId(), "Buyer")
                    : partyName(sellerNames, payment.getSellingPartyId(), "Seller"));
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
            expenseRow.put("party_name", partyName(sellerNames, expense.getSellingPartyId(), "Seller"));
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
                "PROFIT", sales.subtract(purchases).subtract(expensesTotal),
                "MORTALITY", mortalityTotal);
        return new ReportData("Trip Summary Report", COLUMNS, rows, rows.size(),
                sales, quantity, totals);
    }

    private Map<Integer, String> loadBuyerNames(TripDeliveryDetailsResponse details) {
        Set<Integer> ids = details.getPayments().stream()
                .map(Payment::getBuyingPartyId).filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return buyingPartyRepository.findAllById(ids).stream()
                .filter(party -> party.getName() != null && !party.getName().isBlank())
                .collect(Collectors.toMap(BuyingParty::getId, BuyingParty::getName, (first, ignored) -> first));
    }

    private Map<Integer, String> loadSellerNames(TripDeliveryDetailsResponse details) {
        Set<Integer> ids = new LinkedHashSet<>();
        details.getPayments().stream().map(Payment::getSellingPartyId)
                .filter(id -> id != null).forEach(ids::add);
        details.getExpenses().stream().map(Expenses::getSellingPartyId)
                .filter(id -> id != null).forEach(ids::add);
        return sellingPartyRepository.findAllById(ids).stream()
                .filter(party -> party.getName() != null && !party.getName().isBlank())
                .collect(Collectors.toMap(SellingParty::getId, SellingParty::getName, (first, ignored) -> first));
    }

    private String partyName(Map<Integer, String> names, Integer id, String type) {
        if (id == null) return null;
        String name = names.get(id);
        return name == null || name.isBlank() ? type + " #" + id : name;
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
