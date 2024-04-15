package GP2.group;

//import GP2.group.GroupAccount._AGroup;
import GP2.group.Groups.Group;
import GP2.person.GPAction;
import GP2.person.GPAction.ActionItem;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.TransactionType;
import GP2.person.Person;
import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xcur.CrossCurrency;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

public class GroupProcessor extends Object {
	static final boolean bSkip = true, bDoNoSkip = false ;

	Hashtable<String, Person> Find_CreateGroup(String sGroupName) {
		try {
			if (Utils.m_Groups == null) Utils.m_Groups = new Groups() ;

			Group aG = Utils.m_Groups.get(sGroupName);
			if (aG == null) {
				aG = Utils.m_Groups.new Group(sGroupName);
				//System.out.println("new aG:" + sGroupName + ", state" + aG.m_gState);
				Utils.m_Groups.put(sGroupName, aG) ;
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
			if (Utils.m_Groups == null) return false ;
			Group aG = Utils.m_Groups.get(sGroupName);
			return ( (aG == null) ? false:true ) ;
		} catch (Exception e){
			System.err.println("groupExists::Error:" + e.getMessage());
			return false ;
		}
	}

    boolean personExists(String sGroupName, String sName) {
		try {
			if (Utils.m_Groups == null) return false ;
			Group aG = Utils.m_Groups.get(sGroupName);
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
			System.err.println("Error: \"" + sName + "\" group does not exist. Use: \"" +  sName + " (*:group)\" to create");
		} else if (et.compareTo(EntryType.Self) == 0) {
			System.err.println("Error: \"" + sName + "\" person does not exist. Use: \"" +  sName + " (*:self)\" to create");
		}
		return bSkip;		// skip, does not exist.
	}

	boolean processActions(HashMap<String, LinkedHashSet<ActionItem>> hmActions, TransactionType.TType tt[]) {
		for (Map.Entry<String, LinkedHashSet<ActionItem>> pair : hmActions.entrySet()) {
			String sGroupName = pair.getKey() ;

			LinkedHashSet<ActionItem> hsActions = pair.getValue();
			Iterator<ActionItem> iterator = hsActions.iterator();
			while (iterator.hasNext()) {
				boolean bGroupExists = groupExists(sGroupName) ;

				ActionItem item = iterator.next() ;
				if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Skip) == 0)) {
					tt[0] = TransactionType.TType.Skip;
					return bSkip ;	// skip line
                }
                if ((item.ttype != null) &&	(item.ttype.compareTo(TransactionType.TType.Clearing) == 0)) {
					tt[0] = TransactionType.TType.Clearing;
					return doClearing(sGroupName, bGroupExists) ;
				}
                if ((item.pgtype != null) && (item.pgtype.compareTo(EntryType.Group) == 0)) {
					if ( isEntryStateToggle(item) ) {
						if (!bGroupExists) {
							return logGroupPersonError(EntryType.Group, sGroupName) ;
						} else {
							Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
							Group aG = Utils.m_Groups.get(sGroupName);
							if (aG != null) aG.m_gState = new PGState(item.state) ;
						}
					} else if (isEntryStateAdd(item)) {
						Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
					}
				} else if ((item.pgtype != null) && (item.pgtype.compareTo(EntryType.Self) == 0)) {
					if ( isEntryStateToggle(item) )  {
						if (!bGroupExists) return logGroupPersonError(EntryType.Group, sGroupName) ;

						boolean bPersonExists = personExists(sGroupName, item.name) ;
						if (!bPersonExists) {
							logGroupPersonError(EntryType.Self, item.name) ;
						} else {
							Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
							Person person = aGroup.get(item.name);
							person.m_gState = new PGState(item.state) ;
						}
					} else if (isEntryStateAdd(item)) {
						if (!bGroupExists) return logGroupPersonError(EntryType.Group, sGroupName) ;

						Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
						Person aPerson = new Person(item.name.trim(), EntryState.Add) ;
						aGroup.put(item.name, aPerson) ;
					}
				} else if ((item.xcType != null) &&	(item.xcType.compareTo(CrossCurrency.XCurrencyType.XCurrency) == 0)) {
					for (Map.Entry<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> xpair: item.xCurrency.entrySet()) {
						String sG = xpair.getKey() ;
						//System.out.println("sG:" + sG);
						EnumMap<CrossCurrency.XCurrencyProperties, String> xProperties = xpair.getValue();
						/*for (XCurrencyProperties p : xProperties.keySet()) {
							System.out.println(p + ":" + xProperties.get(p));
						}*/

						Hashtable<String, Person> aGroup = Find_CreateGroup(sGroupName) ;
						Group aG = Utils.m_Groups.get(sGroupName);
						if (aG != null) aG.m_ccurrency = new CrossCurrency(xProperties);
						//if (aG.m_ccurrency != null) System.out.println("aG.m_xcurrency:" + aG.m_ccurrency);
					}
				}
			}
		}
		return bDoNoSkip ;
	}

	public boolean doGroupAction(String action, String sGroup, TransactionType.TType tt[]) {
		String sG = ((sGroup.length() == 0) ? Constants._DEFAULT_GROUP : sGroup) ;
		HashMap<String, LinkedHashSet<ActionItem>> hmActions = GPAction.breakdownActions(action, sG) ;
		boolean bProcess = processActions(hmActions, tt) ;

		// pull group name from action processing, otherwise this is "default"
		// https://www.geeksforgeeks.org/how-to-print-all-keys-of-the-linkedhashmap-in-java/

		Set<String> allKeys = hmActions.keySet();
		if (!allKeys.contains(sGroup)) {
			System.err.println("Error: " + allKeys + " missing under \"group\". Add to resolve.");
		}

		/*
			<blank>	(default)		<blank>, jn (*:self)				no match, null
			<blank>	(default)		home (*:group), jn (*:self)			no match, not null
																		default missing under "group". Add to resolve.
			home					<blank>, jn (*:self)				no match, null
																		Group specified does not exist. Use: "broadridge.return(*:group)" to create
																		broadridge.return found in Action
			home					home (*:group), jn (*:self)			match, not null
		*/


		/*
			b.r	(*:group), b.r, xrate	b.r = b.r, groups match. good
			(*:group) b.r, xrate		default != b.r: groups don't match
			b.r	xrate					if b.r found, good, else group "b.r" does not exist
			xrate						default group
		*/
		/*(if (Utils.m_Groups.get(sGroup) == null) {
			System.err.println("group \"" + sGroup + "\" not found. add action: \"" +  sGroup + " (*:group)\"" + " to create group." );
		}*/

		return bProcess;
	}
}
