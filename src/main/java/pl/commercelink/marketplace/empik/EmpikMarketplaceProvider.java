package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.*;
import pl.commercelink.rest.client.RestApi;

import java.util.List;

class EmpikMarketplaceProvider implements MarketplaceProvider {

    private final EmpikOrdersImport ordersImport;
    private final EmpikOrderLifecycleEventHandler lifecycleHandler;
    private final EmpikOfferExport offerExport;

    EmpikMarketplaceProvider(RestApi restApi) {
        this.ordersImport = new EmpikOrdersImport(restApi);
        this.lifecycleHandler = new EmpikOrderLifecycleEventHandler(restApi);
        this.offerExport = new EmpikOfferExport(restApi);
    }

    @Override
    public List<MarketplaceOrder> fetchOrders() {
        return ordersImport.fetchOrders();
    }

    @Override
    public void exportOffers(List<MarketplaceOffer> toPublish, List<MarketplaceOffer> toRemove) {
        offerExport.export(toPublish, toRemove);
    }

    @Override
    public void updateOrderStatus(String externalOrderId, MarketplaceOrderStatus status) {
        lifecycleHandler.updateOrderStatus(externalOrderId, status);
    }

    @Override
    public void updateShipment(String externalOrderId, ShipmentUpdate update) {
        lifecycleHandler.updateShipment(externalOrderId, update);
    }

    @Override
    public void updateInvoice(String externalOrderId, InvoiceUpdate update) {
        lifecycleHandler.updateInvoice(externalOrderId, update);
    }
}
