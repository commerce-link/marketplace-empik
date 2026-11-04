package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.commercelink.marketplace.api.MarketplaceListedOffer;
import pl.commercelink.marketplace.api.MarketplaceOffersPage;
import pl.commercelink.marketplace.api.MarketplaceRateLimitedException;
import pl.commercelink.rest.client.HttpClientException;
import pl.commercelink.rest.client.RestApi;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpikOffersListingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RestApi restApi;

    @Test
    void firstPageStartsAtOffsetZero() throws Exception {
        // given
        ArgumentCaptor<Map<String, String>> params = ArgumentCaptor.forClass(Map.class);
        when(restApi.fetch(eq("/api/offers"), params.capture(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(250, 100));

        // when
        MarketplaceOffersPage page = new EmpikOffersListing(restApi).page(null);

        // then
        assertEquals("0", params.getValue().get("offset"));
        assertEquals("100", params.getValue().get("max"));
        assertEquals(100, page.offers().size());
        assertEquals(250, page.total());
    }

    @Test
    void nextCursorIsTheNextOffset() throws Exception {
        // given
        ArgumentCaptor<Map<String, String>> params = ArgumentCaptor.forClass(Map.class);
        when(restApi.fetch(eq("/api/offers"), params.capture(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(250, 100));

        // when
        MarketplaceOffersPage page = new EmpikOffersListing(restApi).page("100");

        // then
        assertEquals("100", params.getValue().get("offset"));
        assertEquals("200", page.nextCursor());
    }

    @Test
    void lastPageHasNoNextCursor() throws Exception {
        // given
        when(restApi.fetch(eq("/api/offers"), anyMap(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(150, 50));

        // when
        MarketplaceOffersPage page = new EmpikOffersListing(restApi).page("100");

        // then
        assertNull(page.nextCursor());
        assertEquals(50, page.offers().size());
    }

    @Test
    void translatesTooManyRequestsIntoRateLimit() {
        // given
        when(restApi.fetch(eq("/api/offers"), anyMap(), eq(EmpikOffersResponse.class)))
                .thenThrow(new HttpClientException(429, "Too Many Requests"));

        // when
        MarketplaceRateLimitedException exception = assertThrows(MarketplaceRateLimitedException.class,
                () -> new EmpikOffersListing(restApi).page(null));

        // then
        assertEquals(Duration.ofSeconds(60), exception.retryAfter());
    }

    @Test
    void otherHttpErrorsPropagate() {
        // given
        HttpClientException failure = new HttpClientException(500, "boom");
        when(restApi.fetch(eq("/api/offers"), anyMap(), eq(EmpikOffersResponse.class))).thenThrow(failure);

        // when / then
        assertSame(failure, assertThrows(HttpClientException.class, () -> new EmpikOffersListing(restApi).page(null)));
    }

    @Test
    void rejectsNonNumericCursor() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> new EmpikOffersListing(restApi).page("abc"));
        verifyNoInteractions(restApi);
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
                      {"reference": "2001000000012", "reference_type": "EAN"},
                      {"reference": "MPN-1", "reference_type": "MPN"}
                    ]
                  }]
                }
                """, EmpikOffersResponse.class);
        when(restApi.fetch(eq("/api/offers"), anyMap(), eq(EmpikOffersResponse.class)))
                .thenReturn(response);

        // when
        MarketplaceListedOffer offer = new EmpikOffersListing(restApi).page(null).offers().get(0);

        // then
        assertEquals(List.of("2001000000012"), offer.eans());
        assertEquals("SKU-1", offer.sellerOfferKey());
        assertEquals("Mouse", offer.title());
        assertEquals(7, offer.quantity());
        assertEquals(0, new BigDecimal("49.90").compareTo(offer.price()));
        assertTrue(offer.active());
    }

    @Test
    void stopsOnEmptyPage() throws Exception {
        // given
        when(restApi.fetch(eq("/api/offers"), anyMap(), eq(EmpikOffersResponse.class)))
                .thenReturn(response(500, 0));

        // when
        MarketplaceOffersPage page = new EmpikOffersListing(restApi).page("100");

        // then
        assertTrue(page.offers().isEmpty());
        assertNull(page.nextCursor());
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
