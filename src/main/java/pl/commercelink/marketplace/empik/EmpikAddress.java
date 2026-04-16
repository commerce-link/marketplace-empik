package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikAddress {

    @JsonProperty("city")
    private String city;

    @JsonProperty("company")
    private String company;

    @JsonProperty("country")
    private String country;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("street_1")
    private String street1;

    @JsonProperty("zip_code")
    private String zipCode;

    @JsonProperty("additional_info")
    private String additionalInfo;

    public EmpikAddress() {
    }

    public String getFullName() {
        if (firstname == null && lastname == null) return null;
        return ((firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "")).trim();
    }

    public String getCity() {
        return city;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry() {
        return country;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet1() {
        return street1;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
