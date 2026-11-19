package org.route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Order(String orderNumber,
                    Customer customer,
                    Governorate governorate,
                    List<OrderLine> lines,
                    BigDecimal subtotal,
                    BigDecimal vat,
                    BigDecimal shippingFee,
                    BigDecimal total    )
    {
        public Order {
            if (orderNumber == null || orderNumber.isBlank()) throw new StoreException("Order number is required.");
            if (customer == null || governorate == null) throw new StoreException("Customer and governorate are required.");
            lines = List.copyOf(lines);
            subtotal = money(subtotal);
            vat = money(vat);
            shippingFee = money(shippingFee);
            total = money(total);
        }

        private static BigDecimal money(BigDecimal amount) {
            if (amount == null ) throw new StoreException("Order amount cannot be null.");
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
}
