package tournament_trail.demo.entities;


public enum CurrencyCode {

    ALL("Albanian Lek"),
    AMD("Armenian Dram"),
    AZN("Azerbaijani Manat"),
    BAM("Bosnia and Herzegovina Convertible Mark"),
    BYN("Belarusian Ruble"),
    CHF("Swiss Franc"),
    CZK("Czech Koruna"),
    DKK("Danish Krone"),
    EUR("Euro"),
    GBP("British Pound Sterling"),
    GEL("Georgian Lari"),
    HUF("Hungarian Forint"),
    ISK("Icelandic Króna"),
    KZT("Kazakhstani Tenge"),
    MDL("Moldovan Leu"),
    MKD("Macedonian Denar"),
    NOK("Norwegian Krone"),
    PLN("Polish Złoty"),
    RON("Romanian Leu"),
    RSD("Serbian Dinar"),
    RUB("Russian Ruble"),
    SEK("Swedish Krona"),
    TRY("Turkish Lira"),
    UAH("Ukrainian Hryvnia"),
    USD("United States dollar");

    private final String displayName;

    CurrencyCode(String displayName) {
        this.displayName = displayName;
    }


    public String getDisplayName() {
        return displayName;
    }



}