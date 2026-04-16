# Marketplace Empik

[EmpikPlace](https://www.empik.com/empikplace) marketplace integration for the CommerceLink platform. This library implements the [marketplace-api](../marketplace-api) provider interface, enabling order import, offer export, and order lifecycle updates through the EmpikPlace marketplace API.

## Mirakl Platform

EmpikPlace is built on the [Mirakl](https://www.mirakl.com/) marketplace platform. This integration uses Mirakl's standard Seller API conventions (workflow codes like `OR11`, `OR21`, `OF01`, endpoints like `/api/orders`, `/api/offers`, `/api/orders/ship`, `/api/orders/tracking`). Authentication uses a simple API key passed in the `Authorization` header.

Because the API surface is Mirakl-standard, this module can serve as a baseline for integrating other Mirakl-powered marketplaces — for example Carrefour, Decathlon, FNAC, Leroy Merlin, and similar partners. A new integration typically only needs to change:

- Provider name, display name and `ServiceLoader` registration
- The base API URL in descriptor metadata
- Any marketplace-specific carrier codes, category mappings, or payment type names
- Optional fields/attributes the specific marketplace requires or exposes

If multiple Mirakl-based integrations are introduced, consider extracting a shared `marketplace-mirakl` module with the common request/response DTOs and API client logic, keeping partner-specific modules as thin configuration layers.

## Provider Discovery

This library registers `EmpikMarketplaceProviderDescriptor` via `ServiceLoader` for automatic discovery by the main application. See the [provider-api README](https://github.com/commerce-link/provider-api) for registration details.
