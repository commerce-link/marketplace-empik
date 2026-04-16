package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.MarketplaceOffer;

@JsonInclude(JsonInclude.Include.NON_NULL)
class EmpikOffer {

    @JsonProperty("shop_sku")
    private String shopSku;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_id_type")
    private String productIdType;

    @JsonProperty("price")
    private double price;

    @JsonProperty("quantity")
    private long quantity;

    @JsonProperty("state_code")
    private String stateCode;

    @JsonProperty("leadtime_to_ship")
    private String leadtimeToShip;

    @JsonProperty("update_delete")
    private String updateDelete;

    public static EmpikOffer createOrUpdateOffer(MarketplaceOffer offer) {
        EmpikOffer empikOffer = new EmpikOffer();
        empikOffer.shopSku = offer.productId();
        empikOffer.productId = offer.ean();
        empikOffer.productIdType = "EAN";
        empikOffer.price = offer.price();
        empikOffer.quantity = offer.quantity();
        empikOffer.stateCode = "11";
        empikOffer.leadtimeToShip = String.valueOf(offer.estimatedDeliveryDays());
        empikOffer.updateDelete = "update";
        return empikOffer;
    }

    public static EmpikOffer unpublishOffer(MarketplaceOffer offer) {
        EmpikOffer empikOffer = new EmpikOffer();
        empikOffer.shopSku = offer.productId();
        empikOffer.productId = offer.ean();
        empikOffer.productIdType = "EAN";
        empikOffer.price = offer.price();
        empikOffer.quantity = 0;
        empikOffer.updateDelete = "delete";
        return empikOffer;
    }

    public String getShopSku() {
        return shopSku;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductIdType() {
        return productIdType;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getLeadtimeToShip() {
        return leadtimeToShip;
    }

    public String getUpdateDelete() {
        return updateDelete;
    }
}
