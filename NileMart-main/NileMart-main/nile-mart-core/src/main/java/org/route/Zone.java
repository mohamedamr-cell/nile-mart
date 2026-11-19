package org.route;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Zone {
    GREATER_CAIRO(new BigDecimal("50.00"), true),
    ALEX_DELTA(new BigDecimal("65.00"), true),
    CANAL(new BigDecimal("70.00"), true),
    UPPER_EGYPT( new BigDecimal("85.00"), true),
    REMOTE( new BigDecimal("120.00"), false);

    private final BigDecimal fee;
    private final boolean freeShippingEligible;


}
