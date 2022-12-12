package GP2.account;

import GP2.input.InputProcessor;
import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.utils.fileUtils;
import GP2.xls._SheetProperties;
import GP2.person.Person;
import GP2.group.GroupProcessor;
import GP2.format.GPFormatter;
import GP2.group.groupCsvJsonMapping;
import GP2.group.csvFileJSON;
import GP2.json.WriteJson;
import GP2.xls.buildXLS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashSet;

public class account extends Object
{
	//CONSTANTS
	//calculation direction
	private final int _FR 	= 0 ;
	private final int _TO	= 1 ;

	// control row
	private final char 	CONTROL 		= '@' ;
	//private final char 	USE_COLUMN	= '+' ;
	//private final char 	SKIP_COLUMN	= '-' ;

	private final String S_ITEM 	= "item" ;
	private final String S_CATEGORY	= "category" ;
	private final String S_VENDOR	= "vendor" ;
	private final String S_DESC		= "description" ;
	private final String S_AMOUNT	= "amount" ;
	private final String S_FROM		= "from" ;
	private final String S_TO		= "to" ;
	private final String S_GROUP	= "group" ;
	private final String S_ACTION	= "action" ;

	private int	P_ITEM ;
	private int	P_CATEGORY ;
	private int P_VENDOR ;
	private int	P_DESC ;
	private int P_AMOUNT ;
	private int	P_FROM ;
	private int P_TO ;
	private int	P_GROUP ;
	private int	P_ACTION ;

	// ----------------------------------------------------
	// Declarations
	// ----------------------------------------------------
	private float m_nTotAmount = 0, m_nSysToAmount ;

	// ----------------------------------------------------
	// Class body
	// ----------------------------------------------------

	// ----------------------------------------------------
	// percentageToAmounts
	// ----------------------------------------------------
	private String percentageToAmounts(float amt, String in, String action)
	{
		if (in.indexOf(Constants._PERCENTAGE) == -1) return in;

		String sOut = "" ;
		String sIn[] = in.split(Constants._ITEM_SEPARATOR) ;

		String eachName = "" ;
		float eachPer = 0 ;
		for (int i = 0; i < sIn.length; i++) {
			eachName = "";	eachPer = 0 ;
			String sEach[] = sIn[i].split(Constants._AMT_INDICATOR) ;
			for (int k = 0; k < sEach.length; k++) {
				int pLoc = -1 ;
				if ((pLoc = sEach[k].indexOf(Constants._PERCENTAGE)) == -1) {
					try {
						eachPer = Float.parseFloat(sEach[k]) ;
						sOut += Constants._AMT_INDICATOR + sEach[k] ;
					} catch (NumberFormatException e) {
						eachName = sEach[k].trim() ;
						if (sOut == "") sOut += sEach[k] ;
						else sOut += Constants._ITEM_SEPARATOR + sEach[k] ;
					}
				} else {
					eachPer = Float.parseFloat(sEach[k].substring(0, pLoc)) ;
					float xAmt = amt * eachPer / 100 ;
					sOut += Constants._AMT_INDICATOR + String.valueOf(xAmt) ;
				}
			}
		}
		return sOut ;
	}


	private void putIndivAmount(String sGroupName, int idx, float fAmount, HashSet<String> indiv)
	{
		try {
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(sGroupName) ;
			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				if ( !(indiv.contains(person.m_name)) ) continue ;
                if (idx == _FR) person.incAmount(Person.AccountEntry.FROM, fAmount) ;
                if (idx == _TO) person.incAmount(Person.AccountEntry.TO, fAmount) ;
				if ((idx == _FR) && (Utils.m_bClearing == false)) {
                    person.incAmount(Person.AccountEntry.IND_PAID, fAmount) ;
				}
				if ((idx == _TO) && (Utils.m_bClearing == false)) {
                    person.incAmount(Person.AccountEntry.TRANS_AMT, fAmount) ;
                    person.incAmount(Person.AccountEntry.IND_SUM, fAmount) ;
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
		Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(sGroupName) ;

		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()) {
			Person person = aGroup.get(keysPeople.nextElement());
			if (person.m_active == true) allSet.add(person.m_name) ;
		}
		return allSet ;
	}

	private HashSet<String> getIndivInput(InputProcessor ip)
	{
		HashSet<String> indivInput = new HashSet<String>() ;
		InputProcessor._WhoFromTo w = ip._Input.get(Constants._INDIV_key) ;
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

	private void doFromTo(String item, int idx, float amt, String in, String sGroupName)
	{
		try {
			int numAll = getAllActive(sGroupName).size() ;
			InputProcessor sT = new InputProcessor() ;
			sT.processFrTo(item, idx, amt, numAll, in) ;

			processSetAll(Constants._ALL_key, sGroupName, idx, sT) ;
			processSetRem(Constants._REM_key, sGroupName, idx, sT) ;
			processSetIndiv(Constants._INDIV_key, sGroupName, idx, sT) ;
		} catch (Exception e) {
			System.err.println("Error:doFromTo::" + e.getMessage()) ;
		}
	}

	// ----------------------------------------------------
	// 	initFromTo
	// ----------------------------------------------------
	private void initFromTo(String sGroupName)
	{
		Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(sGroupName) ;
		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person person = aGroup.get(keysPeople.nextElement());
			person.m_amount.put(Person.AccountEntry.FROM, 0.0f);
            person.m_amount.put(Person.AccountEntry.TO, 0.0f);
            person.m_amount.put(Person.AccountEntry.TRANS_AMT, 0.0f);
            person.m_amount.put(Person.AccountEntry.CHK_SUM, 0.0f);
            person.m_amount.put(Person.AccountEntry.CHK_INDSUM, 0.0f);
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
		m_nTotAmount = 0;		m_nSysToAmount = 0 ;
		Utils.m_System = new Hashtable<String, Person>() ;
		Person aPerson = new Person(Constants._SYS, true) ;
		Utils.m_System.put(Constants._SYS, aPerson) ;
		Utils.m_bSys = false ;
	}

	// ----------------------------------------------------
	// 	sumFromTo
	// ----------------------------------------------------
	private float sumFromTo(float amt, String action, String sGroupName)
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
		Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(sGroupName) ;
		Enumeration<String> keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person person = aGroup.get(keysPeople.nextElement());
            if (person.m_active == true) {
                Float f = person.m_amount.get(Person.AccountEntry.FROM) ;
                Float t = person.m_amount.get(Person.AccountEntry.TO) ;
                person.incAmount(Person.AccountEntry.SYS_SUM, (f + ((-1)*t))) ;

            }
            person.m_amount.put(Person.AccountEntry.FROM, 0.0f) ;
            person.m_amount.put(Person.AccountEntry.TO, 0.0f) ;

			nCheckSum += person.m_amount.get(Person.AccountEntry.SYS_SUM) ;
			if (!action.endsWith(Constants._CLEARING)) nCheckIndSum += person.m_amount.get(Person.AccountEntry.IND_SUM) ;
		}
		keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person person = aGroup.get(keysPeople.nextElement());
			if (person.m_active == true) person.m_amount.put(Person.AccountEntry.CHK_SUM, (nCheckSum + sysAmount /* adjust for sys account*/));
			aGroup.put(person.m_name, person) ;
		}

		// individual checksum
		m_nTotAmount += amt ;
		keysPeople = aGroup.keys();
		while(keysPeople.hasMoreElements()){
			Person person = aGroup.get(keysPeople.nextElement());
			if (!action.endsWith(Constants._CLEARING)) person.m_amount.put(Person.AccountEntry.CHK_INDSUM, ((m_nTotAmount - m_nSysToAmount) /* this is adjusted for sys account */ - nCheckIndSum)) ;
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
			Utils.m_bClearing = false ;

			String sGroupName = group ;
			GroupProcessor gp = new GroupProcessor() ;
			gp.doGroupAction(action) ;

			float xAmt = 0 ;
			try {
				xAmt = Float.parseFloat(amt) ;
			} catch (NumberFormatException e) {
			}
			initFromTo(sGroupName) ;

			String aFrom = percentageToAmounts(xAmt, from, action) ;
			String aTo = percentageToAmounts(xAmt, to, action) ;

			doFromTo(item, _FR, xAmt, aFrom, sGroupName) ;
			doFromTo(item, _TO, xAmt, aTo, sGroupName) ;
			sumFromTo(xAmt, action, sGroupName) ;
		} catch (Exception e){
			System.err.println("Error:ProcessTransaction:" + e.getMessage());
		}
	}


	// dump collection
	private void dumpCollection()
	{
		System.out.println("--------------------------------------");
		Enumeration<String> keysGroup = Utils.m_GroupCollection.keys();
		while(keysGroup.hasMoreElements()){
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(groupName) ;
			System.out.println("");
			System.out.println(groupName);

			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				String sTransAmt = "", sPerAmt = "", sIndAmt = "", sIndPaid="" ;
				sTransAmt +=  Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANS_AMT)) + Constants.rBr ;
				sPerAmt += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SYS_SUM)) + Constants.rBr ;
				sIndAmt += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_SUM)) + Constants.rBr ;
				sIndPaid += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_PAID)) + Constants.rBr ;
				System.out.println(person.m_name + ":" + sTransAmt + Constants._DUMP_SEPARATOR + sPerAmt + Constants._DUMP_SEPARATOR + sIndAmt + Constants._DUMP_SEPARATOR + sIndPaid);
			}
		}
		System.out.println("--------------------------------------");
	}

	// ----------------------------------------------------
	// ReadAndProcessTransactions
	// ----------------------------------------------------
	public void ReadAndProcessTransactions(String fileName)
	{
		Utils.m_GroupCollection = null ;
		GPFormatter gpF = null ;
        FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileUtils.getFile(fileName));
			BufferedReader buffReader = new BufferedReader(fileReader);
			String sLine = "";

			initPersons() ;
			if (Utils.m_settings.getExportToUse()) gpF = new GPFormatter() ;

			try {
				while ((sLine = buffReader.readLine()) != null) {
					String item="", category="", vendor="", desc="", amt="", from="", to="", group="", action="", def="" ;
					// stream the input, one line at a time
					String[] pieces = sLine.split(Constants._READ_SEPARATOR);
					int pos = 0 ;
					/* control implementation */
					if (sLine.charAt(0) == CONTROL) { // control record, read
						for (String p : pieces) {
							String sColumn = p ;
							sColumn = sColumn.substring(sColumn.indexOf(CONTROL) + 1, sColumn.length()) ;
							if (sColumn.compareToIgnoreCase(S_ITEM) == 0) 			P_ITEM = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_CATEGORY) == 0) 	P_CATEGORY = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_VENDOR) == 0) 	P_VENDOR = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_DESC) == 0) 		P_DESC = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_AMOUNT) == 0) 	P_AMOUNT = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_FROM) == 0) 		P_FROM = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_TO) == 0) 		P_TO = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_GROUP) == 0)		P_GROUP = pos++ ;
							else if (sColumn.compareToIgnoreCase(S_ACTION) == 0) 	P_ACTION = pos++ ;
							else pos++ ;
						}
						continue ;
					}

					for (String p : pieces) {
						if (pos == P_ITEM)					item = p ;
						else if (pos == P_CATEGORY)			category = p ;
						else if (pos == P_VENDOR)			vendor = p ;
						else if (pos == P_DESC)				desc = p ;
						else if (pos == P_AMOUNT)			amt = p ;
						else if (pos == P_FROM) {
							from = p ;
							from = Utils.removeQuotes(from) ;
						}
						else if (pos == P_TO) {
							to = p ;
							to = Utils.removeQuotes(to) ;
						}
						else if (pos == P_GROUP)			group = p ;
						else if (pos == P_ACTION) {
							action = p ;
							action = Utils.removeQuotes(action) ;
						}
						else def = def + p ;
						pos++ ;
					}
					if (sLine.length() == 0) continue ;
					if (item.charAt(0) == Constants._COMMENT) continue ; // comment, skip

					//System.out.println("item:" + item + ", category:" + category + ", vendor:" + vendor + ", desc:" + desc + ", amt:" + amt + ", from:" + from + ", to:" + to + ", group:" + group + ", action:" + action);
					if (group.length() == 0) group = Constants._DEFAULT_GROUP ;
					ProcessTransaction(item, desc, amt, from, to, group, action, def) ;
					if (Utils.m_settings.getExportToUse())
						gpF.prepareToExportGroup(item, category, vendor, desc, amt, from, to, group, action, def) ;
				} // end of while
				buffReader.close() ;

				buildGroupCsvJsonMap(fileName) ;
				if (Utils.m_settings.getExportToUse()) gpF.exportToCSVGroup(fileName) ;
			} catch (IOException e) {
				System.out.println("There was a problem reading:" + fileName);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not locate a file: " + e.getMessage());
		}
	}

	//filename=sep.mint.csv		nType(0)=default.sep.mint.out.csv		nType(1)=default.sep.mint.out.json
	private String makeOutFileName (int nType, String sGroupName, String fileName) {
		String outFilename = "" ;
		String sExt = (nType == 0 ? Constants.OUT_EXTENSION : Constants.OUT_JSON_EXTENSION) ;
		int fileExt = fileName.lastIndexOf(Constants.OUT_FILESEP) ;
		if (fileExt == -1) // not found
			outFilename += sExt ;
		else
			outFilename = sGroupName + Constants.OUT_FILESEP + fileName.substring(0, fileExt) + Constants.OUT_FILE + sExt ;
		return outFilename ;
	}

	private groupCsvJsonMapping buildGroupCsvJsonMap(String csvFileName) {
		Enumeration<String> keysGroup = Utils.m_GroupCollection.keys();
		while(keysGroup.hasMoreElements()){
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(groupName) ;

			String gCSVFile = null, sCSVJSON = null ;
			csvFileJSON csvFile = null ;
			_SheetProperties sp = new _SheetProperties() ;

			if (Utils.m_settings.getExportToUse()) gCSVFile = makeOutFileName(0, groupName, csvFileName);
			if (Utils.m_settings.getJsonToUse()) {
				sCSVJSON = makeOutFileName(1, groupName, csvFileName);
				csvFile = new csvFileJSON() ;
			}

			//add to map
			if (Utils.m_grpCsvJsonMap == null) Utils.m_grpCsvJsonMap = new groupCsvJsonMapping();
			Utils.m_grpCsvJsonMap.addItem(groupName, gCSVFile, sCSVJSON, csvFile, sp);
		}
		return Utils.m_grpCsvJsonMap ;
	}

	private boolean writeJsonFiles() {
		if (Utils.m_grpCsvJsonMap == null) return false ;
		for(String key: Utils.m_grpCsvJsonMap._groupMap.keySet()) {
			groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key) ;
			WriteJson jFileW = new WriteJson() ;
			//cj.dumpCollection();
			jFileW.writeJSON(cj._sCSVJSONFile, cj._oCSVFileJSON) ;
		}
		return true ;
	}

	private String writeMapFile(String fName) {
		String mapFile = Utils.m_settings.getMapFileToUse(fName) ;
		WriteJson jFileW = new WriteJson() ;
		mapFile = jFileW.writeJSONMapFile(mapFile, fName, Utils.m_grpCsvJsonMap) ;
		return mapFile;
	}

	public void writeJson(String fileName) {
        if (!Utils.m_settings.getExportToUse()) return ;

		if (Utils.m_settings.getJsonToUse()) {
			boolean b = writeJsonFiles() ;
			String mapFile = writeMapFile(fileName) ;
		}
	}

	public void buildXLS(String fName) {
        if (!Utils.m_settings.getExportToUse()) return ;
		if (!Utils.m_settings.getPropertyXLS().IsPropertyUsed()) return ;

		buildXLS xls = new buildXLS();
		File f = xls.InitializeXLS(fName);
		if (f == null) return ;

		if (Utils.m_settings.getPropertyXLS().IsPropertyUsed()) {
			if (Utils.m_settings.getJsonToUse())
				xls.readFromJSON(fName, f) ;
			else
				xls.readFromMap(fName, f) ;
		}
    }
} // end of class
