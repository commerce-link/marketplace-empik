package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.MarketplaceListedOffer;
import pl.commercelink.marketplace.api.MarketplaceOffers;
import pl.commercelink.marketplace.api.MarketplaceOffersPage;
import pl.commercelink.marketplace.api.MarketplaceRateLimitedException;
import pl.commercelink.rest.client.HttpClientException;
import pl.commercelink.rest.client.RestApi;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EmpikOffersListing implements MarketplaceOffers {

    private static final int PAGE_SIZE = 100;
    private static final Duration RATE_LIMIT_RETRY_AFTER = Duration.ofSeconds(60);

    private final RestApi restApi;

    EmpikOffersListing(RestApi restApi) {
        this.restApi = restApi;
    }

    @Override
    public MarketplaceOffersPage page(String cursor) {
        int offset = parseOffset(cursor);

        Map<String, String> params = new HashMap<>();
        params.put("max", String.valueOf(PAGE_SIZE));
        params.put("offset", String.valueOf(offset));

        EmpikOffersResponse response;
        try {
            response = restApi.fetch("/api/offers", params, EmpikOffersResponse.class);
        } catch (HttpClientException e) {
            if (e.getStatusCode() == 429) {
                // Why a constant: rest-client 0.1.0 does not expose response headers, so Retry-After cannot be read;
                // Empik was measured answering 55-60 s
                throw new MarketplaceRateLimitedException(RATE_LIMIT_RETRY_AFTER);
            }
            throw e;
        }

        List<EmpikOffersResponse.Offer> offers = response.getOffers();
        int totalCount = response.getTotalCount();
        List<MarketplaceListedOffer> listed = offers == null ? List.of() : offers.stream().map(this::toListedOffer).toList();
        // An empty page ends the walk even if total_count promises more, so a shrinking catalogue cannot loop forever
        boolean last = listed.isEmpty() || offset + PAGE_SIZE >= totalCount;
        return new MarketplaceOffersPage(listed, last ? null : String.valueOf(offset + PAGE_SIZE), totalCount);
    }

    private int parseOffset(String cursor) {
        if (cursor == null) {
            return 0;
        }
        try {
            int offset = Integer.parseInt(cursor);
            if (offset < 0) {
                throw new IllegalArgumentException("Invalid Empik offers cursor: " + cursor);
            }
            return offset;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Empik offers cursor: " + cursor, e);
        }
    }

    private MarketplaceListedOffer toListedOffer(EmpikOffersResponse.Offer offer) {
        List<String> eans = offer.getProductReferences() == null ? List.of() :
                offer.getProductReferences().stream()
                        .filter(r -> "EAN".equals(r.getReferenceType()) && r.getReference() != null)
                        .map(EmpikOffersResponse.ProductReference::getReference)
                        .toList();
        return new MarketplaceListedOffer(
                offer.getShopSku(), eans, offer.getProductTitle(),
                offer.getQuantity(), offer.getPrice(), offer.isActive());
    }
}
