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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpikOrderLifecycleEventHandlerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RestApi restApi;

    @InjectMocks
    private EmpikOrderLifecycleEventHandler handler;

    @Test
    void acceptOrderDoesNothingBecauseAcceptanceHappensOnTheMiraklPanel() {
        // given
        // no stubbing: the handler must not reach the Mirakl API at all

        // when
        handler.acceptOrder("ORDER-1");

        // then
        verifyNoInteractions(restApi);
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
    void shipOrderDoesNothingWhenShipmentHasNoTracking() throws Exception {
        // given
        ShipmentUpdate update = new ShipmentUpdate(null, null, null);

        // when
        handler.shipOrder("ORDER-1", update);

        // then
        verifyNoInteractions(restApi);
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
    void shipOrderShipsWhenOrderAwaitsDebitAndTrackingIsPresent() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT");

        // when
        handler.shipOrder("ORDER-1", new ShipmentUpdate("TRACK-9", "DPD", "https://track.example/TRACK-9"));

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/ship"), any(), eq(Void.class));
    }

    @Test
    void cancelOrderPerformsFullCancelationOfAcceptedOrder() throws Exception {
        // given
        givenFetchedOrder("SHIPPING");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi).put(eq("/api/orders/ORDER-1/cancel"), any(), eq(Void.class));
    }

    @Test
    void cancelOrderSkipsOrderStillWaitingForAcceptance() throws Exception {
        // given
        givenFetchedOrder("WAITING_ACCEPTANCE");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void cancelOrderSkipsOrderAwaitingDebit() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
    }

    @Test
    void cancelOrderSkipsOrderAwaitingDebitPayment() throws Exception {
        // given
        givenFetchedOrder("WAITING_DEBIT_PAYMENT");

        // when
        handler.cancelOrder("ORDER-1");

        // then
        verify(restApi, never()).put(anyString(), any(), any());
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

    private void givenNoOrder() throws Exception {
        EmpikOrdersResponse response = MAPPER.readValue("{\"orders\":[]}", EmpikOrdersResponse.class);
        when(restApi.fetch(eq("/api/orders"), anyMap(), eq(EmpikOrdersResponse.class))).thenReturn(response);
    }
}
