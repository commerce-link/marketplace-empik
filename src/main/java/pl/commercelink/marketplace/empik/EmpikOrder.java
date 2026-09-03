package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.PickupPoint;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    @JsonProperty("customer_notification_email")
    private String customerNotificationEmail;

    @JsonProperty("customer")
    private EmpikCustomer customer;

    @JsonProperty("order_lines")
    private List<EmpikOrderLine> orderLines;

    @JsonProperty("order_additional_fields")
    private List<EmpikOrderAdditionalField> orderAdditionalFields;
    @JsonProperty("shipping_pudo_id")
    private String shippingPudoId;
    @JsonProperty("shipping_carrier_code")
    private String shippingCarrierCode;

    @JsonProperty("shipping_deadline")
    private String shippingDeadline;

    public String getShippingDeadline() {
        return shippingDeadline;
    }

    public void setShippingDeadline(String shippingDeadline) {
        this.shippingDeadline = shippingDeadline;
    }

    public LocalDate toEstimatedShippingAt() {
        if (shippingDeadline == null || shippingDeadline.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(shippingDeadline.trim().substring(0, 10));
        } catch (DateTimeParseException | IndexOutOfBoundsException ignored) {
            return null;
        }
    }

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

    public String getCustomerNotificationEmail() {
        return customerNotificationEmail;
    }

    public EmpikCustomer getCustomer() {
        return customer;
    }

    public List<EmpikOrderLine> getOrderLines() {
        return orderLines;
    }

    public List<EmpikOrderAdditionalField> getOrderAdditionalFields() {
        return orderAdditionalFields;
    }

    public String getShippingCarrierCode() {
        return shippingCarrierCode;
    }

    public PickupPoint toPickupPoint() {
        return shippingPudoId == null || shippingPudoId.isBlank() ? null : new PickupPoint(shippingPudoId);
    }

    public String findAdditionalField(String code) {
        if (orderAdditionalFields == null || code == null) return null;
        return orderAdditionalFields.stream()
                .filter(f -> code.equalsIgnoreCase(f.getCode()))
                .map(EmpikOrderAdditionalField::getValue)
                .findFirst()
                .orElse(null);
    }
}
