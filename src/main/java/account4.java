//package com.mycompany.GPExplorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;

public class account4
{
	// ----------------------------------------------------
	// Declarations
	// ----------------------------------------------------
	//private Hashtable<String, Person3> /* m_Persons, replaced by Group */ m_System ;
	//private Hashtable<String, Hashtable<String, Person3>> m_GroupCollection3 ;
	private int m_numActive = 0 ;
	private float m_nTotAmount = 0, m_nSysToAmount ;
	private ArrayList<String> m_exportLines = null;
	//private Hashtable<String, ArrayList<String>> m_exportLinesGroup ;
	private boolean m_bClearing = false ;
	//private boolean m_bSys = false ;

	// ----------------------------------------------------
	// Class body
	// ----------------------------------------------------

	// ----------------------------------------------------
	// getFile
	// ----------------------------------------------------
	private File getFile(String fileName)
	throws FileNotFoundException
	{
		File aFile = new File(fileName);
		if (aFile.exists()) return aFile;
		else throw new FileNotFoundException("File  " + fileName + " does not exist.");
	}

	// ----------------------------------------------------
	// numActive
	// ----------------------------------------------------
	private int numActive(String sGroupName)
	{
		int i = 0 ;
		Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(sGroupName) ;
		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person3 person = aGroup.get(keysPeople.nextElement());
			if (person.m_active == true) i++ ;
		}
		return i ;
	}

	// ----------------------------------------------------
	// percentageToAmounts
	// ----------------------------------------------------
	private String percentageToAmounts(float amt, String in, String action)
	{
		if (in.indexOf(Utils._PERCENTAGE) == -1) return in;

		String sOut = "" ;
		String sIn[] = in.split(Utils._ITEM_SEPARATOR) ;

		String eachName = "" ;
		float eachPer = 0 ;
		for (int i = 0; i < sIn.length; i++) {
			eachName = "";	eachPer = 0 ;
			String sEach[] = sIn[i].split(Utils._AMT_INDICATOR) ;
			for (int k = 0; k < sEach.length; k++) {
				int pLoc = -1 ;
				if ((pLoc = sEach[k].indexOf(Utils._PERCENTAGE)) == -1) {
					try {
						eachPer = Float.parseFloat(sEach[k]) ;
						sOut += Utils._AMT_INDICATOR + sEach[k] ;
					} catch (NumberFormatException e) {
						eachName = sEach[k].trim() ;
						if (sOut == "") sOut += sEach[k] ;
						else sOut += Utils._ITEM_SEPARATOR + sEach[k] ;
					}
				} else {
					eachPer = Float.parseFloat(sEach[k].substring(0, pLoc)) ;
					float xAmt = amt * eachPer / 100 ;
					sOut += Utils._AMT_INDICATOR + String.valueOf(xAmt) ;
				}
			}
		}
		////System.out.println("percentageToAmounts: amt = " + amt + ", sIn = " + in + ", sOut = " +  sOut);
		return sOut ;
	}


	private void putIndivAmount(String sGroupName, int idx, float fAmount, HashSet<String> indiv)
	{
		try {
			Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(sGroupName) ;
			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person3 person = aGroup.get(keysPeople.nextElement());
				if ( !(indiv.contains(person.m_name)) ) continue ;
                if (idx == Utils._FR) person.incAmount(Person3.AccountEntry.FROM, fAmount) ; 
                if (idx == Utils._TO) person.incAmount(Person3.AccountEntry.TO, fAmount) ;
				if ((idx == Utils._FR) && (m_bClearing == false)) {
                    person.incAmount(Person3.AccountEntry.IND_PAID, fAmount) ; 
				}
				if ((idx == Utils._TO) && (m_bClearing == false)) {
                    person.incAmount(Person3.AccountEntry.TRANS_AMT, fAmount) ; 
                    person.incAmount(Person3.AccountEntry.IND_SUM, fAmount) ; 
				}
				aGroup.put(person.m_name, person) ;
			}
		} catch (Exception e) {
			System.err.println("Error:putIndivAmount::" + e.getMessage()) ;
		}		
	}

	private HashSet<String> getAllActive(String sGroupName)
	{
		HashSet<String> allSet = new HashSet<String>() ; 
		Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(sGroupName) ;

		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()) {
			Person3 person = aGroup.get(keysPeople.nextElement());
			if (person.m_active == true) allSet.add(person.m_name) ;
		}
		return allSet ; 
	}

	private HashSet<String> getIndivInput(InputProcessor ip)
	{
		HashSet<String> indivInput = new HashSet<String>() ; 
		InputProcessor._WhoFromTo w = ip._Input.get(Utils._INDIV_key) ;
		if (w != null) {
			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator();
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				indivInput.add(na.m_name) ;
			}
		}
		return indivInput ;
	}

	private HashSet<String> getRemActive(HashSet<String> all, HashSet<String> indiv)
	{
		HashSet<String> diff = new HashSet<String>(all) ;
        diff.removeAll(indiv) ;
		return diff ;
	}

	private void processSetAll(String key, String sGroupName, int idx, InputProcessor ip)
	{
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(key) ;
			if (w == null) return ; 

			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ; 
				float fIndivAmt = na.m_amount / w._Count ;
				putIndivAmount(sGroupName, idx, fIndivAmt, getAllActive(sGroupName)) ;
			}
		} catch (Exception e) {
			System.err.println("Error:processSetAll::" + e.getMessage()) ;
		}		
	}

	private void processSetRem(String key, String sGroupName, int idx, InputProcessor ip)
	{
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(key) ;
			if (w == null) return ; 

			HashSet<String> setRem = null ;
			int rSize = 1 ;
			try {	// prepare sets
				setRem = getRemActive(getAllActive(sGroupName), getIndivInput(ip));
				rSize = setRem.size() ;
			}  catch (Exception e) {
				System.err.println("Error:processSetRem::prepareSets::" + e.getMessage()) ;
				return ;
			}
			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ;
				float fIndivAmt = na.m_amount / rSize ;
				putIndivAmount(sGroupName, idx, fIndivAmt, setRem) ;
			}
		} catch (Exception e) {
			System.err.println("Error:processSetRem::" + e.getMessage()) ;
		}		
	}

	private void processSetIndiv(String key, String sGroupName, int idx, InputProcessor ip)
	{
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(key) ;
			if (w == null) return ; 

			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ; 
				float fIndivAmt = na.m_amount ;
				HashSet<String> iSet = new HashSet<String>();
				iSet.add(na.m_name) ;
				putIndivAmount(sGroupName, idx, fIndivAmt, iSet) ;
			}
		} catch (Exception e) {
			System.err.println("Error:processSetIndiv::" + e.getMessage()) ;
		}		
	}

	private void doFromTo2(String item, int idx, float amt, String in, String sGroupName)
	{
		try {
			//System.out.println("\ndoFromTo2: idx = " + idx + ", sIn = " + in + ", sGroupName = " +  sGroupName);

			int numAll = numActive(sGroupName) ;

			InputProcessor sT = new InputProcessor() ;
			sT.processFrTo(item, idx, amt, numAll, in) ;

			processSetAll(Utils._ALL_key, sGroupName, idx, sT) ;
			processSetRem(Utils._REM_key, sGroupName, idx, sT) ;
			processSetIndiv(Utils._INDIV_key, sGroupName, idx, sT) ;
		} catch (Exception e) {
			System.err.println("Error:doFromTo2::" + e.getMessage()) ;
		}		
	}

	// ----------------------------------------------------
	// 	initFromToGroup
	// ----------------------------------------------------
	private void initFromToGroup(String sGroupName)
	{
		Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(sGroupName) ;
		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person3 person = aGroup.get(keysPeople.nextElement());
			person.m_amount.put(Person3.AccountEntry.FROM, 0.0f);
            person.m_amount.put(Person3.AccountEntry.TO, 0.0f);
            person.m_amount.put(Person3.AccountEntry.TRANS_AMT, 0.0f);
            person.m_amount.put(Person3.AccountEntry.CHK_SUM, 0.0f);
            person.m_amount.put(Person3.AccountEntry.CHK_INDSUM, 0.0f);
			aGroup.put(person.m_name, person) ;
		}

		// system inplementation pending
		/*
		iter = m_System.keySet().iterator();
		while(iter.hasNext()){
			Person3 aPer = m_System.get(iter.next()) ;
			aPer.m_amount[aPer.FROM] = aPer.m_amount[aPer.TO] = aPer.m_amount[aPer.TRANS_AMT] = aPer.m_amount[aPer.CHK_SUM] = aPer.m_amount[aPer.CHK_INDSUM] = 0 ;
			m_System.put(aPer.m_name, aPer) ;
		} */
	}

	// ----------------------------------------------------
	// 	initPersons
	// ----------------------------------------------------
	private void initPersons()
	{
		//m_Persons = new Hashtable<String, Person3>() ;
		m_nTotAmount = 0;		m_nSysToAmount = 0 ;

		Utils.m_System3 = new Hashtable<String, Person3>() ;
		Person3 aPerson = new Person3(Utils._SYS, true) ;
		Utils.m_System3.put(Utils._SYS, aPerson) ;
		Utils.m_bSys = false ;
	}

	// ----------------------------------------------------
	// 	sumFromToGroup
	// ----------------------------------------------------
	private float sumFromToGroup(float amt, String action, String sGroupName)
	{
		float sysAmount = 0 ;
		// sys account pending
		/*
		float sysAmount = 0 ;
		Iterator<String> iter = m_System.keySet().iterator();
		while(iter.hasNext()){
			Person3 aPer = m_System.get(iter.next()) ;
			if (aPer.m_active == true) aPer.m_amount[aPer.SYS_SUM] += (aPer.m_amount[aPer.FROM] + ((-1)*aPer.m_amount[aPer.TO])) ;
			sysAmount = aPer.m_amount[aPer.SYS_SUM] ;		m_nSysToAmount += aPer.m_amount[aPer.TO] ;
			m_System.put(aPer.m_name, aPer) ;
		}
		*/

		// person account
		float nCheckSum = 0, nCheckIndSum = 0 ;
		Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(sGroupName) ;
		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person3 person = aGroup.get(keysPeople.nextElement());
            if (person.m_active == true) {
                Float f = person.m_amount.get(Person3.AccountEntry.FROM) ;
                Float t = person.m_amount.get(Person3.AccountEntry.TO) ;
                person.incAmount(Person3.AccountEntry.SYS_SUM, (f + ((-1)*t))) ;

            }
            person.m_amount.put(Person3.AccountEntry.FROM, 0.0f) ; 
            person.m_amount.put(Person3.AccountEntry.TO, 0.0f) ;

			nCheckSum += person.m_amount.get(Person3.AccountEntry.SYS_SUM) ;
			if (!action.endsWith(Utils._CLEARING)) nCheckIndSum += person.m_amount.get(Person3.AccountEntry.IND_SUM) ;
		}
		keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person3 person = aGroup.get(keysPeople.nextElement());
			if (person.m_active == true) person.m_amount.put(Person3.AccountEntry.CHK_SUM, (nCheckSum + sysAmount /* adjust for sys account*/));
			aGroup.put(person.m_name, person) ;
		}

		// individual checksum
		m_nTotAmount += amt ;
		keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person3 person = aGroup.get(keysPeople.nextElement());
			if (!action.endsWith(Utils._CLEARING)) person.m_amount.put(Person3.AccountEntry.CHK_INDSUM, ((m_nTotAmount - m_nSysToAmount) /* this is adjusted for sys account */ - nCheckIndSum)) ;
			aGroup.put(person.m_name, person) ;
		}

		return nCheckSum ;
	}

	// ----------------------------------------------------
	// ProcessTransaction
	// ----------------------------------------------------
	private void ProcessTransaction(String item, String desc, String amt, String from, String to, String group, String action, String def)
	{
		try {
			//System.out.println("ProcessTransaction::" + "item:" + item + ",amt:" + amt + ",from:" + from + ",to:" + to + ",group:" + group + ",action:" + action) ;

			m_bClearing = false ;

			String sGroupName = group ;
			doGroupAction(action) ;

			float xAmt = 0 ;
			try {
				xAmt = Float.parseFloat(amt) ;
			} catch (NumberFormatException e) {
			}
			initFromToGroup(sGroupName) ;

			String aFrom = percentageToAmounts(xAmt, from, action) ;
			String aTo = percentageToAmounts(xAmt, to, action) ;

			doFromTo2(item, Utils._FR, xAmt, aFrom, sGroupName) ;
			doFromTo2(item, Utils._TO, xAmt, aTo, sGroupName) ;
			sumFromToGroup(xAmt, action, sGroupName) ;
		} catch (Exception e){
			System.err.println("Error:ProcessTransaction:" + e.getMessage());
		}
	}


	// dump collection
	private void dumpCollection()
	{
		System.out.println("--------------------------------------");
		Enumeration<String> keysGroup = Utils.m_GroupCollection3.keys();
		while(keysGroup.hasMoreElements()){
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person3> aGroup = Utils.m_GroupCollection3.get(groupName) ;
			System.out.println("");
			System.out.println(groupName);

			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				/* two step get
				String key = keysPeople.nextElement();
				Person3 person = (Person3)aGroup.get(key);*/
				// single step, get
				Person3 person = aGroup.get(keysPeople.nextElement());
				//System.out.println("person: " + person.m_name + ":" + person.m_active);
				//System.out.println("Value of "+key+" is: "+aGroup.get(key));

				String sTransAmt = "", sPerAmt = "", sIndAmt = "", sIndPaid="" ;
				sTransAmt +=  Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person3.AccountEntry.TRANS_AMT)) + Utils.rBr ;
				sPerAmt += Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person3.AccountEntry.SYS_SUM)) + Utils.rBr ;
				sIndAmt += Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person3.AccountEntry.IND_SUM)) + Utils.rBr ;
				sIndPaid += Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person3.AccountEntry.IND_PAID)) + Utils.rBr ;
				System.out.println(person.m_name + ":" + sTransAmt + Utils._DUMP_SEPARATOR + sPerAmt + Utils._DUMP_SEPARATOR + sIndAmt + Utils._DUMP_SEPARATOR + sIndPaid);
			}
		}
		System.out.println("--------------------------------------");
	}

	// getPersons
	Hashtable<String, Person3> getPersons(String sGrpName)
	{
		// find group
		try {
			Hashtable<String, Person3> persons = Utils.m_GroupCollection3.get(sGrpName) ;
			if (persons != null) {
			} else {
				// not found, error !
			}
			return persons ;
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			return null ;
		}
	}

	// Find_CreateGroup
	Hashtable<String, Person3> Find_CreateGroup(String sGrpName)
	{
		// find group
		try {
			Hashtable<String, Person3> aGrp = Utils.m_GroupCollection3.get(sGrpName) ;
			if (aGrp == null) {
				aGrp = new Hashtable<String, Person3>() ;
				Utils.m_GroupCollection3.put(sGrpName, aGrp) ;
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
		if ( ((idS = sA.indexOf(Utils._ID_SEPARATOR)) != -1) )
			sAct = sA.substring(lR+1, idS).trim() ;
		else
			System.err.println("Action not specified: " + sA);

		return sAct ;
	}

	// doGroupAction: process input action
	// name1 (*/+/-:self), name2 (*/+/-:self): add/enable/disable individuals
	// group1 (*/+/-:group): add/enable/disable group
	private String doGroupAction(String action)
	{
		//System.out.println("action: " + action);

		String sGroupName = Utils._DEFAULT_GROUP ;
		boolean bGroup = false, bInd = false ;
		ArrayList<String> grpActions = null, indActions = null ;

		// process Action
		if (action.length() != 0) {
			String[] pieces = action.split(Utils._ITEM_SEPARATOR);
			String sActs = "", sIndAct = Utils.ADD_ITEM, sGrpAct = Utils.ADD_ITEM;
			for (String p : pieces) {
				sActs = p ;

				if (sActs.endsWith(Utils._CLEARING)) {	// pay between individuals
					m_bClearing = true ;
				}

				int lR = 0, rR = 0 ;
				String aName = "", aGroup = Utils._DEFAULT_GROUP ;
				if ( ((lR = sActs.indexOf(Utils._ID_lR)) != -1) && ((rR = sActs.indexOf(Utils._ID_rR)) != -1) ) {	// valid construct: <name> (*:self or group)
						// get name: self or group
						if ( (bInd = sActs.contains(Utils._SELF)) ) {
							aName = sActs.substring(0, lR).trim() ;
							sIndAct = getAction(lR, sActs) ;
						}
						else if ( (bGroup = sActs.contains(Utils._GROUP)) ) {
							aGroup = sActs.substring(0, lR).trim() ;
							sGrpAct = getAction(lR, sActs) ;
						}
						else
							; //System.err.println("Individual or Group not specified: " + action);
				}

				if (bGroup) {
					if (grpActions == null) {
						grpActions = new ArrayList<String>() ;
						grpActions.add(aGroup + Utils._ID_SEPARATOR + sGrpAct) ;
					} else
						grpActions.add(aGroup + Utils._ID_SEPARATOR + sGrpAct) ;
				}

				if (bInd) {
					if (indActions == null) {
						indActions = new ArrayList<String>() ;
						indActions.add(aName + Utils._ID_SEPARATOR + sIndAct) ;
					} else
						indActions.add(aName + Utils._ID_SEPARATOR + sIndAct) ;
				}
			} // while

			// Create Collections
			if (Utils.m_GroupCollection3 == null) Utils.m_GroupCollection3 = new Hashtable<String, Hashtable<String, Person3>>() ;

			if (grpActions != null) {
				for (String aAction : grpActions) {
					int idS = -1 ;
					if ( ((idS = aAction.indexOf(Utils._ID_SEPARATOR)) != -1) ) {
						sGroupName = aAction.substring(0, idS).trim() ;
						sGrpAct = aAction.substring(idS+1, aAction.length()).trim() ;
						Find_CreateGroup(sGroupName) ;
					}
				}
			}

			if (indActions != null) {
				for (String aAction : indActions) {
					int idS = -1 ;
					if ( ((idS = aAction.indexOf(Utils._ID_SEPARATOR)) != -1) ) {
						String sIndName = aAction.substring(0, idS).trim() ;
						sIndAct = aAction.substring(idS+1, aAction.length()).trim() ;

						try {
							Hashtable<String, Person3> aGrp = Find_CreateGroup(sGroupName) ;
							//System.out.println("sGroupName: " + sGroupName);

							Person3 aPn = aGrp.get(sIndName);
							if (aPn != null) { // found, flip enable/disable
								//System.out.println("SEARCH: " + sIndName + " ,FOUND: " + aPn.m_name + ":" + aPn.m_active + ": flip active");
								if (sIndAct.compareToIgnoreCase(Utils.DISABLE_ITEM) == 0) {
									aPn.m_active = false ;
								} else if (sIndAct.compareToIgnoreCase(Utils.ENABLE_ITEM) == 0) {
									aPn.m_active = true ;
								}
								aGrp.put(sIndName, aPn) ;
							} else { // not found, add
								//System.out.println("SEARCH: " + sIndName + ": NOT found, add");
								if (sIndAct.compareToIgnoreCase(Utils.ADD_ITEM) == 0) {
									Person3 aPerson = new Person3(sIndName.trim(), true) ;
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

	// ----------------------------------------------------
	// removeQuotes
	// ----------------------------------------------------
	private String removeQuotes(String inString)
	{
		// strip quotes from inString
		StringBuilder sb = new StringBuilder(inString);
		int q = -1 ;
		while ((q = sb.indexOf(Utils.S_ACTION_QUOTE)) != -1) sb.deleteCharAt(q) ;
		return sb.toString();
	}


	// ----------------------------------------------------
	// ReadAndProcessTransactions
	// ----------------------------------------------------
	public void ReadAndProcessTransactions(String fileName, boolean bExport)
	{
		GPFormatter3 gpF = null ;

		// open logfile
        FileReader fileReader = null;
		try {
			fileReader = new FileReader(getFile(fileName));
			//read file
			BufferedReader buffReader = new BufferedReader(fileReader);
			String sLine = "";

			initPersons() ;
			//if (bExport) m_exportLines = null ;
			if (bExport) {
				//Utils.m_exportLinesGroup = null ;
				gpF = new GPFormatter3() ;
				//gpF.m_gpCollectionToFormat = this.m_GroupCollection ;
			}

			try {
				while ((sLine = buffReader.readLine()) != null) {

				String item="", category="", vendor="", desc="", amt="", from="", to="", group="", action="", def="" ;
				// stream the input, one line at a time
				String[] pieces = sLine.split(Utils._READ_SEPARATOR);
				int pos = 0 ;

				/* control implementation */
					if (sLine.charAt(0) == Utils.CONTROL) { // control record, read
						for (String p : pieces) {
							String sColumn = p ;
							sColumn = sColumn.substring(sColumn.indexOf(Utils.CONTROL) + 1, sColumn.length()) ;
							//System.out.println("sColumn:" + sColumn);

							if (sColumn.compareToIgnoreCase(Utils.S_ITEM) == 0) 		Utils.P_ITEM = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_CATEGORY) == 0) 	Utils.P_CATEGORY = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_VENDOR) == 0) 	Utils.P_VENDOR = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_DESC) == 0) 	Utils.P_DESC = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_AMOUNT) == 0) 	Utils.P_AMOUNT = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_FROM) == 0) 	Utils.P_FROM = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_TO) == 0) 		Utils.P_TO = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_GROUP) == 0)	Utils.P_GROUP = pos++ ;
							else if (sColumn.compareToIgnoreCase(Utils.S_ACTION) == 0) 	Utils.P_ACTION = pos++ ;
							else pos++ ;
						}
						//System.out.println("P_ITEM:" + P_ITEM + ", P_CATEGORY:" + P_CATEGORY + ", P_VENDOR:" + P_VENDOR + ", P_DESC:" + P_DESC + ", P_AMOUNT:" + P_AMOUNT + ", P_FROM:" + P_FROM + ", P_TO:" + P_TO + ", P_GROUP:" + P_GROUP + ", P_ACTION:" + P_ACTION);
						continue ;
					}

					for (String p : pieces) {
						if (pos == Utils.P_ITEM)
							item = p ;
						else if (pos == Utils.P_CATEGORY)
							category = p ;
						else if (pos == Utils.P_VENDOR)
							vendor = p ;
						else if (pos == Utils.P_DESC)
							desc = p ;
						else if (pos == Utils.P_AMOUNT)
							amt = p ;
						else if (pos == Utils.P_FROM) {
							from = p ;
							from = removeQuotes(from) ;
						}
						else if (pos == Utils.P_TO) {
							to = p ;
							to = removeQuotes(to) ;
						}
						else if (pos == Utils.P_GROUP)
							group = p ;
						else if (pos == Utils.P_ACTION) {
							action = p ;
							action = removeQuotes(action) ;
						}
						else def = def + p ;
						pos++ ;
					}

					if (sLine.length() == 0) continue ;
					if (item.charAt(0) == Utils._COMMENT) continue ; // comment, skip

					//System.out.println("item:" + item + ", category:" + category + ", vendor:" + vendor + ", desc:" + desc + ", amt:" + amt + ", from:" + from + ", to:" + to + ", group:" + group + ", action:" + action);

					if (group.length() == 0) group = Utils._DEFAULT_GROUP ;
					ProcessTransaction(item, desc, amt, from, to, group, action, def) ;
					//if (bExport) prepareToExport(item, category, vendor, desc, amt, from, to, action, def) ;
					if (bExport) gpF.prepareToExportGroup(item, category, vendor, desc, amt, from, to, group, action, def) ;

				} // end of while
				buffReader.close() ;
				////System.out.println("map: " + m_Transactions.toString()); // dump HashMap

				//if (bExport) exportToCSV(fileName) ;
				if (bExport) gpF.exportToCSVGroup(fileName) ;

				dumpCollection() ;
			} catch (IOException e) {
				////System.out.println("There was a problem reading:" + fileName);
			}
		} catch (FileNotFoundException e) {
			////System.out.println("Could not locate a file: " + e.getMessage());
		}
	}

} // end of class
