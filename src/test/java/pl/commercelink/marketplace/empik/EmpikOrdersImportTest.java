package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.commercelink.marketplace.api.MarketplaceOrder;
import pl.commercelink.marketplace.api.MarketplaceProduct;
import pl.commercelink.marketplace.api.PickupPoint;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.rest.client.RestApi;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
                  "shipping_carrier_code": "INPOST"
                }
                """, EmpikOrder.class);

        // when
        PickupPoint point = order.toPickupPoint();

        // then
        assertEquals("KRA01M", point.code());
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

    @Test
    void mapsOfferSkuToSellerOfferKeyAndKeepsProductSkuAsManufacturerCode() throws Exception {
        // given
        EmpikOrdersResponse response = MAPPER.readValue("""
                {"orders":[{
                  "order_id": "ORD-1",
                  "customer": {"firstname": "Jan", "lastname": "Kowalski"},
                  "order_lines": [{
                    "offer_sku": "SILLO-84980",
                    "product_sku": "1696938202",
                    "product_title": "Mouse",
                    "price_unit": 10.0,
                    "quantity": 2,
                    "total_commission": 1.0
                  }]
                }]}
                """, EmpikOrdersResponse.class);
        when(restApi.fetch(eq("/api/orders"), any(), eq(EmpikOrdersResponse.class))).thenReturn(response);

        // when
        List<MarketplaceOrder> orders = ordersImport.fetchOrders();

        // then
        MarketplaceProduct product = orders.get(0).products().get(0);
        assertEquals("SILLO-84980", product.sellerOfferKey());
        assertEquals("1696938202", product.manufacturerCode());
    }
}
