package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikOrder {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("commercial_id")
    private String commercialId;

    @JsonProperty("order_state")
    private String orderState;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("currency_iso_code")
    private String currencyIsoCode;

    @JsonProperty("total_price")
    private double totalPrice;

    @JsonProperty("shipping_price")
    private double shippingPrice;

    @JsonProperty("total_commission")
    private double totalCommission;

    @JsonProperty("payment_type")
    private String paymentType;

    @JsonProperty("payment_workflow")
    private String paymentWorkflow;

    @JsonProperty("transaction_number")
    private String transactionNumber;

    @JsonProperty("customer")
    private EmpikCustomer customer;

    @JsonProperty("order_lines")
    private List<EmpikOrderLine> orderLines;

    public EmpikOrder() {
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCommercialId() {
        return commercialId;
    }

    public String getOrderState() {
        return orderState;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getShippingPrice() {
        return shippingPrice;
    }

    public double getTotalCommission() {
        return totalCommission;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getPaymentWorkflow() {
        return paymentWorkflow;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public EmpikCustomer getCustomer() {
        return customer;
    }

    public List<EmpikOrderLine> getOrderLines() {
        return orderLines;
    }
}
