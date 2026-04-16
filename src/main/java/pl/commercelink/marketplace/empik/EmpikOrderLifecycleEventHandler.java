package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.InvoiceUpdate;
import pl.commercelink.marketplace.api.MarketplaceOrderStatus;
import pl.commercelink.marketplace.api.ShipmentUpdate;
import pl.commercelink.rest.client.RestApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class EmpikOrderLifecycleEventHandler {

    private final RestApi restApi;

    EmpikOrderLifecycleEventHandler(RestApi restApi) {
        this.restApi = restApi;
    }

    void updateOrderStatus(String externalOrderId, MarketplaceOrderStatus status) {
        if (status != MarketplaceOrderStatus.Shipping) {
            return;
        }

        EmpikOrder order = fetchOrder(externalOrderId);
        if (order == null) return;

        List<ShipOrderLine> lines = order.getOrderLines().stream()
                .map(line -> new ShipOrderLine(line.getOrderLineId()))
                .collect(Collectors.toList());

        ShipOrderRequest request = new ShipOrderRequest(externalOrderId, lines);
        restApi.put("/api/orders/ship", List.of(request), String.class);
    }

    void updateShipment(String externalOrderId, ShipmentUpdate update) {
        EmpikOrder order = fetchOrder(externalOrderId);
        if (order == null) return;

        List<TrackingOrderLine> lines = order.getOrderLines().stream()
                .map(line -> new TrackingOrderLine(
                        line.getOrderLineId(),
                        update.trackingNo(),
                        update.carrier(),
                        update.trackingUrl()
                ))
                .collect(Collectors.toList());

        TrackingUpdateRequest request = new TrackingUpdateRequest(externalOrderId, lines);
        restApi.put("/api/orders/tracking", List.of(request), String.class);
    }

    void updateInvoice(String externalOrderId, InvoiceUpdate update) {
        // Empik document upload (OR74) requires multipart file upload.
        // To be implemented after verifying the exact format on sandbox.
    }

    private EmpikOrder fetchOrder(String orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("order_ids", orderId);

        EmpikOrdersResponse response = restApi.fetch("/api/orders", params, EmpikOrdersResponse.class);
        if (response.getOrders() == null || response.getOrders().isEmpty()) {
            return null;
        }
        return response.getOrders().get(0);
    }

    static class ShipOrderRequest {

        @JsonProperty("order_id")
        private final String orderId;

        @JsonProperty("order_lines")
        private final List<ShipOrderLine> orderLines;

        ShipOrderRequest(String orderId, List<ShipOrderLine> orderLines) {
            this.orderId = orderId;
            this.orderLines = orderLines;
        }

        public String getOrderId() {
            return orderId;
        }

        public List<ShipOrderLine> getOrderLines() {
            return orderLines;
        }
    }

    static class ShipOrderLine {

        @JsonProperty("id")
        private final String id;

        ShipOrderLine(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    static class TrackingUpdateRequest {

        @JsonProperty("order_id")
        private final String orderId;

        @JsonProperty("order_lines")
        private final List<TrackingOrderLine> orderLines;

        TrackingUpdateRequest(String orderId, List<TrackingOrderLine> orderLines) {
            this.orderId = orderId;
            this.orderLines = orderLines;
        }

        public String getOrderId() {
            return orderId;
        }

        public List<TrackingOrderLine> getOrderLines() {
            return orderLines;
        }
    }

    static class TrackingOrderLine {

        @JsonProperty("id")
        private final String id;

        @JsonProperty("tracking_number")
        private final String trackingNumber;

        @JsonProperty("carrier_code")
        private final String carrierCode;

        @JsonProperty("tracking_url")
        private final String trackingUrl;

        TrackingOrderLine(String id, String trackingNumber, String carrierCode, String trackingUrl) {
            this.id = id;
            this.trackingNumber = trackingNumber;
            this.carrierCode = carrierCode;
            this.trackingUrl = trackingUrl;
        }

        public String getId() {
            return id;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public String getCarrierCode() {
            return carrierCode;
        }

        public String getTrackingUrl() {
            return trackingUrl;
        }
    }
}
