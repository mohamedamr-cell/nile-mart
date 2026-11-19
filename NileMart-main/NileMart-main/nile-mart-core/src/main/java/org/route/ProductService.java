package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor

public class ProductService {

    private final ProductRepository repository;
    private final PricingService pricingService;


    public List<Product> findAll() {
        return List.copyOf(repository.findAll());
    }

    public Product findBySku(String sku) {
        if (sku == null || sku.isBlank()) {
        throw new StoreException("SKU is required.");
        }
        return repository.findBySku(sku.trim())
                .filter(Product::isActive)
                .orElseThrow(() -> new StoreException("Product " + sku + "was not found or is inactive."));
    }
    public BigDecimal getPrice(String sku, int qty) {
        Product product = repository.findBySku(sku)
                .filter(Product::isActive)
                .orElseThrow(() -> new RuntimeException("Product isn't available."));
        return pricingService.getPrice(product, qty);
    }

    public void validateStock(String sku, int qty) {
        reduceStockAtomically(Map.of(sku, qty));
    }

    public void validateStock(Map<String, Integer> requestedQuantities) {
        Objects.requireNonNull(requestedQuantities, "Cart lines cannot be null.");
        synchronized (repository) {
            requestedQuantities.forEach(this::validateStockOneLine);

            requestedQuantities.forEach((sku, qty) -> {
                Product product = findBySku(sku);
                product.setStockQuantity(product.getStockQuantity() - qty);
            });
        }
    }

    public void reduceStockAtomically(Map<String, Integer> requestedQuantities) {
        Objects.requireNonNull(requestedQuantities, "Cart lines cannot be null.");
        synchronized (repository)  {
            requestedQuantities.forEach(this::validateStockOneLine);

            requestedQuantities.forEach((sku, qty) -> {
                Product product = findBySku(sku);
                product.setStockQuantity(product.getStockQuantity() -qty);
            });
        }
    }
    private void validateStockOneLine(String sku, Integer qty) {
        if (qty == null || qty <= 0) {
            throw new StoreException("Quantity for " + sku + " Must be greater than Zero.");
        }
        Product product = findBySku(sku);
        if (product.getStockQuantity() < qty) {
            throw new StoreException("Insufficient stock for " + product.getName()
                    + ". Requested " + qty + ", available"
                    + product.getStockQuantity() + ".");
        }
    }

}
