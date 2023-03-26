package GP2.group;

import GP2.group.GroupAccount._AGroup;
import GP2.person.GPAction;
import GP2.person.Person;
import GP2.person.GPAction.ActionItem;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.TransactionType;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.utils.Utils;
import GP2.utils.Constants;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

public class GroupProcessor extends Object {
	static final boolean bSkip = true, bDoNoSkip = false ;

	Hashtable<String, Person> Find_CreateGroup(String sGroupName) {
		try {
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new GroupAccount() ;

			_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
			if (aG == null) {
				aG = Utils.m_GroupCollection.new _AGroup();
				//System.out.println("new aG:" + sGroupName + ", state" + aG.m_gState);
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

    boolean groupExists(String sGroupName) {
		try {
			if (Utils.m_GroupCollection == null) return false ;
			_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
			return ( (aG == null) ? false:true ) ;
		} catch (Exception e){
			System.err.println("groupExists::Error:" + e.getMessage());
			return false ;
		}
	}

    boolean personExists(String sGroupName, String sName) {
		try {
			if (Utils.m_GroupCollection == null) return false ;
			_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
			if (aG == null) {
				return false ;
			} else {
				Hashtable<String, Person> aGrp = aG.getCollection();
				Person person = aGrp.get(sName);
				return ( (person == null) ? false:true ) ;
			}
		} catch (Exception e){
			System.err.println("personExists::Error:" + e.getMessage());
			return false ;
		}
	}

	static boolean doClearing(String sGroupName, boolean bGroupExists) {
		//System.out.println("clearing, groupName:" + sGroupName + ", " + bGroupExists);
		if (!bGroupExists) {
			return logGroupPersonError(EntryType.Group, sGroupName) ;
		} else {
			Utils.m_bClearing = true ;
			return bDoNoSkip;
		}
	}

	static boolean isEntryStateToggle(ActionItem item) {
		return (((item.state.compareTo(EntryState.Enable)) == 0) || ((item.state.compareTo(EntryState.Disable)) == 0) || ((item.state.compareTo(EntryState.NoOp)) == 0)) ;
	}
	static boolean isEntryStateAdd(ActionItem item) {
		return ((item.state.compareTo(EntryState.Add)) == 0) ;
	}

	static boolean logGroupPersonError(EntryType et, String sName) {
		if (et.compareTo(EntryType.Group) == 0) {
			System.err.println("Group specified does not exist. Use: \"" +  sName + "(*:group)\" to create");
		} else if (et.compareTo(EntryType.Self) == 0) {
			System.err.println("Person specified does not exist. Use: \"" +  sName + "(*:self)\" to create");
		}
		return bSkip;		// skip, does not exist.
	}

	boolean processActions(HashMap<String, LinkedHashSet<ActionItem>> hmActions) {
		for (Map.Entry<String, LinkedHashSet<ActionItem>> pair : hmActions.entrySet()) {
			String sGroupName = pair.getKey() ;

			LinkedHashSet<ActionItem> hsActions = pair.getValue();
			Iterator<ActionItem> iterator = hsActions.iterator();
			while (iterator.hasNext()) {
				boolean bGroupExists = groupExists(sGroupName) ;

				ActionItem item = iterator.next() ;
				if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Skip) == 0)) return bSkip ;	// skip line
				if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Clearing) == 0)) return doClearing(sGroupName, bGroupExists) ;

				if ((item.pgtype.compareTo(EntryType.Group) == 0)) {
					if ( isEntryStateToggle(item) ) {
						if (!bGroupExists) {
							return logGroupPersonError(EntryType.Group, sGroupName) ;
						} else {
							Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
							_AGroup aG = Utils.m_GroupCollection.get(sGroupName);
							if (aG != null) aG.m_gState = new PGState(item.state) ;
						}
					} else if (isEntryStateAdd(item)) {
						Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
					}
				} else if ((item.pgtype.compareTo(EntryType.Self) == 0)) {
					if ( isEntryStateToggle(item) )  {
						if (!bGroupExists) return logGroupPersonError(EntryType.Group, sGroupName) ;

						boolean bPersonExists = personExists(sGroupName, item.name) ;
						if (!bPersonExists) {
							logGroupPersonError(EntryType.Self, item.name) ;
						} else {
							Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
							Person person = aGroup.get(item.name);
							person.m_gState = new PGState(item.state) ;
							//person.m_active = !person.m_active; // toggle, remove
						}
					} else if (isEntryStateAdd(item)) {
						if (!bGroupExists) return logGroupPersonError(EntryType.Group, sGroupName) ;

						Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
						Person aPerson = new Person(item.name.trim(), EntryState.Add) ;
						aGroup.put(item.name, aPerson) ;
					}
				}
			}
		}
		return bDoNoSkip ;
	}

	public boolean doGroupAction(String action, String sGroup) {
		String sG = ((sGroup.length() == 0) ? Constants._DEFAULT_GROUP : sGroup) ;
		HashMap<String, LinkedHashSet<ActionItem>> hmActions = GPAction.breakdownActions(action, sG) ;
		boolean bProcess = processActions(hmActions) ;
		return bProcess;
	}
}
