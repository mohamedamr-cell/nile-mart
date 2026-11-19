package org.route;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingQuoteService {

    private final ShippingCalculator zoneShippingCalculator;
    private  final ShippingCalculator freeShippingCalculator;
    private  final BigDecimal freeShippingThreshold;

    public ShippingQuoteService (
        ShippingCalculator zoneShippingCalculator,
                @Qualifier("freeShipping") ShippingCalculator freeShippingCalculator,
                        @Value("${store.free-shpping-threshold}") BigDecimal freeShippingThreshold)
     {
         this.zoneShippingCalculator = zoneShippingCalculator;
         this.freeShippingThreshold= freeShippingThreshold;
         this.freeShippingCalculator = freeShippingCalculator;
     }

    public BigDecimal quote(Governorate governorate, BigDecimal subtotal ) {
        if (governorate == null || subtotal == null ) {
            throw new StoreException("Governorate and subtotal are required to calculate shipping.");
        }
        boolean reachesThreshold = subtotal.compareTo(freeShippingThreshold) >= 0;
        boolean zoneAllowsFreeShipping = governorate.getZone().isFreeShippingEligible();

        if (reachesThreshold && zoneAllowsFreeShipping) {
            return freeShippingCalculator.calculate(governorate).setScale(2);
        }
        return zoneShippingCalculator.calculate(governorate).setScale(2);
    }
}
