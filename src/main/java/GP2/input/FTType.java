package GP2.input;

import java.util.Map;
import java.util.HashMap;

public class FTType {
    // fr/to strings (search)
    public interface FromToTypes {
        final String Rem	       = "rem" ;
        final String All	       = "all" ;
        final String Sys	       = "sys" ;
        final String Percentage    = "%" ;
    }

    // index for input processing
    public enum FromToType {
        Remainder(FromToTypes.Rem,  "_rem"),
        All(FromToTypes.All,        "_all"),
        Individual("Indiv",     "_indiv"),
        Unknown("Unknown",      "_unknown");

        private final String value;
        private final String key;
        
        private static final Map<String, FromToType> byValueMap = new HashMap<>();
        private static final Map<FromToType, String> byKeyMap = new HashMap<>();
        private static final Map<String, String>  byStringMap = new HashMap<>();

        static {
            for (FromToType e : values()) {
                byValueMap.put(e.value, e);
                byKeyMap.put(e, e.key);
                byStringMap.put(e.value, e.key);
            }
        }

        private FromToType(String v, String k) {
            this.value = v;
            this.key = k;
        }
            
        public static FromToType byValue(String v) {
            return byValueMap.get(v);
        }

        public static String byKey(FromToType v) {
            return byKeyMap.get(v);
        }

        public static String byString(String v) {
            return byStringMap.get(v);
        }
    }    
}
