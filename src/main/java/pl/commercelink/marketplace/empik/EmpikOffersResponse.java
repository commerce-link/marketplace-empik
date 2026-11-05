package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikOffersResponse {

    @JsonProperty("offers")
    private List<Offer> offers;

    @JsonProperty("total_count")
    private int totalCount;

    public EmpikOffersResponse() {
    }

    List<Offer> getOffers() {
        return offers;
    }

    int getTotalCount() {
        return totalCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Offer {

        @JsonProperty("shop_sku")
        private String shopSku;

        @JsonProperty("product_title")
        private String productTitle;

        @JsonProperty("quantity")
        private long quantity;

        @JsonProperty("price")
        private BigDecimal price;

        @JsonProperty("active")
        private boolean active;

        @JsonProperty("product_references")
        private List<ProductReference> productReferences;

        String getShopSku() {
            return shopSku;
        }

        String getProductTitle() {
            return productTitle;
        }

        long getQuantity() {
            return quantity;
        }

        BigDecimal getPrice() {
            return price;
        }

        boolean isActive() {
            return active;
        }

        List<ProductReference> getProductReferences() {
            return productReferences;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ProductReference {

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("reference_type")
        private String referenceType;

        String getReference() {
            return reference;
        }

        String getReferenceType() {
            return referenceType;
        }
    }
}
