package com.example.demo.report;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.dto.VendorDeliveryRetrieveResponse;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SellingParty;
import com.example.demo.service.TripDeliveryDetailsService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripSummaryReportDataServiceTest {
    @Test
    void buildsSeparateActivityTypesAndTopLevelProfitTotals() {
        TripDeliveryDetailsService detailsService = mock(TripDeliveryDetailsService.class);
        TripRetrieveResponse trip = TripRetrieveResponse.builder()
                .tripId(42)
                .date(LocalDate.of(2026, 11, 24))
                .route("Farm to market")
                .build();

        Order salesOrder = new Order();
        salesOrder.setId(11);
        salesOrder.setQty(10);
        salesOrder.setWeight(new BigDecimal("20"));
        salesOrder.setRate(new BigDecimal("5"));
        salesOrder.setTotal(new BigDecimal("100"));
        VendorDeliveryRetrieveResponse vendorDelivery = VendorDeliveryRetrieveResponse.builder()
                .sellingParty(SellingParty.builder().name("Market").build())
                .actualOrders(List.of(salesOrder))
                .build();
        DeliveryRetrieveResponse delivery = DeliveryRetrieveResponse.builder()
                .id(21)
                .vendorDeliveries(List.of(vendorDelivery))
                .build();

        Order purchaseOrder = new Order();
        purchaseOrder.setId(12);
        purchaseOrder.setQty(8);
        purchaseOrder.setWeight(new BigDecimal("10"));
        purchaseOrder.setRate(new BigDecimal("4"));
        SupplierPickupRetrieveResponse pickup = SupplierPickupRetrieveResponse.builder()
                .id(22)
                .buyingParty(BuyingParty.builder().name("Farm").build())
                .actualOrders(List.of(purchaseOrder))
                .build();

        Payment payment = new Payment();
        payment.setId(31);
        payment.setAmount(new BigDecimal("5"));
        payment.setPaymentDate(LocalDate.of(2026, 11, 24));
        Expenses expense = new Expenses();
        expense.setId(41);
        expense.setAmount(new BigDecimal("10"));
        expense.setExpenseDate(LocalDate.of(2026, 11, 24));
        TripDeliveryDetailsResponse details = TripDeliveryDetailsResponse.builder()
                .trip(trip)
                .deliveries(List.of(delivery))
                .supplierPickups(List.of(pickup))
                .payments(List.of(payment))
                .expenses(List.of(expense))
                .build();
        when(detailsService.retrieve(42)).thenReturn(Optional.of(details));

        ReportData report = new TripSummaryReportDataService(detailsService).load(42);

        assertEquals(new BigDecimal("100"), report.summaryTotals().get("SALES"));
        assertEquals(new BigDecimal("40"), report.summaryTotals().get("PURCHASE"));
        assertEquals(new BigDecimal("10"), report.summaryTotals().get("EXPENSE"));
        assertEquals(new BigDecimal("50"), report.summaryTotals().get("PROFIT"));
        assertEquals(List.of("TRIP", "SALES", "PURCHASE", "PAYMENT", "EXPENSE"),
                report.rows().stream().map(row -> row.get("record_type")).toList());
    }
}
