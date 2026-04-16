package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikOrderLine {

    @JsonProperty("order_line_id")
    private String orderLineId;

    @JsonProperty("offer_id")
    private Long offerId;

    @JsonProperty("offer_sku")
    private String offerSku;

    @JsonProperty("product_sku")
    private String productSku;

    @JsonProperty("product_title")
    private String productTitle;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("price_unit")
    private double priceUnit;

    @JsonProperty("total_price")
    private double totalPrice;

    @JsonProperty("shipping_price")
    private double shippingPrice;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("category_label")
    private String categoryLabel;

    @JsonProperty("order_line_state")
    private String orderLineState;

    @JsonProperty("commission_fee")
    private double commissionFee;

    @JsonProperty("total_commission")
    private double totalCommission;

    public EmpikOrderLine() {
    }

    public String getOrderLineId() {
        return orderLineId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public String getOfferSku() {
        return offerSku;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceUnit() {
        return priceUnit;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getShippingPrice() {
        return shippingPrice;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getOrderLineState() {
        return orderLineState;
    }

    public double getCommissionFee() {
        return commissionFee;
    }

    public double getTotalCommission() {
        return totalCommission;
    }
}
