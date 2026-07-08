package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.InvoiceUpdate;
import pl.commercelink.marketplace.api.ShipmentUpdate;
import pl.commercelink.rest.client.RestApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class EmpikOrderLifecycleEventHandler {

    private static final String WAITING_ACCEPTANCE = "WAITING_ACCEPTANCE";

    private static final String SHIPPING = "SHIPPING";

    // States where marking the order as shipped is pointless (already shipped) or
    // impossible (order closed on the Mirakl side); earlier states stay fail-loud so
    // SQS retries can complete the shipment once the order reaches SHIPPING.
    private static final Set<String> SHIPPED_OR_CLOSED_ORDER_STATES = Set.of(
            "SHIPPED", "TO_COLLECT", "RECEIVED", "CLOSED", "CANCELED", "REFUSED"
    );

    private final RestApi restApi;

    EmpikOrderLifecycleEventHandler(RestApi restApi) {
        this.restApi = restApi;
    }

    void acceptOrder(String externalOrderId) {
        EmpikOrder order = fetchOrder(externalOrderId);
        if (order == null || !WAITING_ACCEPTANCE.equals(order.getOrderState())) {
            return;
        }
        acceptOrderLines(order);
    }

    void shipOrder(String externalOrderId, ShipmentUpdate update) {
        // Mirakl (OR24) rejects a ship transition without a registered tracking number
        // (OR23), so with no tracking data we do nothing at all.
        if (update.trackingNo() == null) {
            return;
        }
        EmpikOrder order = fetchOrder(externalOrderId);
        if (order == null || SHIPPED_OR_CLOSED_ORDER_STATES.contains(order.getOrderState())) {
            return;
        }
        TrackingUpdateRequest tracking = new TrackingUpdateRequest(
                update.carrier(),
                update.trackingUrl(),
                update.trackingNo()
        );
        restApi.put("/api/orders/" + externalOrderId + "/tracking", tracking, Void.class);
        restApi.put("/api/orders/" + externalOrderId + "/ship", Map.of(), Void.class);
    }

    // Acceptance happens on the Mirakl panel and EmpikOrdersImport only pulls SHIPPING orders,
    // so an order reaching Commerce Link has always been accepted; Mirakl states never move
    // backwards, hence SHIPPING is the only state a cancel can legitimately observe. Any other
    // state (already shipped, closed, cancelled) is a no-op rather than a 4xx that would loop
    // the SQS message into the DLQ. Widening the import filter means revisiting this gate:
    // cancelling a not-yet-accepted order requires refusing its lines (OR21), not OR29.
    void cancelOrder(String externalOrderId) {
        EmpikOrder order = fetchOrder(externalOrderId);
        if (order == null || !SHIPPING.equals(order.getOrderState())) {
            return;
        }
        restApi.put("/api/orders/" + externalOrderId + "/cancel", Map.of(), Void.class);
    }

    void updateInvoice(String externalOrderId, InvoiceUpdate update) {
        // Empik document upload (OR74) requires multipart file upload.
        // To be implemented after verifying the exact format on sandbox.
    }

    private void acceptOrderLines(EmpikOrder order) {
        List<AcceptOrderLine> lines = order.getOrderLines().stream()
                .filter(line -> line.getOrderLineState() == null || WAITING_ACCEPTANCE.equals(line.getOrderLineState()))
                .map(line -> new AcceptOrderLine(true, line.getOrderLineId()))
                .collect(Collectors.toList());
        if (lines.isEmpty()) {
            return;
        }
        restApi.put("/api/orders/" + order.getOrderId() + "/accept", new AcceptOrderRequest(lines), Void.class);
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

    static class AcceptOrderRequest {

        @JsonProperty("order_lines")
        private final List<AcceptOrderLine> orderLines;

        AcceptOrderRequest(List<AcceptOrderLine> orderLines) {
            this.orderLines = orderLines;
        }

        public List<AcceptOrderLine> getOrderLines() {
            return orderLines;
        }
    }

    static class AcceptOrderLine {

        @JsonProperty("accepted")
        private final boolean accepted;

        @JsonProperty("id")
        private final String id;

        AcceptOrderLine(boolean accepted, String id) {
            this.accepted = accepted;
            this.id = id;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getId() {
            return id;
        }
    }

    static class TrackingUpdateRequest {

        @JsonProperty("carrier_name")
        private final String carrierName;

        @JsonProperty("carrier_url")
        private final String carrierUrl;

        @JsonProperty("tracking_number")
        private final String trackingNumber;

        TrackingUpdateRequest(String carrierName, String carrierUrl, String trackingNumber) {
            this.carrierName = carrierName;
            this.carrierUrl = carrierUrl;
            this.trackingNumber = trackingNumber;
        }

        public String getCarrierName() {
            return carrierName;
        }

        public String getCarrierUrl() {
            return carrierUrl;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }
    }
}
