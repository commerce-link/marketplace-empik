package pl.commercelink.marketplace.empik;

import pl.commercelink.marketplace.api.AliasedCarrier;

import java.util.List;

public enum EmpikCarrier implements AliasedCarrier {

    INPOST(List.of("Paczkomat", "Paczkomaty")),
    ORLEN(List.of("Orlen", "RUCH")),
    ZABKA(List.of("Zabka", "Żabka")),
    POCZTA_POLSKA(List.of("Poczta", "Pocztex")),
    DPD(List.of()),
    DHL(List.of()),
    UPS(List.of()),
    GLS(List.of()),
    FEDEX(List.of());

    private final List<String> aliases;

    EmpikCarrier(List<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public List<String> aliases() {
        return aliases;
    }

    static EmpikCarrier fromCode(String code) {
        return AliasedCarrier.deserialize(values(), code);
    }
}
