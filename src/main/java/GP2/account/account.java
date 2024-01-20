package GP2.account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Hashtable;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import GP2.format.GPFormatter;
import GP2.group.GroupProcessor;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.json.WriteJson;
import GP2.person.GPAction.TransactionType;
import GP2.person.GPAction.TransactionType.TType;
import GP2.person.Person;
import GP2.transaction.Transaction;
import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xls._SheetProperties;
import GP2.xls.buildXLS;

public class account {
	public boolean ProcessTransaction(int lNo, String item, String desc, String amt, String from, String to, String group, String action) {
		try {
			//System.out.println(lNo + ":" + item + ":" + desc + ":" + amt + ":" + from + ":" + to + ":" + group + ":" + action);

			TransactionType.TType tt[] = {TransactionType.TType.Normal};
			GroupProcessor gp2 = new GroupProcessor() ;
			boolean bSkip = gp2.doGroupAction(action, group, tt) ;
			if (bSkip) return false; // return false = skip processing

			if (!Utils.m_Groups.get(group).m_gState.getActive()) return false ;  // return false = skip processing

			Transaction transaction = new Transaction(group, from, to, amt, tt[0]);
			transaction.init();
			transaction.process();
			transaction.sum();

			return true;
		} catch (Exception e){
			System.err.println("Error:ProcessTransaction: processing << line# [" + lNo + "][" + item + "][" + desc + "][" + amt + "] >>:" + e.getMessage());
            return false;
		}
    }

	// dump collection
	private void dumpCollection()
	{
		System.out.println("--------------------------------------");
		Enumeration<String> keysGroup = Utils.m_Groups.keys();
		while(keysGroup.hasMoreElements()){
			String sGroupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(sGroupName).getCollection() ;
			System.out.println("");
			System.out.println(sGroupName);

			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				String sTransAmt = "", sPerAmt = "", sIndAmt = "", sIndPaid="" ;
				sTransAmt +=  Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANSACTION)) + Constants.rBr ;
				sPerAmt += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.OWE_OWED)) + Constants.rBr ;
				sIndAmt += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SPENT)) + Constants.rBr ;
				sIndPaid += Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.PAID)) + Constants.rBr ;
				System.out.println(person.m_name + ":" + sTransAmt + Constants._DUMP_SEPARATOR + sPerAmt + Constants._DUMP_SEPARATOR + sIndAmt + Constants._DUMP_SEPARATOR + sIndPaid);
			}
		}
		System.out.println("--------------------------------------");
	}

	private boolean skipLine(String d) {
		if (d.length() == 0) return true ; // empty line, skip

		String firstChar = String.valueOf(d.charAt(0));
		TType t1 = TransactionType.TType.byValue(firstChar);
		if ( (t1 != null) && (t1.compareTo(TransactionType.TType.Skip) == 0) ) return true; // comment, skip

		return false ;
	}

	private Class getSourceClass() {
		//return mint.class ;
		return ffv.class ;
	}

	public void ReadAndProcessTransactions2(String fileName) {
		Utils.m_Groups = null ;
		GPFormatter gpF = null ;
		if (Utils.m_settings.getExportToUse()) gpF = new GPFormatter() ;
		int lNo = 0 ;

		Class<source> sourceClass = getSourceClass() ;

		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		ObjectReader oReader = csvMapper.reader(sourceClass).with(schema);

		try (Reader reader = new FileReader(fileName)) {
			MappingIterator<source> mi = oReader.readValues(reader);
			while (mi.hasNext()) {
				lNo++ ;
				source current = mi.next();
				if (skipLine(current.getDate()) == true) continue ;

				String date = current.getDate() ;
				String category = current.getCategory() ;
				String vendor = "";//current.getVendor() ;				// mint | ffv (added for compatibility)
				String desc = current.getDescription() ;
				String amt = current.getAmount() ;
				String from = current.getFrom() ;
				String to = current.getTo() ;
				String group = current.getGroup() ;
				String action = current.getAction() ;

				//System.out.println("item:" + date + ", category:" + category + ", vendor:" + vendor + ", desc:" + desc + ", amt:" + amt + ", from:" + from + ", to:" + to + ", group:" + group + ", action:" + action);
				if (group.length() == 0) group = Constants._DEFAULT_GROUP ;
				if (ProcessTransaction(lNo, date, desc, amt, from, to, group, action) == false) continue;
				if (Utils.m_settings.getExportToUse())
					gpF.prepareToExportGroup(date, category, vendor, desc, amt, from, to, group, action) ;
			}
			buildGroupCsvJsonMap(fileName) ;
			if (Utils.m_settings.getExportToUse()) gpF.exportToCSVGroup(fileName) ;
		} catch (FileNotFoundException e) {
			System.out.println("Could not locate a file: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
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
		if (Utils.m_Groups == null) return null ;

		Enumeration<String> keysGroup = Utils.m_Groups.keys();
		while(keysGroup.hasMoreElements()){
			String sGroupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(sGroupName).getCollection() ;

			String gCSVFile = null, sCSVJSON = null ;
			csvFileJSON csvFile = null ;
			_SheetProperties sp = new _SheetProperties() ;

			if (Utils.m_settings.getExportToUse()) gCSVFile = makeOutFileName(0, sGroupName, csvFileName);
			if (Utils.m_settings.getJsonToUse()) {
				sCSVJSON = makeOutFileName(1, sGroupName, csvFileName);
				csvFile = new csvFileJSON() ;
			}

			//add to map
			if (Utils.m_grpCsvJsonMap == null) Utils.m_grpCsvJsonMap = new groupCsvJsonMapping();
			Utils.m_grpCsvJsonMap.addItem(sGroupName, gCSVFile, sCSVJSON, csvFile, sp);
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
		if (Utils.m_grpCsvJsonMap == null) return null ;
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
}