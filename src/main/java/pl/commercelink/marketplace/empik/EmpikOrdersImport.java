package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.MarketplaceOrder;
import pl.commercelink.marketplace.api.MarketplaceProduct;
import pl.commercelink.rest.client.RestApi;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

class EmpikOrdersImport {

    private final RestApi restApi;

    EmpikOrdersImport(RestApi restApi) {
        this.restApi = restApi;
    }

    List<MarketplaceOrder> fetchOrders() {
        List<MarketplaceOrder> allOrders = new ArrayList<>();
        String pageToken = null;

        do {
            Map<String, String> params = new HashMap<>();
            params.put("order_state_codes", "SHIPPING");
            params.put("limit", "100");
            if (pageToken != null) {
                params.put("page_token", pageToken);
            }

            EmpikOrdersResponse response = restApi.fetch("/api/orders", params, EmpikOrdersResponse.class);

            if (response.getOrders() != null) {
                response.getOrders().stream()
                        .map(this::toMarketplaceOrder)
                        .forEach(allOrders::add);
            }

            pageToken = response.hasNextPage() ? response.getNextPageToken() : null;
        } while (pageToken != null);

        return allOrders;
    }

    private MarketplaceOrder toMarketplaceOrder(EmpikOrder empikOrder) {
        var marketplaceCustomer = empikOrder.getCustomer().toMarketplaceCustomer(
                empikOrder.getCustomerNotificationEmail(),
                empikOrder.findAdditionalField("nip")
        );

        List<MarketplaceProduct> products = empikOrder.getOrderLines().stream()
                .map(line -> new MarketplaceProduct(
                        line.getProductTitle(),
                        line.getProductSku(),
                        BigDecimal.valueOf(line.getPriceUnit()),
                        line.getQuantity(),
                        BigDecimal.valueOf(line.getTotalCommission())
                ))
                .collect(Collectors.toList());

        return new MarketplaceOrder(
                empikOrder.getOrderId(),
                marketplaceCustomer,
                products,
                BigDecimal.valueOf(empikOrder.getShippingPrice()),
                resolvePaymentType(empikOrder.getPaymentType()),
                empikOrder.getTransactionNumber()
        );
    }

    private String resolvePaymentType(String paymentType) {
        if (paymentType == null) return "DirectDebit";
        if (paymentType.toLowerCase().contains("pobranie")) return "CashOnDelivery";
        return "DirectDebit";
    }
}
