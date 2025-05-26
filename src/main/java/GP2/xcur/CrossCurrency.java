package GP2.xcur;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CrossCurrency {

    public static enum Currencies {
        CHF("chf", "#,##0.00 [$CHF-gsw-CH]"),
        EUR("eur", "[$EUR-x-euro2] #,##0.00_);([$EUR-x-euro2] #,##0.00)"),
        GBP("gbp", "[$GBP-en-GB]#,##0.00"),
        INR("inr", "_([$INR] * #,##0.00_);_([$INR] * (#,##0.00);_([$INR] * \"-\"??_);_(@_)"),
        JPY("jpy", "[$JPY-ja-JP]#,##0.00"),
        MAD("mad", "[$MAD] #,##0.00_);([$MAD] #,##0.00)"),
        REN("ren", "[$REN-zh-CN]#,##0.00"),
        UAE("uae", "[$AED] #,##0.00"),
        USD("usd", "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)");

        public static final int SIZE = values().length;
        private final String currency;
        public final String format;

        private static final Map<String, Currencies> BY_CURRENCY_MAP = new HashMap<>();

        static {
            for (Currencies currency : values()) {
                BY_CURRENCY_MAP.put(currency.currency, currency);
            }
        }

        private Currencies(String currency, String format) {
            this.currency = currency;
            this.format = format;
        }

        public static Currencies byCurrency(String currency) {
            return BY_CURRENCY_MAP.get(currency);
        }
    }

    public enum XCurrencyType {
        XCurrency;
    }

    public interface CurrencyProperties {
        String CURRENCY = ":currency";
        String FORMAT = ":format";
        String X_CURRENCY = ":xCurrency";
        String RATE = ":rate";
    }

    public static enum XCurrencyProperties {
        CURRENCY("currency"),
        FORMAT("format"),
        X_CURRENCY("xCurrency"),
        RATE("rate");

        public static final int SIZE = values().length;
        private final String value;

        private static final Map<String, XCurrencyProperties> BY_VALUE_MAP = new HashMap<>();

        static {
            for (XCurrencyProperties property : values()) {
                BY_VALUE_MAP.put(property.value, property);
            }
        }

        private XCurrencyProperties(String value) {
            this.value = value;
        }

        public static XCurrencyProperties byValue(String value) {
            return BY_VALUE_MAP.get(value);
        }
    }

    public Double rate = 1.0d;
    public Currencies currency;
    public Currencies xCurrency;

    public CrossCurrency(Currencies currency, Currencies xCurrency, Double rate) {
        this.rate = rate;
        this.currency = currency;
        this.xCurrency = xCurrency;
    }

    public CrossCurrency(EnumMap<CrossCurrency.XCurrencyProperties, String> xProperties) {
        for (Map.Entry<CrossCurrency.XCurrencyProperties, String> entry : xProperties.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case CURRENCY:
                    this.currency = Currencies.byCurrency(value);
                    break;
                case X_CURRENCY:
                    this.xCurrency = Currencies.byCurrency(value);
                    break;
                case RATE:
                    this.rate = Double.valueOf(value);
                    break;
                default:
                    // Handle unknown properties if necessary
                    break;
            }
        }
    }

    // Getters for the private fields can be added here if needed
}