package GP2.person;

import java.util.Map;

import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.PGType.EntryTypes;
import GP2.person.GPAction.TransactionType.TType;
import GP2.utils.Constants;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class GPAction {
    final static String _ITEM_SEPARATOR	    = "," ;
    final static String _ID_lR 		        = "(" ;
    final static String _ID_rR 		        = ")" ;

    public static class PGState {
        public enum EntryState {
            Add("*"),
            Enable("+"),
            Disable("-"),
            NoOp("0");

            public static final int size;
            private final String value;
            private static final Map<String, EntryState> byValueMap = new HashMap<>();

            static { size = values().length; }
            static {
                for (EntryState e : values()) {
                    byValueMap.put(e.value, e);            }
            }
            EntryState(String value) {
                this.value = value;
            }
            public static EntryState byValue(String v) {
                return byValueMap.get(v);
            }
			public String toString() {
				return value ;
			}
        }

        private EntryState state;

        public PGState(EntryState s) {
            state = s;
        }

        public void add()		    { state = EntryState.Add; }
        public void enable()		{ state = EntryState.Enable; }
        public void disable()	    { state = EntryState.Disable; }
        public void doNothing()	    { state = EntryState.NoOp; }

        public boolean getActive() {
            return ( (state == EntryState.Disable) ? false : true )  ;
        }
        public boolean equals (EntryState es) {
            return (this.state == es) ;
        }
		public String toString() {
			return state.toString() ;
		}
    }

    //-----------------------------------------------
    public static class PGType {
        public interface EntryTypes {
            final String Self	       = ":self" ;
            final String Group	       = ":group" ;
        }

        public static enum EntryType {
            Self("self"),
            Group("group");

            public static final int size;
            private final String value;

            private static final Map<String, EntryType> byValueMap = new HashMap<>();

            static { size = values().length; }
            static {
                for (EntryType e : values()) {
                    byValueMap.put(e.value, e);            }
            }
            private EntryType(String v) {
                this.value = v;
            }
            public static EntryType byValue(String v) {
                return byValueMap.get(v);
            }
        }
    }

    //-----------------------------------------------
    public static class TransactionType {
        public static enum TType {
            Clearing("$"),
            Skip("#");

            public static final int size;
            private final String value;

            private static final Map<String, TType> byValueMap = new HashMap<>();

            static { size = values().length; }
            static {
                for (TType e : values()) {
                    byValueMap.put(e.value, e);            }
            }
            private TType(String v) {
                this.value = v;
            }
            public static TType byValue(String v) {
                return byValueMap.get(v);
            }
        }
    }

    static EntryState getAction(int lR, String sA)
	{
		String sAct = "" ;
		int idS = 0 ;
		if ( ((idS = sA.indexOf(Constants._ID_SEPARATOR)) != -1) )
			sAct = sA.substring(lR+1, idS).trim() ;
		else
			System.err.println("Action not specified: " + sA);
		return EntryState.byValue(sAct) ;
	}


	public static HashMap<String, LinkedHashSet<ActionItem>> breakdownActions(String sAction, String sGroupName) {
		String sActionGroupName = Constants._DEFAULT_GROUP;
		HashMap<String, LinkedHashSet<ActionItem>> hmActions = new HashMap<String, LinkedHashSet<ActionItem>>();
		LinkedHashSet<ActionItem> hsActions = new LinkedHashSet<ActionItem>() ;

		LinkedHashSet<ActionItem> groupActions = new LinkedHashSet<ActionItem>();
		LinkedHashSet<ActionItem> selfActions = new LinkedHashSet<ActionItem>();
		LinkedHashSet<ActionItem> clearingActions = new LinkedHashSet<ActionItem>();
		LinkedHashSet<ActionItem> skipActions = new LinkedHashSet<ActionItem>();
		EntryState esSelf, esGroup;

		String[] pieces = sAction.split(_ITEM_SEPARATOR);
		for (String p : pieces) {
			String sActs = p ;

			TType t1 = TransactionType.TType.byValue(sActs);
			if ( (t1 != null) && (t1.compareTo(TransactionType.TType.Clearing) == 0) ) {
				clearingActions.add(new ActionItem(null, TType.Clearing, null, EntryState.NoOp)) ;
			}
			if ( (t1 != null) && (t1.compareTo(TransactionType.TType.Skip) == 0) ) {
				skipActions.add(new ActionItem(null, TType.Skip, null, EntryState.NoOp)) ;
			}

			int lR = 0, rR = 0 ;
			String aName, aGroup ;
			if ( ((lR = sActs.indexOf(_ID_lR)) != -1) &&
				 ((rR = sActs.indexOf(_ID_rR)) != -1) ) {
				if (sActs.contains(EntryTypes.Self.toString())) {
					aName = sActs.substring(0, lR).trim() ;		// get Name
					esSelf = getAction(lR, sActs) ;			    // EntryState.Add|Enable|Disable|NoOp
					selfActions.add(new ActionItem(EntryType.Self, null, aName, esSelf));
				} else if (sActs.contains(EntryTypes.Group.toString())) {
					aGroup = sActs.substring(0, lR).trim() ;
					esGroup = getAction(lR, sActs) ;			// EntryState.Add|Enable|Disable|NoOp
					groupActions.add(new ActionItem(EntryType.Group, null, aGroup, esGroup));
				}
			}
		}
		if (skipActions.size() != 0) hsActions = new LinkedHashSet<ActionItem>(skipActions) ;
		if (clearingActions.size() != 0) hsActions = new LinkedHashSet<ActionItem>(clearingActions) ;
		if (groupActions.size() != 0) {
			hsActions = new LinkedHashSet<ActionItem>(groupActions) ;
			for (ActionItem ai : groupActions) {
				if (ai.name.length() != 0) sActionGroupName = ai.name;		// expecting only one
			}
		}
		if (selfActions.size() != 0) {
			if (hsActions == null) hsActions = new LinkedHashSet<ActionItem>(selfActions.size()) ;
			for (ActionItem ai : selfActions) hsActions.add(ai) ;
		}

		hmActions.put(sActionGroupName, hsActions) ;
		return hmActions ;
	}

	public static class ActionItem {
		public EntryType pgtype = null;
		public TType ttype = null;
		public String name = null;
		public EntryState state = null;

		public ActionItem() {
		}

		public ActionItem(EntryType t, TType tt, String n, EntryState s) {
			if (t != null) pgtype = t;
			if (tt != null) ttype = tt;
			if ((n != null) && (n.length() != 0)) name = n;
			if (s != null) state = s;
		}
	}
}
