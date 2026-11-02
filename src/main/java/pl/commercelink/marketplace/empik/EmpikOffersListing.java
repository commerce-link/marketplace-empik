package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.MarketplaceListedOffer;
import pl.commercelink.marketplace.api.MarketplaceOffers;
import pl.commercelink.rest.client.RestApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class EmpikOffersListing implements MarketplaceOffers {

    private static final int PAGE_SIZE = 100;

    private final RestApi restApi;

    EmpikOffersListing(RestApi restApi) {
        this.restApi = restApi;
    }

    @Override
    public void forEachPage(Consumer<List<MarketplaceListedOffer>> page) {
        int offset = 0;
        int totalCount;

        do {
            Map<String, String> params = new HashMap<>();
            params.put("max", String.valueOf(PAGE_SIZE));
            params.put("offset", String.valueOf(offset));

            EmpikOffersResponse response = restApi.fetch("/api/offers", params, EmpikOffersResponse.class);
            List<EmpikOffersResponse.Offer> offers = response.getOffers();
            // An empty page ends the walk even if total_count promises more, so a shrinking catalogue cannot loop forever
            if (offers == null || offers.isEmpty()) {
                return;
            }

            page.accept(offers.stream().map(this::toListedOffer).toList());

            totalCount = response.getTotalCount();
            offset += PAGE_SIZE;
        } while (offset < totalCount);
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
