package org.route;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShoppingCart {

    private final ProductService productService;
    private final int maxQuantityPerProduct ;
    private final Map<String, Integer> lines = new LinkedHashMap<>();

    @Getter private int cartNumber;
    @Getter private Customer customer;

    public ShoppingCart (ProductService productService, @Value("${store.max-quantity-per-product}")int maxQuantityPerProduct) {

        this.productService = productService;
        if (maxQuantityPerProduct <= 0) {
            throw new StoreException("Maximum quantity must be greater than Zero.");
        }
        this.maxQuantityPerProduct = maxQuantityPerProduct;
    }

    public void initialize(int cartNumber, Customer customer ) {

        if (this.cartNumber != 0 || !lines.isEmpty()) {
            throw new StoreException("Cart Number must be positive.");
        }
        if (customer == null) {
            throw new StoreException("Cart Customer is required.");
        }
        this.cartNumber = cartNumber;
        this.customer = customer;
    }

    public Map<String,Integer> getLines() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(lines));
    }

    public void addItem(String sku,int qty) {
        ensureInitialized();
        if (qty <= 0) {
            throw new StoreException("Quantity must be greater than Zero.");
        }
        Product product = productService.findBySku(sku);
        if (product.getStockQuantity() <= 0 ) {
            throw new StoreException(product.getName() + "is out of stock and can not be added to cart.");
        }
        int currentQty = lines.getOrDefault(sku,0);
        int nextQty;
        try {
            nextQty = Math.addExact(currentQty, qty);
        } catch (ArithmeticException ex) {
            throw new StoreException("Requested quantity is too large.", ex);
        }

        if (nextQty > maxQuantityPerProduct) {
            throw new StoreException("Maximum quantity per product is " + maxQuantityPerProduct + ". Your cart would contain " + nextQty + ".");
        }
        if (nextQty > product.getStockQuantity()) {
            throw new StoreException("Insufficient stock for " + product.getName()
                    + ". Requested total: " + nextQty
                    + ". available: " + product.getStockQuantity() + ".");
        }
        lines.put(sku, nextQty);
    }
    private void  ensureInitialized() {
        if (cartNumber <= 0 || customer == null) {
            throw new IllegalStateException("Cart has not be initialized.");
        }
    }

    public BigDecimal subTotal() {
        ensureInitialized();
        return lines.entrySet().stream()
                .map(entry -> productService.getPrice(entry.getKey(), entry.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    public boolean isEmpty() {
        return lines.isEmpty();
    }
    public void clear() {
        lines.clear();
    }
}
