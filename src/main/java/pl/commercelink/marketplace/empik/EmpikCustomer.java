package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.commercelink.marketplace.api.MarketplaceCustomer;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikCustomer {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("billing_address")
    private EmpikAddress billingAddress;

    @JsonProperty("shipping_address")
    private EmpikAddress shippingAddress;

    public EmpikCustomer() {
    }

    public MarketplaceCustomer toMarketplaceCustomer() {
        String fullName = (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
        fullName = fullName.trim();

        String company = billingAddress != null ? billingAddress.getCompany() : null;
        MarketplaceCustomer.CustomerType customerType = (company != null && !company.isBlank())
                ? MarketplaceCustomer.CustomerType.COMPANY
                : MarketplaceCustomer.CustomerType.INDIVIDUAL;

        return new MarketplaceCustomer(
                customerType,
                fullName,
                company,
                email,
                billingAddress != null ? billingAddress.getPhone() : null,
                null,
                toBillingAddress(),
                toShippingAddress()
        );
    }

    private MarketplaceCustomer.Address toBillingAddress() {
        if (billingAddress == null) return null;
        return new MarketplaceCustomer.Address(
                billingAddress.getFullName(),
                billingAddress.getPhone(),
                billingAddress.getStreet1(),
                billingAddress.getZipCode(),
                billingAddress.getCity(),
                billingAddress.getCountry()
        );
    }

    private MarketplaceCustomer.Address toShippingAddress() {
        if (shippingAddress == null) return null;
        return new MarketplaceCustomer.Address(
                shippingAddress.getFullName(),
                shippingAddress.getPhone(),
                shippingAddress.getStreet1(),
                shippingAddress.getZipCode(),
                shippingAddress.getCity(),
                shippingAddress.getCountry()
        );
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public EmpikAddress getBillingAddress() {
        return billingAddress;
    }

    public EmpikAddress getShippingAddress() {
        return shippingAddress;
    }
}
