package org.route;

import java.math.BigDecimal;

public record OrderLine(String sku, String productName, int quantity, BigDecimal linePrice) {

    public OrderLine {
            if (sku == null || sku.isBlank()) throw new StoreException("Order line SKU is required.");
            if (productName == null || productName.isBlank()) throw new StoreException("Order line product name is required.");
            if (quantity <= 0) throw new StoreException("Order line quantity must be positive.");
            if (linePrice == null || linePrice.signum() < 0) throw new StoreException("Order line price is invalid.");
            linePrice = linePrice.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
