package org.route;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public enum Governorate {
    CAIRO("Cairo", Zone.GREATER_CAIRO),
    GIZA("Giza", Zone.GREATER_CAIRO),
    QALYUBIA("Qalyubia", Zone.GREATER_CAIRO),

    ALEXANDRIA("Alexandria", Zone.ALEX_DELTA),
    BEHEIRA("Beheira", Zone.ALEX_DELTA),
    KAFR_EL_SHEIKH("Kafr El Sheikh", Zone.ALEX_DELTA),
    DAKAHLIA("Dakahlia", Zone.ALEX_DELTA),
    DAMIETTA("Damietta", Zone.ALEX_DELTA),
    GHARBIA("Gharbia", Zone.ALEX_DELTA),
    MONUFIA("Monufia", Zone.ALEX_DELTA),
    SHARQIA("Sharqia", Zone.ALEX_DELTA),

    PORT_SAID("Port Said", Zone.CANAL),
    ISMAILIA("Ismailia", Zone.CANAL),
    SUEZ("Suez", Zone.CANAL),

    FAIYUM("Faiyum", Zone.UPPER_EGYPT),
    BENI_SUEF("Beni Suef", Zone.UPPER_EGYPT),
    MINYA("Minya", Zone.UPPER_EGYPT),
    ASYUT("Asyut", Zone.UPPER_EGYPT),
    SOHAG("Sohag", Zone.UPPER_EGYPT),
    QENA("Qena", Zone.UPPER_EGYPT),
    LUXOR("Luxor", Zone.UPPER_EGYPT),
    ASWAN("Aswan", Zone.UPPER_EGYPT),

    MATROUH("Matrouh", Zone.REMOTE),
    NORTH_SINAI("North Sinai", Zone.REMOTE),
    SOUTH_SINAI("South Sinai", Zone.REMOTE),
    RED_SEA("Red Sea", Zone.REMOTE),
    NEW_VALLEY("New Valley", Zone.REMOTE);

    private final String displayName;
    private final Zone zone;

    public static Governorate fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new StoreException("Governorate is required.");
        }
        String normalized = normalize(input);
        return Arrays.stream(values())
                .filter(g -> normalize(g.name()).equals(normalized)
                || normalize(g.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new StoreException( "Unkown Governate: " + input + ". Please Enter a valid Governorate."));
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("\\s+", "");
    }
}
