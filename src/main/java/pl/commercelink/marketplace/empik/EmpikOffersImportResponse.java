package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikOffersImportResponse {

    @JsonProperty("import_id")
    private Long importId;

    public EmpikOffersImportResponse() {
    }

    public Long getImportId() {
        return importId;
    }
}
