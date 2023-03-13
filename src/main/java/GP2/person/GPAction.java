package GP2.person;

import java.util.Map;

import javax.net.ssl.SNIHostName;

import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.PGType.EntryTypes;
import GP2.utils.Constants;
import GP2.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

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
    
    public static HashMap breakdownActions(String sAction, String sGroupName) {
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

    public static HashMap breakdownActions2(String sAction, String sGroupName) {
        HashMap<String, ActionItem> hmActions = new HashMap<String, ActionItem>() ;

		if (TransactionType.TType.byValue(sAction) == TransactionType.TType.Skip) {
            ActionItem ai = new ActionItem(TransactionType.TType.Skip.toString(), "", "");
            hmActions.put(sGroupName, ai);
            return hmActions;
        }

        if (TransactionType.TType.byValue(sAction) == TransactionType.TType.Clearing) {
            ActionItem ai = new ActionItem(TransactionType.TType.Clearing.toString(), "", "");
            hmActions.put(sGroupName, ai);
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
                ActionItem ai = new ActionItem(EntryType.Group.toString(), "", esGrp.toString());
                hmActions.put(aGroup, ai);
            }
            if (bInd) {
                ActionItem ai = new ActionItem(EntryType.Self.toString(), aName, esInd.toString());
                hmActions.put(sGroupName, ai);
            }
        }

        return hmActions;
    }
}
