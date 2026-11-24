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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripSummaryReportDataServiceTest {
    private BuyingParty buyingParty(String name) {
        BuyingParty party = new BuyingParty();
        party.setName(name);
        return party;
    }

    private SellingParty sellingParty(String name) {
        SellingParty party = new SellingParty();
        party.setName(name);
        return party;
    }

    @Test
    void includesMortalityAndResolvesBuyerAndSellerNames() {
        TripDeliveryDetailsService detailsService = mock(TripDeliveryDetailsService.class);
        BuyingPartyRepository buyingPartyRepository = mock(BuyingPartyRepository.class);
        SellingPartyRepository sellingPartyRepository = mock(SellingPartyRepository.class);

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
                .sellingParty(sellingParty("Market"))
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
                .buyingParty(buyingParty("Farm"))
                .actualOrders(List.of(purchaseOrder))
                .build();

        Payment buyerPayment = new Payment();
        buyerPayment.setId(31);
        buyerPayment.setBuyingPartyId(5);
        buyerPayment.setAmount(new BigDecimal("5"));
        buyerPayment.setPaymentDate(LocalDate.of(2026, 11, 24));

        Payment sellerPayment = new Payment();
        sellerPayment.setId(32);
        sellerPayment.setSellingPartyId(7);
        sellerPayment.setAmount(new BigDecimal("8"));
        sellerPayment.setPaymentDate(LocalDate.of(2026, 11, 24));

        Expenses expense = new Expenses();
        expense.setId(41);
        expense.setSellingPartyId(6);
        expense.setAmount(new BigDecimal("10"));
        expense.setExpenseDate(LocalDate.of(2026, 11, 24));

        Mortality mortality = new Mortality();
        mortality.setId(51);
        mortality.setCombinedMortality(3);
        mortality.setReason("Transit loss");
        mortality.setCreateDate(LocalDateTime.of(2026, 11, 24, 10, 0));

        TripDeliveryDetailsResponse details = TripDeliveryDetailsResponse.builder()
                .trip(trip)
                .deliveries(List.of(delivery))
                .supplierPickups(List.of(pickup))
                .payments(List.of(buyerPayment, sellerPayment))
                .expenses(List.of(expense))
                .mortalities(List.of(mortality))
                .build();
        when(detailsService.retrieve(42)).thenReturn(Optional.of(details));

        BuyingParty buyer = new BuyingParty();
        buyer.setId(5);
        buyer.setName("Buyer One");
        when(buyingPartyRepository.findAllById(Set.of(5))).thenReturn(List.of(buyer));

        SellingParty sellerOne = new SellingParty();
        sellerOne.setId(6);
        sellerOne.setName("Seller One");
        SellingParty sellerTwo = new SellingParty();
        sellerTwo.setId(7);
        sellerTwo.setName("Seller Two");
        when(sellingPartyRepository.findAllById(Set.of(6, 7))).thenReturn(List.of(sellerOne, sellerTwo));

        ReportData report = new TripSummaryReportDataService(
                detailsService, buyingPartyRepository, sellingPartyRepository).load(42);

        assertEquals(new BigDecimal("100"), report.summaryTotals().get("SALES"));
        assertEquals(new BigDecimal("40"), report.summaryTotals().get("PURCHASE"));
        assertEquals(new BigDecimal("10"), report.summaryTotals().get("EXPENSE"));
        assertEquals(new BigDecimal("50"), report.summaryTotals().get("PROFIT"));
        assertEquals(new BigDecimal("3"), report.summaryTotals().get("MORTALITY"));

        var mortalityRow = report.rows().stream()
                .filter(row -> "MORTALITY".equals(row.get("record_type"))).findFirst().orElseThrow();
        assertEquals(3, mortalityRow.get("quantity"));
        assertEquals("Transit loss", mortalityRow.get("description"));

        var buyerPaymentRow = report.rows().stream()
                .filter(row -> Integer.valueOf(31).equals(row.get("reference_id"))).findFirst().orElseThrow();
        assertEquals("Buyer One", buyerPaymentRow.get("party_name"));

        var sellerPaymentRow = report.rows().stream()
                .filter(row -> Integer.valueOf(32).equals(row.get("reference_id"))).findFirst().orElseThrow();
        assertEquals("Seller Two", sellerPaymentRow.get("party_name"));

        var expenseRow = report.rows().stream()
                .filter(row -> "EXPENSE".equals(row.get("record_type"))).findFirst().orElseThrow();
        assertEquals("Seller One", expenseRow.get("party_name"));
    }
}
