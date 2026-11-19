package org.route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class CheckoutService {
    @Service
    @RequiredArgsConstructor
    public class CheckoutService {
        private final ProductService productService;
        private final ShippingQuoteService shippingQuoteService;
        private final TaxCalculator taxCalculator;
        private final OrderRegistry orderRegistry;
        private final List<NotificationSender> notificationSenders;

        public Order placeOrder(ShoppingCart cart, Governorate governorate) {
            if (cart == null) {
                throw new StoreException("Shopping cart was not found.");
            }
            if (cart.isEmpty()) {
                throw new StoreException("Cannot check out an empty cart.");
            }
            if (governorate == null) {
                throw new StoreException("Please select a governorate.");
            }


            Map<String, Integer> cartLines = cart.getLines();


            productService.validateStock(cartLines);

            BigDecimal subtotal = cart.subTotal();
            BigDecimal vat = taxCalculator.calculate(subtotal);
            BigDecimal shippingFee = shippingQuoteService.quote(governorate, subtotal);
            BigDecimal total = subtotal.add(vat).add(shippingFee)
                    .setScale(2, RoundingMode.HALF_UP);

            List<OrderLine> orderLines = cartLines.entrySet().stream()
                    .map(entry -> {
                        Product product = productService.findBySku(entry.getKey());
                        BigDecimal linePrice = productService.getPrice(
                                entry.getKey(), entry.getValue()
                        );
                        return new OrderLine(
                                product.getSku(),
                                product.getName(),
                                entry.getValue(),
                                linePrice
                        );
                    })
                    .toList();


            productService.reduceStockAtomically(cartLines);

            String orderNumber = orderRegistry.nextOrderNumber();
            Order order = new Order(
                    orderNumber,
                    cart.getCustomer(),
                    governorate,
                    orderLines,
                    subtotal,
                    vat,
                    shippingFee,
                    total
            );

            orderRegistry.save(order);

            String confirmationMessage = "Order " + order.orderNumber()
                    + " confirmed. Total "
                    + String.format(Locale.US, "%,.2f", order.total())
                    + " EGP";

            notificationSenders.forEach(sender ->
                    sender.send(order.customer(), confirmationMessage)
            );

            cart.clear();
            return order;
        }
    }
}
