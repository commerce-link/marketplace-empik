# Marketplace Empik

[EmpikPlace](https://www.empik.com/empikplace) marketplace integration for the CommerceLink platform. This library implements the [marketplace-api](../marketplace-api) provider interface, enabling order import, offer export, and order lifecycle updates through the EmpikPlace marketplace API.

## Provider Discovery

This library registers `EmpikMarketplaceProviderDescriptor` via `ServiceLoader` for automatic discovery by the main application. See the [provider-api README](https://github.com/commerce-link/provider-api) for registration details.
