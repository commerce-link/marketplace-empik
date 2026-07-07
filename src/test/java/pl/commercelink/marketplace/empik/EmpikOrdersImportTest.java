package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.rest.client.RestApi;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
