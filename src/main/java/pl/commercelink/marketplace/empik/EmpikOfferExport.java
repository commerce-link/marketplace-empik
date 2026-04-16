package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.MarketplaceOffer;
import pl.commercelink.rest.client.RestApi;

import java.util.ArrayList;
import java.util.List;

class EmpikOfferExport {

    private final RestApi restApi;

    EmpikOfferExport(RestApi restApi) {
        this.restApi = restApi;
    }

    void export(List<MarketplaceOffer> offersToPublish, List<MarketplaceOffer> offersToRemove) {
        List<EmpikOffer> empikOffers = new ArrayList<>();
        empikOffers.addAll(offersToPublish.stream().map(EmpikOffer::createOrUpdateOffer).toList());
        empikOffers.addAll(offersToRemove.stream().map(EmpikOffer::unpublishOffer).toList());

        OffersRequest request = new OffersRequest(empikOffers);
        restApi.post("/api/offers", request, EmpikOffersImportResponse.class);
    }

    static class OffersRequest {

        @JsonProperty("offers")
        private final List<EmpikOffer> offers;

        OffersRequest(List<EmpikOffer> offers) {
            this.offers = offers;
        }

        public List<EmpikOffer> getOffers() {
            return offers;
        }
    }
}
