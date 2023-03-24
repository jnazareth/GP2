package GP2.group;

import GP2.group.GroupAccount._AGroup;
import GP2.person.GPAction;
import GP2.person.Person;
import GP2.person.GPAction.ActionItem;
import GP2.person.GPAction.ActionItem2;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGType;
import GP2.person.GPAction.TransactionType;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.PGType.EntryTypes;
import GP2.utils.Utils;
import GP2.utils.Constants;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GroupProcessor extends Object {

	Hashtable<String, Person> newAGCollection(String sGroupName) {
		try {
			_AGroup aG = Utils.m_GroupCollection.new _AGroup();
			Utils.m_GroupCollection.put(sGroupName, aG) ;
			return aG.getCollection();
		} catch (Exception e){
			System.err.println("newAGCollection::Error:" + e.getMessage());
			return null ;
		}
	}

	// Find_CreateGroup
	Hashtable<String, Person> Find_CreateGroup2(String sGroupName)
	{
		// find group
		try {
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new GroupAccount() ;

			if (Utils.m_GroupCollection.size() == 0)
				return newAGCollection(sGroupName);
			else {
				_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
				if (aG == null) {
					return newAGCollection(sGroupName);
				} else {
					return aG.getCollection();
				}
			}
		} catch (Exception e){
			System.err.println("Find_CreateGroup::Error:" + e.getMessage());
			return null ;
		}
	}

	// Find_CreateGroup
	Hashtable<String, Person> Find_CreateGroup(String sGroupName)
	{
		// find group
		try {
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new GroupAccount() ;

			_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
			if (aG == null) {
				aG = Utils.m_GroupCollection.new _AGroup();
				Utils.m_GroupCollection.put(sGroupName, aG) ;
				return aG.getCollection();
			} else {
				return aG.getCollection();
			}
		} catch (Exception e){
			System.err.println("Find_CreateGroup::Error:" + e.getMessage());
			return null ;
		}
	}

	EntryState getAction2(int lR, String sA)
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

	// getAction: get specific action
	String getAction(int lR, String sA)
	{
		String sAct = "" ;
		// get action
		int idS = 0 ;
		if ( ((idS = sA.indexOf(Constants._ID_SEPARATOR)) != -1) )
			sAct = sA.substring(lR+1, idS).trim() ;
		else
			System.err.println("Action not specified: " + sA);

		return sAct ;
	}

	void listActions(String a, String g) {
		HashMap<String, ArrayList<String>> hmActions = GPAction.breakdownActions(a, g);
		for (Map.Entry<String, ArrayList<String>> pair : hmActions.entrySet()) {
			System.out.println("groupName:" + pair.getKey());
			for (String item : pair.getValue()) {
				System.out.println("action:" + item);
			}
		}
	}

	void listActions2(String a, String g) {
		HashMap<String, HashSet<ActionItem>> hmActions = GPAction.breakdownActions2(a, g);
		for (Map.Entry<String, HashSet<ActionItem>> pair : hmActions.entrySet()) {
			System.out.println("groupName:" + pair.getKey());
			HashSet<ActionItem> hs = pair.getValue();
			Iterator<ActionItem> iterator = hs.iterator();
			while (iterator.hasNext()) {
				ActionItem ai = iterator.next() ;
				System.out.println("actionItem:" + ai.type + "|" + ai.name + "|" + ai.state);
      		}
		}
	}

	private boolean processActions(String a, String g) {
		boolean bSkip = false;
		//System.out.println("action:" + a + ", group:" + g);

		Hashtable<String, Person> grpPersons = null ;
		HashMap<String, HashSet<ActionItem>> hmActions = GPAction.breakdownActions2(a, g);
		for (Map.Entry<String, HashSet<ActionItem>> pair : hmActions.entrySet()) {
			String sGroupName = pair.getKey() ;
			//System.out.println("for, groupName:" + sGroupName);
			grpPersons = Find_CreateGroup(sGroupName) ;
			//System.out.println(sGroupName + ", grpPersons.size():" + grpPersons.size());


			HashSet<ActionItem> hsActions = pair.getValue();
			Iterator<ActionItem> iterator = hsActions.iterator();
			while (iterator.hasNext()) {
				ActionItem item = iterator.next() ;
				//System.out.println("while, actionItem:" + item.type + "|" + item.name + "|" + item.state);
				if (item.type.toString().compareToIgnoreCase(TransactionType.TType.Skip.toString()) == 0) {
					//System.out.println("skipping ...");
					return true ;	// skip line
				}
				if (item.type.toString().compareToIgnoreCase(TransactionType.TType.Clearing.toString()) == 0) {
					Utils.m_bClearing = true ;
					//System.out.println("bSkip ..." +  bSkip);
					return bSkip;
				}

				//debug
				/*
				System.out.println("item.type:"+ item.type.toString() + ", EntryType.Self.toString():" + EntryType.Self.toString());
				if (item.type.toString().compareToIgnoreCase(EntryType.Group.toString()) == 0) {
					System.out.println("type match::item.type.equals(EntryType.Group.toString()");
				} else if (item.type.toString().compareToIgnoreCase(EntryType.Self.toString()) == 0) {
					System.out.println("type match::item.type.equals(EntryType.Self.toString()");
				}
				System.out.println("item state:" + item.state.toString());
				if (item.state.toString().compareToIgnoreCase(EntryState.Add.toString()) == 0) {
					System.out.println("state match::es1.equals(EntryState.Add)");
				}
				*/

				if (item.type.toString().compareToIgnoreCase(EntryType.Group.toString()) == 0) {
					// group actions, pending
					//System.out.println("(item.type.toString().compareToIgnoreCase(EntryType.Group.toString()) == 0), nothing to do");
				} else if (item.type.toString().compareToIgnoreCase(EntryType.Self.toString()) == 0) {
					Person person = grpPersons.get(item.name);
					if (person != null) {
						if ((item.state.toString().compareToIgnoreCase(EntryState.Enable.toString()) == 0) ||
							(item.state.toString().compareToIgnoreCase(EntryState.Disable.toString()) == 0)) {
							person.m_gState = new PGState(EntryState.byValue(item.state.toString())) ;
							person.m_active = false ;	// remove
						}
					} else {
						if (item.state.toString().compareToIgnoreCase(EntryState.Add.toString()) == 0) {
							person = new Person(item.name, EntryState.Add) ;
							grpPersons.put(item.name, person) ;
							//System.out.println(sGroupName +"::" + item.name + ",grpPersons.size():" + grpPersons.size());
						}
					}
				}
			}
		}
		return bSkip ;
	}

	void listActions3(String a, String g) {
		HashMap<String, HashSet<ActionItem2>> hmActions = GPAction.breakdownActions3(a, g);
        System.out.println("listActions3: ----- " + hmActions.size());
		for (Map.Entry<String, HashSet<ActionItem2>> pair : hmActions.entrySet()) {
			System.out.println("groupName:" + pair.getKey());
			HashSet<ActionItem2> hs = pair.getValue();
			Iterator<ActionItem2> iterator = hs.iterator();
			while (iterator.hasNext()) {
				ActionItem2 ai = iterator.next() ;
				System.out.println("actionItem:" + ai.pgtype + "|" + ai.ttype + "|" + ai.name + "|" + ai.state);
      		}
		}
        System.out.println("listActions3: ----- ");
	}

	private boolean processActions2(String a, String g) {
		boolean bSkip = false;
		//System.out.println("processActions2. action:" + a + ", group:" + g);

		Hashtable<String, Person> grpPersons = null ;
		HashMap<String, HashSet<ActionItem2>> hmActions = GPAction.breakdownActions3(a, g);
		for (Map.Entry<String, HashSet<ActionItem2>> pair : hmActions.entrySet()) {
			String sGroupName = pair.getKey() ;
			//System.out.println("for, groupName:" + sGroupName);
			grpPersons = Find_CreateGroup(sGroupName) ;
			//System.out.println(sGroupName + ", grpPersons.size():" + grpPersons.size());

			HashSet<ActionItem2> hsActions = pair.getValue();
			Iterator<ActionItem2> iterator = hsActions.iterator();
			while (iterator.hasNext()) {
				ActionItem2 item = iterator.next() ;

				if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Skip) == 0)) {
					//System.out.println("match skip::" + item.ttype.toString());
					return true ;	// skip line
				} else if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Clearing) == 0)) {
					Utils.m_bClearing = true ;
					//System.out.println("bSkip ..." +  bSkip);
					return bSkip;
				}
				//System.out.println("clearing check done");

				if ((item.pgtype.compareTo(EntryType.Group) == 0)) {
					//System.out.println("group processing");
					// group actions, pending
					//System.out.println("(item.type.toString().compareToIgnoreCase(EntryType.Group.toString()) == 0), nothing to do");
				} else if ((item.pgtype.compareTo(EntryType.Self) == 0)) {
					//System.out.println("self processing");

					Person person = grpPersons.get(item.name);
					if (person != null) {
						//System.out.println("not null, person:" + person.m_name);
						if (((item.state.compareTo(EntryState.Enable)) == 0) ||
							((item.state.compareTo(EntryState.Disable)) == 0)) {
							person.m_gState = new PGState(EntryState.byValue(item.state.toString())) ;
							person.m_active = false ;	// remove
						}
					} else {
						//System.out.println("null, person:");
						if ((item.state.compareTo(EntryState.Add) == 0)) {
							person = new Person(item.name, EntryState.Add) ;
							grpPersons.put(item.name, person) ;
							//System.out.println(sGroupName +"::" + item.name + ",grpPersons.size():" + grpPersons.size());
						}
					}
				}
			}
		}
		return bSkip ;
	}

	public boolean doGroupAction3(String action, String sGroup)
	{
		//String sG = ((sGroup.length() == 0) ? Constants._DEFAULT_GROUP : sGroup) ;
		String sG = "";
		if (sGroup.length() == 0) sG = Constants._DEFAULT_GROUP;
		else sG = sGroup;

		//listActions2(action, sG);
		//boolean bProcess = processActions(action, sG) ;

		//listActions3(action, sG);
		boolean bProcess = processActions2(action, sG) ;
		return bProcess;
	}

    // doGroupAction: process input action
	// name1 (*/+/-:self), name2 (*/+/-:self): add/enable/disable individuals
	// group1 (*/+/-:group): add/enable/disable group
	public boolean doGroupAction2(String action, String sGroup)
	{
		String sG = "";
		if (sGroup.length() == 0) sG = Constants._DEFAULT_GROUP;
		else sG = sGroup;
		//listActions2(action, sG);
		//boolean pA = processActions(action, sG) ;
		//return pA;

		boolean bSkip = false;
		if (TransactionType.TType.byValue(action) == TransactionType.TType.Skip) return true ;	// skip line

		String sGroupName = Constants._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
		Hashtable<String, EntryState> gActions = null ;
		Hashtable<String, EntryState> iActions = null ;

		// process Action
		if (action.length() != 0) {
			String[] pieces = action.split(Constants._ITEM_SEPARATOR);
			EntryState esInd = EntryState.Add, esGrp = EntryState.Add;
			String sActs2 = "";
			for (String p : pieces) {
				sActs2 = p ;
				if (TransactionType.TType.byValue(sActs2) == TransactionType.TType.Clearing) {	// pay between individuals
					Utils.m_bClearing = true ;
				}

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
					if (gActions == null) {
						gActions = new Hashtable<String, EntryState>() ;
						gActions.put(aGroup, esGrp) ;
					} else
						gActions.put(aGroup, esGrp) ;
				}
				if (bInd) {
					if (iActions == null) {
						iActions = new Hashtable<String, EntryState>() ;
						iActions.put(aName, esInd) ;
					} else
						iActions.put(aName, esInd) ;
				}
			}
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new GroupAccount() ;

			if (gActions != null) {
				for(String gName: gActions.keySet()) {
					sGroupName = gName ;
					esGrp = gActions.get(gName);
					Find_CreateGroup(sGroupName) ;
				}
			}

			if (iActions != null) {
				for(String iName: iActions.keySet()) {
					esInd = iActions.get(iName);
					try {
						Hashtable<String, Person> aGrp = Find_CreateGroup(sGroupName) ;
						//System.out.println(sGroupName + ", aGrp.size():" + aGrp.size());

						Person person = aGrp.get(iName);
						if (person != null) {	// found, flip enable/disable
							if (esInd.equals(EntryState.Enable)) {
								person.m_gState = new PGState(EntryState.Enable) ;
								person.m_active = true ;	// remove
							} else if (esInd.equals(EntryState.Disable)) {
								person.m_gState = new PGState(EntryState.Disable) ;
								person.m_active = false ;	// remove
							}
						} else {	// not found, add
							if (esInd.equals(EntryState.Add)) {
								Person aPerson = new Person(iName.trim(), EntryState.Add) ;
								aGrp.put(iName, aPerson) ;
								//System.out.println(sGroupName +"::" + iName + ",aGrp.size():" + aGrp.size());
							}
						}
					} catch (Exception e) {
							System.err.println("Error:doGroupAction2 " + e.getMessage());
					}
				}
			}
		} // action
		//Utils.m_GroupCollection.dumpCollection();
		return bSkip ;
	}

    // doGroupAction: process input action
	// name1 (*/+/-:self), name2 (*/+/-:self): add/enable/disable individuals
	// group1 (*/+/-:group): add/enable/disable group
	public String doGroupAction(String action)
	{
		//System.out.println("action: " + action);

		String sGroupName = Constants._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
		ArrayList<String> grpActions = null, indActions = null ;

		// process Action
		if (action.length() != 0) {
			String[] pieces = action.split(Constants._ITEM_SEPARATOR);
			String sActs = "", sIndAct = Constants.ADD_ITEM, sGrpAct = Constants.ADD_ITEM;
			for (String p : pieces) {
				sActs = p ;

				if (sActs.endsWith(Constants._CLEARING)) {	// pay between individuals
					Utils.m_bClearing = true ;
				}

				int lR = 0, rR = 0 ;
				String aName = "", aGroup = Constants._DEFAULT_GROUP ;
				// valid construct: <name> (*:self or *:group)
				if ( ((lR = sActs.indexOf(Constants._ID_lR)) != -1) && ((rR = sActs.indexOf(Constants._ID_rR)) != -1) ) {
						// get name: self or group
						if ( (bInd = sActs.contains(Constants._SELF)) ) {
							aName = sActs.substring(0, lR).trim() ;
							sIndAct = getAction(lR, sActs) ;
						}
						else if ( (bGroup = sActs.contains(Constants._GROUP)) ) {
							aGroup = sActs.substring(0, lR).trim() ;
							sGrpAct = getAction(lR, sActs) ;
						}
						else
							; //System.err.println("Individual or Group not specified: " + action);
				}

				if (bGroup) {
					if (grpActions == null) {
						grpActions = new ArrayList<String>() ;
						grpActions.add(aGroup + Constants._ID_SEPARATOR + sGrpAct) ;
					} else
						grpActions.add(aGroup + Constants._ID_SEPARATOR + sGrpAct) ;
				}

				if (bInd) {
					if (indActions == null) {
						indActions = new ArrayList<String>() ;
						indActions.add(aName + Constants._ID_SEPARATOR + sIndAct) ;
					} else
						indActions.add(aName + Constants._ID_SEPARATOR + sIndAct) ;
				}
			} // while

			// Create Collections
			//if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new Hashtable<String, Hashtable<String, Person>>() ;
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new GroupAccount() ;

			if (grpActions != null) {
				for (String aAction : grpActions) {
					int idS = -1 ;
					if ( ((idS = aAction.indexOf(Constants._ID_SEPARATOR)) != -1) ) {
						sGroupName = aAction.substring(0, idS).trim() ;
						sGrpAct = aAction.substring(idS+1, aAction.length()).trim() ;
						Find_CreateGroup(sGroupName) ;
					}
				}
			}

			if (indActions != null) {
				for (String aAction : indActions) {
					int idS = -1 ;
					if ( ((idS = aAction.indexOf(Constants._ID_SEPARATOR)) != -1) ) {
						String sIndName = aAction.substring(0, idS).trim() ;
						sIndAct = aAction.substring(idS+1, aAction.length()).trim() ;

						try {
							Hashtable<String, Person> aGrp = Find_CreateGroup(sGroupName) ;
							//System.out.println("sGroupName: " + sGroupName);

							Person person = aGrp.get(sIndName);
							if (person != null) { // found, flip enable/disable
								//System.out.println("SEARCH: " + sIndName + " ,FOUND: " + person.m_name + ":" + person.m_active + ": flip active");
								if (sIndAct.compareToIgnoreCase(Constants.DISABLE_ITEM) == 0) {
									person.m_active = false ;
								} else if (sIndAct.compareToIgnoreCase(Constants.ENABLE_ITEM) == 0) {
									person.m_active = true ;
								}
								aGrp.put(sIndName, person) ;
							} else { // not found, add
								//System.out.println("SEARCH: " + sIndName + ": NOT found, add");
								if (sIndAct.compareToIgnoreCase(Constants.ADD_ITEM) == 0) {
									Person aPerson = new Person(sIndName.trim(), true) ;
									aGrp.put(sIndName, aPerson) ;
								}
							}
						} catch (Exception e){
							System.err.println("Error:doGroupAction " + e.getMessage());
						}
					}
				}
			}
		} // action
		return sGroupName ;
	}
}
