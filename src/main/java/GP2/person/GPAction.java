package GP2.person;

import java.util.Map;

import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.PGType.EntryTypes;
import GP2.person.GPAction.TransactionType.TType;
import GP2.utils.Constants;
import GP2.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GPAction {
    public static class PGState {
        public enum EntryState {
            Add("*"),
            Enable("+"),
            Disable("-");

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
        }

        private EntryState state;

        public PGState(EntryState s) {
            state = s;
        }

        public void add()		    { state = EntryState.Add; }
        public void enable()		{ state = EntryState.Enable; }
        public void disable()	    { state = EntryState.Disable; }

        public boolean getActive() {
            return ( ((state == EntryState.Enable) || (state == EntryState.Add)) ? true : false )  ;
        }

        public boolean equals (EntryState es) {
            return (this.state == es) ;
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

	static EntryState getAction2(int lR, String sA)
	{
		String sAct = "" ;
		// get action
		int idS = 0 ;
		if ( ((idS = sA.indexOf(Constants._ID_SEPARATOR)) != -1) )
			sAct = sA.substring(lR+1, idS).trim() ;
		else
			System.err.println("Action not specified: " + sA);

		return EntryState.byValue(sAct) ;
	}

    public static HashMap<String, ArrayList<String>> breakdownActions(String sAction, String sGroupName) {
        HashMap<String, ArrayList<String>> hmActions = new HashMap<String, ArrayList<String>>() ;
        ArrayList<String> actionList = new ArrayList<String>() ;

		if (TransactionType.TType.byValue(sAction) == TransactionType.TType.Skip) {
            actionList.add(TransactionType.TType.Skip.toString());
            hmActions.put(sGroupName, actionList);
            return hmActions;
        }

        if (TransactionType.TType.byValue(sAction) == TransactionType.TType.Clearing) {
            actionList.add(TransactionType.TType.Clearing.toString());
            hmActions.put(sGroupName, actionList);
            return hmActions;
        }

        //sGroupName = Constants._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
        String[] pieces = sAction.split(Constants._ITEM_SEPARATOR);
        EntryState esInd = EntryState.Add, esGrp = EntryState.Add;
        String sActs2 = "";

        for (String p : pieces) {
            sActs2 = p ;

            int lR = 0, rR = 0 ;
            String aName = "", aGroup = Constants._DEFAULT_GROUP ;
            if ( ((lR = sActs2.indexOf(Constants._ID_lR)) != -1) && ((rR = sActs2.indexOf(Constants._ID_rR)) != -1) ) {
                // get name: self or group
                if ( (bInd = sActs2.contains(EntryTypes.Self.toString())) ) {
                    aName = sActs2.substring(0, lR).trim() ;
                    esInd = getAction2(lR, sActs2) ;
                } else if (bGroup = sActs2.contains(EntryTypes.Group.toString())) {
                    aGroup = sActs2.substring(0, lR).trim() ;
                    esGrp = getAction2(lR, sActs2) ;
                } else
                    ; //System.err.println("Individual or Group not specified: " + action);
            }
            if (bGroup) {
                actionList.add(EntryType.Group.toString());
                actionList.add(esGrp.toString());
                hmActions.put(aGroup, actionList);
            }
            if (bInd) {
                actionList.add(EntryType.Self.toString());
                actionList.add(aName);
                actionList.add(esInd.toString());
                hmActions.put(sGroupName, actionList);
            }
        }
        return hmActions ;
    }

    public static class ActionItem {
        public String type;
        public String name;
        public String state;

        public ActionItem() {
        }

        public ActionItem(String t, String n, String s) {
            if (t.length() != 0) type = t;
            if (n.length() != 0) name = n;
            if (s.length() != 0) state = s;
        }
    }

    public static HashMap<String, HashSet<ActionItem>> breakdownActions2(String sAction, String sGroupName) {
        HashMap<String, HashSet<ActionItem>> hmActions = new HashMap<String, HashSet<ActionItem>>() ;
        if (sAction.length() == 0) return hmActions;

        HashSet<ActionItem> hsActions = new HashSet<ActionItem>();
        ActionItem ai = null;

		if ((TransactionType.TType.byValue(sAction) == TransactionType.TType.Skip) ||
            (TransactionType.TType.byValue(sAction) == TransactionType.TType.Clearing)) {
            TransactionType.TType tt = TransactionType.TType.byValue(sAction) ;
            ai = new ActionItem(tt.toString(), "", "");
			hsActions.add(ai) ;
            hmActions.put(sGroupName, hsActions);
            return hmActions;
        }

        //sGroupName = Constants._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
        String[] pieces = sAction.split(Constants._ITEM_SEPARATOR);
        EntryState esInd = EntryState.Add, esGrp = EntryState.Add;
        String sActs2 = "";

        for (String p : pieces) {
            sActs2 = p ;
            //System.out.println("act: " + sActs2);

            int lR = 0, rR = 0 ;
            String aName = "", aGroup = Constants._DEFAULT_GROUP ;
            if ( ((lR = sActs2.indexOf(Constants._ID_lR)) != -1) && ((rR = sActs2.indexOf(Constants._ID_rR)) != -1) ) {
                // get name: self or group
                if ( (bInd = sActs2.contains(EntryTypes.Self.toString())) ) {
                    aName = sActs2.substring(0, lR).trim() ;
                    esInd = getAction2(lR, sActs2) ;
                    //System.out.println(sGroupName + "::" + aName + ":" + esInd);
                } else if (bGroup = sActs2.contains(EntryTypes.Group.toString())) {
                    aGroup = sActs2.substring(0, lR).trim() ;
                    esGrp = getAction2(lR, sActs2) ;
                } else
                    ; //System.err.println("Individual or Group not specified: " + action);
            }
            if (bGroup) {
                ai = new ActionItem(EntryType.Group.toString(), "", esGrp.toString());
				hsActions.add(ai) ;
                //System.out.println("grp, hsActions.size(): " + hsActions.size());
				hmActions.put(aGroup, hsActions);
            }
            if (bInd) {
                ai = new ActionItem(EntryType.Self.toString(), aName, esInd.toString());
				hsActions.add(ai) ;
                //System.out.println("ind, hsActions.size(): " + hsActions.size());
				hmActions.put(sGroupName, hsActions);
            }
        }

        System.out.println("bd2: " + hmActions.size());
		for (Map.Entry<String, HashSet<ActionItem>> pair : hmActions.entrySet()) {
			System.out.println("groupName:" + pair.getKey());
			HashSet<ActionItem> hs2 = pair.getValue();
			Iterator<ActionItem> iterator = hs2.iterator();
			while (iterator.hasNext()) {
				ActionItem ai2 = iterator.next() ;
				System.out.println("actionItem:" + ai2.type + "|" + ai2.name + "|" + ai2.state);
      		}
		}
        System.out.println("bd2: ----- ");

        return hmActions;
    }

	public static class ActionItem2 {
		public EntryType pgtype = null;
		public TType ttype = null;
		public String name = null;
		public EntryState state = null;

		public ActionItem2() {
		}

		public ActionItem2(EntryType t, TType tt, String n, EntryState s) {
			if (t != null) pgtype = t;
			if (tt != null) ttype = tt;
			if (n.length() != 0) name = n;
			if (s != null) state = s;
		}
	}

    public static HashMap<String, HashSet<ActionItem2>> breakdownActions3(String sAction, String sGroupName) {
        HashMap<String, HashSet<ActionItem2>> hmActions = new HashMap<String, HashSet<ActionItem2>>() ;
        //hmActions.clear();
        if (sAction.length() == 0) {
            //System.out.println("no action");
            return hmActions;
        }

    try {
        HashSet<ActionItem2> hsActions = new HashSet<ActionItem2>();
        //hsActions.clear();
        ActionItem2 ai = null;

        //System.out.println(sGroupName + ":::" + sAction);

        TType t1 = TransactionType.TType.byValue(sAction);
        if ( (t1 != null) &&
            ((t1.compareTo(TransactionType.TType.Clearing) == 0) || (t1.compareTo(TransactionType.TType.Skip) == 0)) ) {
            //System.out.println("match skip::" + sAction);
            ai = new ActionItem2();
            if (ai != null ) {
                ai.ttype = t1;

                hsActions.add(ai) ;
                hmActions.put(sGroupName, hsActions);
                //System.out.println("skipping::" + hmActions.size());
                return hmActions;
            }
        }

        //sGroupName = Constants._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
        String[] pieces = sAction.split(Constants._ITEM_SEPARATOR);
        EntryState esInd = EntryState.Add, esGrp = EntryState.Add;
        String sActs2 = "";

        for (String p : pieces) {
            sActs2 = p ;
            //System.out.println("act: " + sActs2);

            int lR = 0, rR = 0 ;
            String aName = "", aGroup = Constants._DEFAULT_GROUP ;
            if ( ((lR = sActs2.indexOf(Constants._ID_lR)) != -1) && ((rR = sActs2.indexOf(Constants._ID_rR)) != -1) ) {
                // get name: self or group
                if ( (bInd = sActs2.contains(EntryTypes.Self.toString())) ) {
                    aName = sActs2.substring(0, lR).trim() ;
                    esInd = getAction2(lR, sActs2) ;
                    //System.out.println("self:" + sGroupName + "::" + aName + ":" + esInd);
                } else if (bGroup = sActs2.contains(EntryTypes.Group.toString())) {
                    aGroup = sActs2.substring(0, lR).trim() ;
                    esGrp = getAction2(lR, sActs2) ;
                    //System.out.println("group:" + sGroupName + "::" + aGroup + ":" + esGrp);
                } else
                    ; //System.err.println("Individual or Group not specified: " + action);
            }
            if (bGroup) {
                //System.out.println("group true");

                /*ai = new ActionItem2(EntryType.Group, null, null, esGrp);
                if (ai != null) {
				hsActions.add(ai) ;
                System.out.println("grp, hsActions.size(): " + hsActions.size());
				hmActions.put(aGroup, hsActions);
                System.out.println("ind, hmActions.size(): " + hmActions.size());
                }*/

                ai = new ActionItem2();
                if (ai != null ) {
                    ai.pgtype = EntryType.Group;
                    ai.state = esGrp;

                    Boolean b = hsActions.add(ai) ;
                    //System.out.println("grp, hsActions.size(): " + hsActions.size() + ":" + b);
                    hmActions.put(aGroup, hsActions);
                    //System.out.println("grp, hmActions.size(): " + hmActions.size());
                }
            }
            if (bInd) {
                //System.out.println("ind true");

                /*if (ai != null) {
                ai = new ActionItem2(EntryType.Self, null, aName, esInd);
				hsActions.add(ai) ;
                System.out.println("ind, hsActions.size(): " + hsActions.size());
				hmActions.put(sGroupName, hsActions);
                System.out.println("ind, hmActions.size(): " + hmActions.size());
                }*/

                ai = new ActionItem2();
                if (ai != null ) {
                    ai.pgtype = EntryType.Self;
                    ai.name = aName;
                    ai.state = esInd;

                    Boolean b = hsActions.add(ai) ;
                    //System.out.println("ind, hsActions.size(): " + hsActions.size() + ":" + b);
                    hmActions.put(sGroupName, hsActions);
                    //System.out.println("ind, hmActions.size(): " + hmActions.size());
                }
            }
        }

        /*System.out.println("bd3: ----- " + hmActions.size());
		for (Map.Entry<String, HashSet<ActionItem2>> pair : hmActions.entrySet()) {
			System.out.println("groupName:" + pair.getKey());
			hsActions = pair.getValue();
			Iterator<ActionItem2> iterator = hsActions.iterator();
			while (iterator.hasNext()) {
				ai = iterator.next() ;
				System.out.println("actionItem2:" + ai.pgtype + "|" + ai.ttype + "|" + ai.name + "|" + ai.state);
      		}
		}
        System.out.println("bd3: ----- " + hmActions.toString());*/

        return hmActions;
    } catch (Exception e) {
        System.err.println("bd3:error:"+ e.getMessage());
        return hmActions;
    }
    }
}
