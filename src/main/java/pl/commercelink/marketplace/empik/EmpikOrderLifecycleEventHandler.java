package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.InvoiceUpdate;
import pl.commercelink.marketplace.api.ShipmentUpdate;
import pl.commercelink.rest.client.RestApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class EmpikOrderLifecycleEventHandler {

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
        // Acceptance is performed by the seller on the Mirakl panel, and EmpikOrdersImport only
        // pulls orders that are already SHIPPING, so an order can never reach Commerce Link in
        // WAITING_ACCEPTANCE. Accepting here is therefore a no-op. Widening the import filter
        // means restoring the OR21 line-decision call (accepted: true) gated on WAITING_ACCEPTANCE.
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
                update.carrierName(),
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

    private EmpikOrder fetchOrder(String orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("order_ids", orderId);

        EmpikOrdersResponse response = restApi.fetch("/api/orders", params, EmpikOrdersResponse.class);
        if (response.getOrders() == null || response.getOrders().isEmpty()) {
            return null;
        }
        return response.getOrders().get(0);
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
