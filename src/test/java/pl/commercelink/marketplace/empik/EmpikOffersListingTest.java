package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.marketplace.api.MarketplaceListedOffer;
import pl.commercelink.rest.client.RestApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpikOffersListingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RestApi restApi;

    @Test
    void pagesThroughAllOffersUsingOffset() throws Exception {
        // given
        ArgumentCaptor<Map<String, String>> params = ArgumentCaptor.forClass(Map.class);
        when(restApi.fetch(eq("/api/offers"), params.capture(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(150, 100), response(150, 50));
        List<List<MarketplaceListedOffer>> pages = new ArrayList<>();

        // when
        new EmpikOffersListing(restApi).forEachPage(pages::add);

        // then
        assertEquals(2, pages.size());
        assertEquals(100, pages.get(0).size());
        assertEquals(50, pages.get(1).size());
        verify(restApi, times(2)).fetch(eq("/api/offers"), params.capture(), eq(EmpikOffersResponse.class));
        assertEquals("0", params.getAllValues().get(0).get("offset"));
        assertEquals("100", params.getAllValues().get(1).get("offset"));
        assertEquals("100", params.getAllValues().get(0).get("max"));
    }

    @Test
    void takesOnlyEanReferencesAndMapsFields() throws Exception {
        // given
        EmpikOffersResponse response = MAPPER.readValue("""
                {
                  "total_count": 1,
                  "unknown": true,
                  "offers": [{
                    "shop_sku": "SKU-1",
                    "product_title": "Mouse",
                    "quantity": 7,
                    "price": 49.90,
                    "active": true,
                    "product_references": [
                      {"reference": "5903771700818", "reference_type": "EAN"},
                      {"reference": "MPN-1", "reference_type": "MPN"}
                    ]
                  }]
                }
                """, EmpikOffersResponse.class);
        when(restApi.fetch(eq("/api/offers"), org.mockito.ArgumentMatchers.anyMap(), eq(EmpikOffersResponse.class)))
                .thenReturn(response);
        List<MarketplaceListedOffer> collected = new ArrayList<>();

        // when
        new EmpikOffersListing(restApi).forEachPage(collected::addAll);

        // then
        MarketplaceListedOffer offer = collected.get(0);
        assertEquals(List.of("5903771700818"), offer.eans());
        assertEquals("SKU-1", offer.sellerOfferKey());
        assertEquals("Mouse", offer.title());
        assertEquals(7, offer.quantity());
        assertEquals(0, new BigDecimal("49.90").compareTo(offer.price()));
        assertTrue(offer.active());
    }

    @Test
    void stopsOnEmptyPage() throws Exception {
        // given
        when(restApi.fetch(eq("/api/offers"), org.mockito.ArgumentMatchers.anyMap(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(500, 0));
        List<List<MarketplaceListedOffer>> pages = new ArrayList<>();

        // when
        new EmpikOffersListing(restApi).forEachPage(pages::add);

        // then
        assertTrue(pages.isEmpty());
        verify(restApi, times(1)).fetch(eq("/api/offers"), org.mockito.ArgumentMatchers.anyMap(), eq(EmpikOffersResponse.class));
    }

    @Test
    void descriptorAdvertisesOfferListing() {
        // given / when / then
        assertTrue(new EmpikMarketplaceProviderDescriptor().supportsOfferListing());
    }

    private EmpikOffersResponse response(int total, int offers) throws Exception {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < offers; i++) {
            if (i > 0) items.append(",");
            items.append("{\"shop_sku\":\"S").append(i).append("\",\"quantity\":1,\"price\":1,\"active\":true}");
        }
        return MAPPER.readValue("{\"total_count\":" + total + ",\"offers\":[" + items + "]}", EmpikOffersResponse.class);
    }
}
