package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.marketplace.api.MarketplaceCustomer;
import pl.commercelink.rest.client.RestApi;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpikOrdersImportTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RestApi restApi;

    @InjectMocks
    private EmpikOrdersImport ordersImport;

    @Test
    void fetchOrdersRequestsOnlyShippingOrders() throws Exception {
        // given
        EmpikOrdersResponse empty = MAPPER.readValue("{\"orders\":[]}", EmpikOrdersResponse.class);
        ArgumentCaptor<Map<String, String>> params = ArgumentCaptor.forClass(Map.class);
        when(restApi.fetch(eq("/api/orders"), params.capture(), eq(EmpikOrdersResponse.class))).thenReturn(empty);

        // when
        ordersImport.fetchOrders();

        // then
        assertEquals("SHIPPING", params.getValue().get("order_state_codes"));
    }

    @Test
    void mapsPudoIdAndCarrierIntoThePickupPoint() throws Exception {
        // given
        EmpikOrder order = MAPPER.readValue("""
                {
                  "shipping_pudo_id": "KRA01M",
                  "shipping_carrier_code": "INPOST",
                  "shipping_type_label": "Paczkomat InPost"
                }
                """, EmpikOrder.class);

        // when
        MarketplaceCustomer.PickupPoint point = order.toPickupPoint();

        // then
        assertEquals("KRA01M", point.id());
        assertEquals("INPOST", point.operator());
        assertEquals("Paczkomat InPost", point.name());
        assertEquals("INPOST", order.getShippingCarrierCode());
    }

    @Test
    void addressDeliveryHasNoPickupPoint() throws Exception {
        // given
        EmpikOrder order = MAPPER.readValue("""
                {"shipping_carrier_code": "DPD"}
                """, EmpikOrder.class);

        // when / then
        assertNull(order.toPickupPoint());
    }
}
