package GP2.person;

import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;

import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.TransactionType.TType;
import GP2.utils.Constants;
import GP2.xcur.CrossCurrency;

public class GPAction {
    private static final String ITEM_SEPARATOR = ",";
    private static final String ITEM_SEPARATOR2 = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final String ID_LR = "(";
    private static final String ID_RR = ")";

    public static class PGState {
        public enum EntryState {
            Add("*"), Enable("+"), Disable("-"), NoOp("0");

            private final String value;
            private static final Map<String, EntryState> byValueMap = new HashMap<>();

            static {
                for (EntryState e : values()) {
                    byValueMap.put(e.value, e);
                }
            }

            EntryState(String value) {
                this.value = value;
            }

            public static EntryState byValue(String v) {
                return byValueMap.get(v);
            }

            @Override
            public String toString() {
                return value;
            }
        }

        private EntryState state;

        public PGState(EntryState s) {
            state = s;
        }

        public void add() { state = EntryState.Add; }
        public void enable() { state = EntryState.Enable; }
        public void disable() { state = EntryState.Disable; }
        public void doNothing() { state = EntryState.NoOp; }

        public boolean isActive() {
            return state != EntryState.Disable;
        }

        public boolean equals(EntryState es) {
            return this.state == es;
        }

        @Override
        public String toString() {
            return state.toString();
        }
    }

    public static class PGType {
        public interface EntryTypes {
            String Self = ":self";
            String Group = ":group";
        }

        public enum EntryType {
            Self("self"), Group("group");

            private final String value;
            private static final Map<String, EntryType> byValueMap = new HashMap<>();

            static {
                for (EntryType e : values()) {
                    byValueMap.put(e.value, e);
                }
            }

            EntryType(String v) {
                this.value = v;
            }

            public static EntryType byValue(String v) {
                return byValueMap.get(v);
            }
        }
    }

    public static class TransactionType {
        public enum TType {
            Clearing("$"), Skip("#"), Normal("");

            private final String value;
            private static final Map<String, TType> byValueMap = new HashMap<>();

            static {
                for (TType e : values()) {
                    byValueMap.put(e.value, e);
                }
            }

            TType(String v) {
                this.value = v;
            }

            public static TType byValue(String v) {
                return byValueMap.get(v);
            }
        }
    }

    private static EntryState getAction(int lR, String sA) {
        int idS = sA.indexOf(Constants._ID_SEPARATOR);
        if (idS != -1) {
            String sAct = sA.substring(lR + 1, idS).trim();
            return EntryState.byValue(sAct);
        } else {
            System.err.println("Action not specified: " + sA);
            return null;
        }
    }

    private static CrossCurrency.XCurrencyProperties getCurrencyProperty(int rR, String sA) {
        int idS = sA.indexOf(ID_RR);
        if (idS != -1) {
            String sAct = sA.substring(rR + 1, idS).trim();
            return CrossCurrency.XCurrencyProperties.byValue(sAct);
        } else {
            System.err.println("Property not specified: " + sA);
            return null;
        }
    }

    private static boolean isXCurrencyProperties(String sAct) {
        return sAct.contains(CrossCurrency.CurrencyProperties.CURRENCY.toString()) ||
               sAct.contains(CrossCurrency.CurrencyProperties.FORMAT.toString()) ||
               sAct.contains(CrossCurrency.CurrencyProperties.X_CURRENCY.toString()) ||
               sAct.contains(CrossCurrency.CurrencyProperties.RATE.toString());
    }

    public static HashMap<String, LinkedHashSet<ActionItem>> breakdownActions(String sAction, String sGroupName) {
        HashMap<String, LinkedHashSet<ActionItem>> actionMap = new HashMap<>();
        LinkedHashSet<ActionItem> actions = new LinkedHashSet<>();

        LinkedHashSet<ActionItem> groupActions = new LinkedHashSet<>();
        LinkedHashSet<ActionItem> selfActions = new LinkedHashSet<>();
        LinkedHashSet<ActionItem> clearingActions = new LinkedHashSet<>();
        LinkedHashSet<ActionItem> skipActions = new LinkedHashSet<>();
        EnumMap<CrossCurrency.XCurrencyProperties, String> currencyProperties = new EnumMap<>(CrossCurrency.XCurrencyProperties.class);
        HashMap<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> currencyMap = new HashMap<>();
        LinkedHashSet<ActionItem> exchangeActions = new LinkedHashSet<>();
        EntryState selfState, groupState;
        String groupName = null;

        String[] pieces = sAction.split(Constants._READ_SEPARATOR);
        for (String piece : pieces) {
            processTransactionType(piece, clearingActions, skipActions);
            processEntryType(piece, groupActions, selfActions, currencyProperties);
        }

        actions = consolidateActions(skipActions, clearingActions, groupActions, selfActions, currencyProperties, groupName, exchangeActions, currencyMap);
        actionMap.put(sGroupName, actions);
        return actionMap;
    }

    private static void processTransactionType(String piece, LinkedHashSet<ActionItem> clearingActions, LinkedHashSet<ActionItem> skipActions) {
        TType transactionType = TransactionType.TType.byValue(piece);
        if (transactionType != null) {
            if (transactionType == TransactionType.TType.Clearing) {
                clearingActions.add(new ActionItem(null, TType.Clearing, null, EntryState.NoOp, null, null));
            } else if (transactionType == TransactionType.TType.Skip) {
                skipActions.add(new ActionItem(null, TType.Skip, null, EntryState.NoOp, null, null));
            }
        }
    }

    private static void processEntryType(String piece, LinkedHashSet<ActionItem> groupActions, LinkedHashSet<ActionItem> selfActions, EnumMap<CrossCurrency.XCurrencyProperties, String> currencyProperties) {
        int lR = piece.indexOf(ID_LR);
        int rR = piece.indexOf(ID_RR);
        if (lR != -1 && rR != -1) {
            if (piece.contains(PGType.EntryTypes.Self)) {
                String name = piece.substring(0, lR).trim();
                EntryState selfState = getAction(lR, piece);
                selfActions.add(new ActionItem(EntryType.Self, null, name, selfState, null, null));
            } else if (piece.contains(PGType.EntryTypes.Group)) {
                String groupName = piece.substring(0, lR).trim();
                EntryState groupState = getAction(lR, piece);
                groupActions.add(new ActionItem(EntryType.Group, null, groupName, groupState, null, null));
            } else if (isXCurrencyProperties(piece)) {
                rR = piece.indexOf(Constants._AMT_INDICATOR);
                String currencyProperty = piece.substring(lR + 1, rR);
                CrossCurrency.XCurrencyProperties currencyProp = getCurrencyProperty(rR, piece);
                currencyProperties.put(currencyProp, currencyProperty);
            }
        }
    }

    private static LinkedHashSet<ActionItem> consolidateActions(LinkedHashSet<ActionItem> skipActions, LinkedHashSet<ActionItem> clearingActions, LinkedHashSet<ActionItem> groupActions, LinkedHashSet<ActionItem> selfActions, EnumMap<CrossCurrency.XCurrencyProperties, String> currencyProperties, String groupName, LinkedHashSet<ActionItem> exchangeActions, HashMap<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> currencyMap) {
        LinkedHashSet<ActionItem> actions = new LinkedHashSet<>();
        if (!skipActions.isEmpty()) actions = new LinkedHashSet<>(skipActions);
        if (!clearingActions.isEmpty()) actions = new LinkedHashSet<>(clearingActions);
        if (!groupActions.isEmpty()) {
            actions = new LinkedHashSet<>(groupActions);
            for (ActionItem actionItem : groupActions) {
                if (!actionItem.name.isEmpty()) groupName = actionItem.name;
            }
        }
        if (!selfActions.isEmpty()) {
            if (actions == null) actions = new LinkedHashSet<>(selfActions.size());
            actions.addAll(selfActions);
        }
        if (!currencyProperties.isEmpty()) {
            if (groupName == null) {
                System.err.println("Group not specified: " + CrossCurrency.XCurrencyType.XCurrency);
            } else {
                currencyMap.put(groupName, currencyProperties);
                if (actions == null) actions = new LinkedHashSet<>(currencyProperties.size());
                exchangeActions.add(new ActionItem(null, null, null, null, groupName, currencyMap));
                actions.addAll(exchangeActions);
            }
        }
        return actions;
    }

    public static class ActionItem {
        public EntryType pgtype = null;
        public TType ttype = null;
        public String name = null;
        public EntryState state = null;
        public CrossCurrency.XCurrencyType xcType = null;
        public HashMap<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> xCurrency = new HashMap<>();

        public ActionItem() {}

        public ActionItem(EntryType t, TType tt, String n, EntryState s, String g, HashMap<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> c) {
            if (t != null) pgtype = t;
            if (tt != null) ttype = tt;
            if (n != null && !n.isEmpty()) name = n;
            if (s != null) state = s;
            if (g != null && !g.isEmpty() && c != null) {
                xcType = CrossCurrency.XCurrencyType.XCurrency;
                xCurrency = c;
            }
        }

        @Override
        public String toString() {
            return "ActionItem [pgtype=" + pgtype + ", ttype=" + ttype + ", name=" + name + ", state=" + state + ", xcType=" + xcType + "]";
        }
    }
}