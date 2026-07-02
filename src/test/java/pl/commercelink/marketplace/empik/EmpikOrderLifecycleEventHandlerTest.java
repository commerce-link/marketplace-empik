package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.marketplace.api.ShipmentUpdate;
import pl.commercelink.rest.client.RestApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpikOrderLifecycleEventHandlerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RestApi restApi;

    @InjectMocks
    private EmpikOrderLifecycleEventHandler handler;

    @Test
    void acceptOrderAcceptsAllLinesOfWaitingAcceptanceOrder() throws Exception {
        // given
        givenFetchedOrder("WAITING_ACCEPTANCE");

        // when
        handler.acceptOrder("ORDER-1");

        // then
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(restApi).put(eq("/api/orders/ORDER-1/accept"), body.capture(), eq(Void.class));
        assertEquals(
                "{\"order_lines\":[{\"accepted\":true,\"id\":\"LINE-1\"},{\"accepted\":true,\"id\":\"LINE-2\"}]}",
                MAPPER.writeValueAsString(body.getValue())
        );
    }

    @Test
    void acceptOrderSendsDecisionsOnlyForLinesAwaitingAcceptance() throws Exception {
        // given
        givenFetchedOrderWithLineStates("WAITING_ACCEPTANCE", "WAITING_ACCEPTANCE", "CANCELED");

        // when
        handler.acceptOrder("ORDER-1");

        // then
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(restApi).put(eq("/api/orders/ORDER-1/accept"), body.capture(), eq(Void.class));
        assertEquals(
                "{\"order_lines\":[{\"accepted\":true,\"id\":\"LINE-1\"}]}",
                MAPPER.writeValueAsString(body.getValue())
        );
    }

    @Test
    void acceptOrderSkipsCallWhenNoLineAwaitsAcceptance() throws Exception {
        // given
        givenFetchedOrderWithLineStates("WAITING_ACCEPTANCE", "CANCELED", "CANCELED");

        // when
        handler.acceptOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void acceptOrderSkipsOrderThatIsNoLongerWaitingAcceptance() throws Exception {
        // given
        givenFetchedOrder("SHIPPING");

        // when
        handler.acceptOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void acceptOrderSkipsUnknownOrder() throws Exception {
        // given
        givenNoOrder();

        // when
        handler.acceptOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void shipOrderSendsTrackingBeforeShipValidation() throws Exception {
        // given
        givenFetchedOrder("SHIPPING");
        ShipmentUpdate update = new ShipmentUpdate("TRACK-9", "DPD", "https://track.example/TRACK-9");

        // when
        handler.shipOrder("ORDER-1", update);

        // then
        ArgumentCaptor<Object> tracking = ArgumentCaptor.forClass(Object.class);
        InOrder order = inOrder(restApi);
        order.verify(restApi).put(eq("/api/orders/ORDER-1/tracking"), tracking.capture(), eq(Void.class));
        order.verify(restApi).put(eq("/api/orders/ORDER-1/ship"), any(), eq(Void.class));
        assertEquals(
                "{\"carrier_name\":\"DPD\",\"carrier_url\":\"https://track.example/TRACK-9\",\"tracking_number\":\"TRACK-9\"}",
                MAPPER.writeValueAsString(tracking.getValue())
        );
    }

    @Test
    void shipOrderSkipsTrackingWhenShipmentHasNoTracking() throws Exception {
        // given
        givenFetchedOrder("SHIPPING");
        ShipmentUpdate update = new ShipmentUpdate(null, null, null);

        // when
        handler.shipOrder("ORDER-1", update);

        // then
        verify(restApi, never()).put(eq("/api/orders/ORDER-1/tracking"), any(), any());
        verify(restApi).put(eq("/api/orders/ORDER-1/ship"), any(), eq(Void.class));
    }

    @Test
    void shipOrderSkipsOrderAlreadyShipped() throws Exception {
        // given
        givenFetchedOrder("SHIPPED");

        // when
        handler.shipOrder("ORDER-1", new ShipmentUpdate("TRACK-9", "DPD", "https://track.example/TRACK-9"));

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void shipOrderSkipsCancelledOrder() throws Exception {
        // given
        givenFetchedOrder("CANCELED");

        // when
        handler.shipOrder("ORDER-1", new ShipmentUpdate("TRACK-9", "DPD", "https://track.example/TRACK-9"));

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void shipOrderSkipsUnknownOrder() throws Exception {
        // given
        givenNoOrder();

        // when
        handler.shipOrder("ORDER-1", new ShipmentUpdate("TRACK-9", "DPD", "https://track.example/TRACK-9"));

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void shipOrderStillAttemptsShipWhenOrderAwaitsDebit() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT");

        // when
        handler.shipOrder("ORDER-1", new ShipmentUpdate(null, null, null));

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/ship"), any(), eq(Void.class));
    }

    @Test
    void cancelOrderRefusesLinesWhenOrderStillWaitsForAcceptance() throws Exception {
        // given
        givenFetchedOrder("WAITING_ACCEPTANCE");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(restApi).put(eq("/api/orders/ORDER-1/accept"), body.capture(), eq(Void.class));
        assertEquals(
                "{\"order_lines\":[{\"accepted\":false,\"id\":\"LINE-1\"},{\"accepted\":false,\"id\":\"LINE-2\"}]}",
                MAPPER.writeValueAsString(body.getValue())
        );
    }

    @Test
    void cancelOrderRefusesOnlyLinesAwaitingAcceptance() throws Exception {
        // given
        givenFetchedOrderWithLineStates("WAITING_ACCEPTANCE", "WAITING_ACCEPTANCE", "CANCELED");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(restApi).put(eq("/api/orders/ORDER-1/accept"), body.capture(), eq(Void.class));
        assertEquals(
                "{\"order_lines\":[{\"accepted\":false,\"id\":\"LINE-1\"}]}",
                MAPPER.writeValueAsString(body.getValue())
        );
    }

    @Test
    void cancelOrderPerformsFullCancelationOfAcceptedOrder() throws Exception {
        // given
        givenFetchedOrder("SHIPPING");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/cancel"), any(), eq(Void.class));
        verify(restApi, never()).put(eq("/api/orders/ORDER-1/accept"), any(), any());
    }

    @Test
    void cancelOrderSkipsOrderInNonCancellableState() throws Exception {
        // given
        givenFetchedOrder("SHIPPED");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void cancelOrderPerformsFullCancelationWhenAwaitingDebit() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/cancel"), any(), eq(Void.class));
        verify(restApi, never()).put(eq("/api/orders/ORDER-1/accept"), any(), any());
    }

    @Test
    void cancelOrderPerformsFullCancelationWhenAwaitingDebitPayment() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT_PAYMENT");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/cancel"), any(), eq(Void.class));
        verify(restApi, never()).put(eq("/api/orders/ORDER-1/accept"), any(), any());
    }

    @Test
    void cancelOrderSkipsUnknownOrder() throws Exception {
        // given
        givenNoOrder();

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    private void givenFetchedOrder(String state) throws Exception {
        EmpikOrdersResponse response = MAPPER.readValue(
                """
                {"orders":[{"order_id":"ORDER-1","order_state":"%s",
                  "order_lines":[{"order_line_id":"LINE-1"},{"order_line_id":"LINE-2"}]}]}
                """.formatted(state),
                EmpikOrdersResponse.class
        );
        when(restApi.fetch(eq("/api/orders"), anyMap(), eq(EmpikOrdersResponse.class))).thenReturn(response);
    }

    private void givenFetchedOrderWithLineStates(String orderState, String line1State, String line2State) throws Exception {
        EmpikOrdersResponse response = MAPPER.readValue(
                """
                {"orders":[{"order_id":"ORDER-1","order_state":"%s",
                  "order_lines":[{"order_line_id":"LINE-1","order_line_state":"%s"},
                    {"order_line_id":"LINE-2","order_line_state":"%s"}]}]}
                """.formatted(orderState, line1State, line2State),
                EmpikOrdersResponse.class
        );
        when(restApi.fetch(eq("/api/orders"), anyMap(), eq(EmpikOrdersResponse.class))).thenReturn(response);
    }

    private void givenNoOrder() throws Exception {
        EmpikOrdersResponse response = MAPPER.readValue("{\"orders\":[]}", EmpikOrdersResponse.class);
        when(restApi.fetch(eq("/api/orders"), anyMap(), eq(EmpikOrdersResponse.class))).thenReturn(response);
    }
}
