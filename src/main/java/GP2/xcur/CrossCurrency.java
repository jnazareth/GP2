package GP2.xcur;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CrossCurrency {
    public static enum Currencies {
        chf("chf", "#,##0.00 [$CHF-gsw-CH]"),
        eur("eur", "[$EUR-x-euro2] #,##0.00_);([$EUR-x-euro2] #,##0.00)"),
        gbp("gbp", "[$GBP-en-GB]#,##0.00"),
        inr("inr", "_([$INR] * #,##0.00_);_([$INR] * (#,##0.00);_([$INR] * \"-\"??_);_(@_)"), //"[$INR] #,##0.00"
        jpy("jpy", "[$JPY-ja-JP]#,##0.00"),
        mad("mad", "[$MAD] #,##0.00_);([$MAD] #,##0.00)"),
        ren("ren", "[$REN-zh-CN]#,##0.00"),
        uae("uae", "[$AED] #,##0.00"),
        usd("usd", "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)");

        public static final int size;
        private final String currency;
        public final String format;

        private static final Map<String, Currencies> byCurrencyMap = new HashMap<String, Currencies>();

        static { size = values().length; }
        static {
            for (Currencies e : values()) {
                byCurrencyMap.put(e.currency, e);
            }
        }
        private Currencies(String c, String f) {
            this.currency = c;
            this.format = f;
        }
        public static Currencies byCurrency(String c) {
            return byCurrencyMap.get(c);
        }
    }

    public enum XCurrencyType {
        XCurrency ;
    }

    public interface CurrencyProperties {
        final String Currency	= ":currency" ;
        final String Format	    = ":format" ;
        final String XCurrency  = ":xCurrency" ;
        final String Rate		= ":rate" ;
    }

    public static enum XCurrencyProperties {
        Currency("currency"),
        Format("format"),
        XCurrency("xCurrency"),
        Rate("rate");

        public static final int size;
        private final String value;

        private static final Map<String, XCurrencyProperties> byValueMap = new HashMap<>();

        static { size = values().length; }
        static {
            for (XCurrencyProperties e : values()) {
                byValueMap.put(e.value, e);
            }
        }
        private XCurrencyProperties(String v) {
            this.value = v;
        }
        public static XCurrencyProperties byValue(String v) {
            return byValueMap.get(v);
        }
    }

    public Double m_rate = 1.0d ;
    public Currencies m_currency;
    public Currencies m_xcurrency;

    public CrossCurrency(Currencies c, Currencies b, Double r) {
        m_rate = r;
        m_currency = c;
        m_xcurrency = b;
    }

    public CrossCurrency (EnumMap<CrossCurrency.XCurrencyProperties, String> xProperties) {
        for (CrossCurrency.XCurrencyProperties p : xProperties.keySet()) {
            //System.out.println(p + ":::" + xProperties.get(p));
            String xcp = xProperties.get(p);
            if (p.compareTo(CrossCurrency.XCurrencyProperties.Currency) == 0)
                m_currency = Currencies.byCurrency(xcp);
            else if (p.compareTo(CrossCurrency.XCurrencyProperties.XCurrency) == 0)
            m_xcurrency = Currencies.byCurrency(xcp);
            else if (p.compareTo(CrossCurrency.XCurrencyProperties.Rate) == 0)
                m_rate = Double.valueOf(xcp) ;
        }
    }
}
