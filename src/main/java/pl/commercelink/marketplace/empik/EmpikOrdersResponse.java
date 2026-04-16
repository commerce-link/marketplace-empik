package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class EmpikOrdersResponse {

    @JsonProperty("orders")
    private List<EmpikOrder> orders;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    public EmpikOrdersResponse() {
    }

    public List<EmpikOrder> getOrders() {
        return orders;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public boolean hasNextPage() {
        return nextPageToken != null && !nextPageToken.isBlank();
    }
}
