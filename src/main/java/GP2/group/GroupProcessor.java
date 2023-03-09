package GP2.group;

import GP2.person.Person;
import GP2.utils.Utils;
import GP2.utils.Constants;

import java.util.Hashtable;
import java.util.ArrayList;

public class GroupProcessor extends Object {
    
	// Find_CreateGroup
	Hashtable<String, Person> Find_CreateGroup(String sGrpName)
	{
		// find group
		try {
			Hashtable<String, Person> aGrp = Utils.m_GroupCollection.get(sGrpName) ;
			if (aGrp == null) {
				aGrp = new Hashtable<String, Person>() ;
				Utils.m_GroupCollection.put(sGrpName, aGrp) ;
			} else {
				// found, do nothing
			}
			return aGrp ;
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			return null ;
		}
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

    // doGroupAction: process input action
	// name1 (*/+/-:self), name2 (*/+/-:self): add/enable/disable individuals
	// group1 (*/+/-:group): add/enable/disable group
	public String doGroupAction2(String action, String sGroup)
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
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new Hashtable<String, Hashtable<String, Person>>() ;

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
			if (Utils.m_GroupCollection == null) Utils.m_GroupCollection = new Hashtable<String, Hashtable<String, Person>>() ;

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
