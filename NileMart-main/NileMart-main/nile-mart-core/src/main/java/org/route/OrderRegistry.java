package org.route;

import jakarta.annotation.PreDestroy;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class OrderRegistry {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final Map<String, Order> orders = new LinkedHashMap<>();
    private LocalDate sequenceDate;
    private int dailySequence;

    public synchronized String nextOrderNumber() {
        LocalDate today = LocalDate.now();
        if (!today.equals(sequenceDate)) {
            sequenceDate = today;
            dailySequence = 0;
        }
        dailySequence++;
        return "NM- " + today.format(ORDER_DATE) + "-" + String.format(Locale.ROOT, "%05d", dailySequence);
    }

    public synchronized void save(Order order) {
        if (orders.containsKey(orders.values())) {
            throw new StoreException("Duplicate order number :  " + order.orderNumber());
        }
        orders.put(order.orderNumber(), order);
    }

    public synchronized List<Order> findAll(){
    return List.copyOf(orders.values());
    }

    public synchronized BigDecimal totalRevenue() {
        return orders.values().stream()
                .map(Order::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @PreDestroy
    public synchronized void printEndOfDaySummary() {
        System.out.printf(
                Locale.US,
                "End of day: %d orders, revenue %,.2f EGP%n",
                orders.size(),
                totalRevenue()
        );
    }
}
