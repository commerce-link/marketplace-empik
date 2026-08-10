package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.MarketplaceProvider;
import pl.commercelink.marketplace.api.MarketplaceProviderDescriptor;
import pl.commercelink.provider.api.ProviderField;
import pl.commercelink.rest.client.RestApi;

import java.util.List;
import java.util.Map;

public class EmpikMarketplaceProviderDescriptor implements MarketplaceProviderDescriptor {

    @Override
    public String name() {
        return "Empik";
    }

    @Override
    public String displayName() {
        return "Empik";
    }

    @Override
    public List<ProviderField> configurationFields() {
        return List.of(
                new ProviderField("apiKey", "API Key", ProviderField.FieldType.PASSWORD, true, "******")
        );
    }

    @Override
    public MarketplaceProvider create(Map<String, String> configuration) {
        throw new UnsupportedOperationException("Use create(configuration, context) instead");
    }

    @Override
    public MarketplaceProvider create(Map<String, String> configuration, Map<String, Object> context) {
        String apiKey = configuration.get("apiKey");
        String apiUrl = metadata().get("apiUrl");
        RestApi restApi = RestApi.builder(apiUrl)
                .defaultHeader("Authorization", apiKey)
                .build();
        return new EmpikMarketplaceProvider(restApi);
    }

    @Override
    public Map<String, String> metadata() {
        return Map.of(
                "apiUrl", "https://marketplace.empik.com"
        );
    }
}
