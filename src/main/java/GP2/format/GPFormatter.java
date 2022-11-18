package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.person.Person;
import GP2.xls._SheetProperties;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.apache.commons.io.FileUtils ;
import java.io.FileOutputStream ;
import java.io.File;

public class GPFormatter {
	// output headers
	private final String H_TRANSACTION_AMOUNTS	= "transaction amounts" ;
	private final String H_OWE					= "(you owe) / owed to you" ;
	private final String H_INDIVIDUAL_TOTALS	= "individual \"spent\"" ;
	private final String H_ITEM					= "Item" ;
	private final String H_CATEGORY				= "Category" ;
	private final String H_VENDOR				= "Vendor" ;
	private final String H_DESCRIPTION			= "Description" ;
	private final String H_AMOUNT				= "Amount" ;
	private final String H_FROM					= "From" ;
	private final String H_TO					= "To" ;
	private final String H_ACTION				= "Action" ;
	private final String H_CHECKSUM				= "CheckSum" ;
	private final String H_INDCHECKSUM			= "IndCheckSum" ;
	private final String H_INDIVIDUAL_PAID		= "individual \"paid\"" ;

	//public Hashtable<String, Hashtable<String, Person3>> m_gpCollectionToFormat = null ;
	private Hashtable<String, ArrayList<String>> m_exportLinesGroup = null ;
	//private xls._SheetProperties m_SheetProperties = new _SheetProperties() ;


	public void	GPFormatter() {
		//m_gpCollectionToFormat = null ;
		m_exportLinesGroup = null ;
	}

	/*
	public void	GPFormatter(Hashtable<String, Hashtable<String, Person3>> gpF) {
		//this.m_gpCollectionToFormat = gpF ;
		m_exportLinesGroup = null ;
	}*/


    // ----------------------------------------------------
	// prepareToExportGroup
	// ----------------------------------------------------
	public void prepareToExportGroup(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action, String def)
	{
		String	sTabs = Constants._TAB_SEPARATOR ;
		String aLine ;
		aLine = item + Constants._TAB_SEPARATOR + category + Constants._TAB_SEPARATOR + vendor + Constants._TAB_SEPARATOR + desc + Constants._TAB_SEPARATOR + amt + Constants._TAB_SEPARATOR + from + Constants._TAB_SEPARATOR + to + Constants._TAB_SEPARATOR + action ;
		////System.out.println("aLine = " + aLine);
		//System.out.println("prepareToExportGroup::item = " + item);

		Iterator<String> iter ;
		String	sSysAmt = "" ;
		// sys account
		/*
		String	sSysAmt = "" ;
		if (m_bSys == true) {
			iter = m_System.keySet().iterator();
			while(iter.hasNext()) {
				Person3 aPer = m_System.get(iter.next()) ;
				sSysAmt += lBr + aPer.m_name + Constants._AMT_INDICATOR + roundAmount(aPer.m_amount[aPer.SYS_SUM]) + rBr ;
			}
			sSysAmt = Constants.lPAD + sSysAmt + Constants.rPAD + Constants._TAB_SEPARATOR;
		}
		//sSysAmt = Constants.lPAD + sSysAmt + Constants.rPAD  + Constants._TAB_SEPARATOR ;
		////System.out.println("sSysAmt = " + sSysAmt);
		*/

		// person account
		String	sPerAmt = "", sIndAmt = "", sTransAmt = "", sIndPaid = "" ;
		float cs = 0, indcs = 0 ;
		/* sort persons, :get the iterator & sort it */
		Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(group) ;
		List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
		Collections.sort(mapKeys);
	    iter = mapKeys.iterator();
		while (iter.hasNext()) {
			Person person = aGroup.get(iter.next());
			sTransAmt += Constants._ITEM_SEPARATOR + Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANS_AMT)) + Constants.rBr ;
			sPerAmt += Constants._ITEM_SEPARATOR + Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SYS_SUM)) + Constants.rBr ;
			sIndAmt += Constants._ITEM_SEPARATOR + Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_SUM)) + Constants.rBr ;
			sIndPaid += Constants._ITEM_SEPARATOR + Constants.lBr + person.m_name + Constants._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_PAID)) + Constants.rBr ;
			cs = person.m_amount.get(Person.AccountEntry.CHK_SUM) ;
			indcs = person.m_amount.get(Person.AccountEntry.CHK_INDSUM) ;
		}
		//System.out.println("prepareToExportGroup::sIndPaid = " + sIndPaid);

		sTransAmt = Constants.lPAD + sTransAmt.substring(Constants._ITEM_SEPARATOR.length(), sTransAmt.length()) + Constants.rPAD ;
		sPerAmt = Constants.lPAD + sPerAmt.substring(Constants._ITEM_SEPARATOR.length(), sPerAmt.length()) + Constants.rPAD ;
		sIndAmt = Constants.lPAD + sIndAmt.substring(Constants._ITEM_SEPARATOR.length(), sIndAmt.length()) + Constants.rPAD ;
		sIndPaid = Constants.lPAD + sIndPaid.substring(Constants._ITEM_SEPARATOR.length(), sIndPaid.length()) + Constants.rPAD ;
		//System.out.println("prepareToExportGroup::sIndPaid = " + sIndPaid);

		aLine += Constants._TAB_SEPARATOR + sTransAmt + Constants._TAB_SEPARATOR + sSysAmt /*+ Constants._TAB_SEPARATOR*/ + sPerAmt + Utils.roundAmount(cs)+ Constants._TAB_SEPARATOR + sIndAmt + Utils.roundAmount(indcs) + Constants._TAB_SEPARATOR + sIndPaid  ;

		// Create Collections
		if (m_exportLinesGroup == null) m_exportLinesGroup = new Hashtable<String, ArrayList<String>>() ;
		ArrayList<String> aGrp = m_exportLinesGroup.get(group) ;
		if (aGrp == null) {
			aGrp = new ArrayList<String>() ;
			aGrp.add(aLine) ;
			m_exportLinesGroup.put(group, aGrp) ;
		} else {
			aGrp.add(aLine) ;
		}
	}

	private String padAmountString(String sNames[], String embededPers, String unpaddedLine)
	{
		try {
			//System.out.println("sNames: " + sNames + ", embededPers: " + embededPers + ", unpaddedLine:[" + unpaddedLine + "]");

			for (int i = 0; i < sNames.length; i++) {
				int fPos = embededPers.indexOf(sNames[i]) ;
				//System.out.println("sNames[i]: " + sNames[i]);
				//System.out.println("fPos: " + fPos);
				if (fPos == -1) /* not found */ {
					unpaddedLine = unpaddedLine.replaceFirst(sNames[i], "") ;
					//System.out.println("unpaddedLine: " + unpaddedLine);
				} else {		/* found */
					int tPos = embededPers.indexOf(Constants.rBr, fPos) ;
					//System.out.println("tPos: " + tPos);
					String sAmt = embededPers.substring(fPos + sNames[i].length() + Constants._AMT_INDICATOR.length(), tPos) ;
					//System.out.println("sAmt: " + sAmt);
					unpaddedLine = unpaddedLine.replaceFirst(sNames[i], sAmt) ;
					//System.out.println("unpaddedLine: " + unpaddedLine);
				}
			}
			//System.out.println("unpaddedLine:[" + unpaddedLine + "]" + unpaddedLine.length());
			return unpaddedLine ;
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			return unpaddedLine ;
		}
	}

	private String padHeader(int nTabs)
	{
		String xHeader = "" ;
		while (nTabs > 0) {
				xHeader += Constants._TAB_SEPARATOR ;
				nTabs-- ;
		}
		return xHeader ;
	}

	private int getLocationOf(String s, String sSearchString, int mCol[]) {
		String aArray[] = s.split(Constants._TAB_SEPARATOR) ;
		mCol[0] = aArray.length;
		//m_SheetProperties.maxColums = aArray.length ;
		for (int i = 0; i < aArray.length; i++) {
			if (aArray[i].indexOf(sSearchString) != -1) return (i+1);
		}
		return -1 ;
	}

	private void exportToCSV(String fileName, String group, _SheetProperties sp)
	{
		String outFilename = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile ;
		//System.out.println("outFilename: " + outFilename + " ,outFilename0: " + outFilename0);

		try {
			//added XLS integration
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;

			// Create file
			FileWriter fstream = fw;
			BufferedWriter out = new BufferedWriter(fstream);

			// begin Header {
			String	sTabs = "" ; //Constants._TAB_SEPARATOR ;
			String	sPersons = "" ;

			// sheetProperties ------------
			String personToSearch = "" ;
			int rowCounter = 0, headerCounter = 0 ;
			// sheetProperties ------------

			/* sort persons, :get the iterator & sort it */
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(group) ;
			List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
			Collections.sort(mapKeys);
			Iterator<String> iter = mapKeys.iterator();
			while (iter.hasNext()) {
				Person person = aGroup.get(iter.next());
				sPersons += sTabs + person.m_name ;
				sTabs = Constants._TAB_SEPARATOR ;

				// sheetProperties ------------
				// get first name
				if (personToSearch.equalsIgnoreCase("")) personToSearch = person.m_name ;
				// sheetProperties ------------
			}

			String sHeader0, sHeader01, sHeader02, sHeader03, sHeader ;
			sHeader01 = H_TRANSACTION_AMOUNTS + padHeader(aGroup.size()) ;
			if (Utils.m_bSys == true)
				//sHeader02 = H_OWE + padHeader(m_Persons.size() + 1 + 1 /*CheckSum*/) ;
				sHeader02 = H_OWE + padHeader(aGroup.size() + 1 + 1 /*CheckSum*/) ;
			else
				sHeader02 = H_OWE + padHeader(aGroup.size() + 1 /*CheckSum*/) ;
			sHeader03 = H_INDIVIDUAL_TOTALS + padHeader(aGroup.size() - 1 + 1 /*IndCheckSum*/) + Constants._TAB_SEPARATOR + H_INDIVIDUAL_PAID ;
			sHeader0 = padHeader(2 /* v15 */ + 6) + sHeader01 + sHeader02 + sHeader03 ;
			sHeader = H_ITEM + Constants._TAB_SEPARATOR + H_CATEGORY + Constants._TAB_SEPARATOR + H_VENDOR + Constants._TAB_SEPARATOR + H_DESCRIPTION + Constants._TAB_SEPARATOR + H_AMOUNT + Constants._TAB_SEPARATOR + H_FROM + Constants._TAB_SEPARATOR + H_TO + Constants._TAB_SEPARATOR + H_ACTION ;

			if (Utils.m_bSys == true)
				sHeader += Constants._TAB_SEPARATOR  + sPersons + Constants._TAB_SEPARATOR + Constants._SYS  + Constants._TAB_SEPARATOR + sPersons + Constants._TAB_SEPARATOR + H_CHECKSUM + Constants._TAB_SEPARATOR + sPersons + Constants._TAB_SEPARATOR + H_INDCHECKSUM + Constants._TAB_SEPARATOR + sPersons;
			else
				sHeader += Constants._TAB_SEPARATOR /*Constants._SYS + Constants._TAB_SEPARATOR*/ + sPersons + Constants._TAB_SEPARATOR + sPersons + Constants._TAB_SEPARATOR + H_CHECKSUM + Constants._TAB_SEPARATOR + sPersons + Constants._TAB_SEPARATOR + H_INDCHECKSUM + Constants._TAB_SEPARATOR + sPersons ;
			// sheetProperties ------------
			out.write(sHeader0);	out.newLine();				headerCounter++ ;
			out.write(sHeader);		out.newLine();				headerCounter++ ;

			// sheetProperties ------------
			sp.setCsvFileName(outFilename) ;
			sp.setlHeaders(headerCounter) ;

			int maxCol[] = {0};
			int amountLocation = getLocationOf(sHeader, H_AMOUNT, maxCol) ;
			sp.maxColums = maxCol[0];

			int pivotColumnStart = -1, pivotColumnEnd = -1 ;
			int personLocation = getLocationOf(sHeader, personToSearch, maxCol) ;
			if (personLocation != -1) {
				sp.maxColums = maxCol[0];
				pivotColumnStart = personLocation ;
				pivotColumnEnd = pivotColumnStart + (aGroup.size()-1);
			}
			sp.amountLocation = amountLocation ;
			sp.personLocation = personLocation ;
			sp.pivotColumnStart = pivotColumnStart ;
			sp.pivotColumnEnd = pivotColumnEnd ;
			// sheetProperties ------------

			// } end Header

			sPersons += Constants._TAB_SEPARATOR ;
			//System.out.println("sPersons: " + sPersons);

			ArrayList<String> exportLines = m_exportLinesGroup.get(group) ;
			for (String aLine : exportLines) {
				//String unpaddedLine = sPersons.substring(Utils._TAB_SEPARATOR.length(), sPersons.length()) ;
				String unpaddedLine = sPersons.substring(0, sPersons.length()) ;
				//System.out.println("unpaddedLine:[" + unpaddedLine + "]");
				String sNames[] = unpaddedLine.split(Constants._TAB_SEPARATOR) ;

				String debugLine ;
				// trans amount
				int fx = aLine.indexOf(Constants.lPAD) + Constants.lPAD.length() ;
				int tx = aLine.indexOf(Constants.rPAD, fx) ;
				String embededPersonsx = aLine.substring(fx, tx).trim() ;
				String sNewLinex = padAmountString(sNames, embededPersonsx, unpaddedLine) ;
				debugLine = "fx = " + fx + ", tx = " + tx + ", embededPersonsx = " + embededPersonsx + ", sNewLinex = [" + sNewLinex + "]" ;
				//out.write(debugLine);		out.newLine();

				// sys amount
				int f0 = 0, t0 = 0 ;
				String sNewLine0 = "" ;
				if (Utils.m_bSys == true) {
					f0 = aLine.indexOf(Constants.lPAD, tx + Constants.rPAD.length()) + Constants.lPAD.length() ;
					t0 = aLine.indexOf(Constants.rPAD, f0) ;
					String embededPersons0 = aLine.substring(f0, t0).trim() ;
					String[] sysAC = new String[] { Constants._SYS };
					sNewLine0 = padAmountString(sysAC, embededPersons0, Constants._SYS) ;
					debugLine = "f0 = " + f0 + ", t0 = " + t0 + ", embededPersons0 = " + embededPersons0 + ", sNewLine0 = [" + sNewLine0 + "]" ;
					//out.write(debugLine);		out.newLine();
				} else {
					f0 = fx ;
					t0 = tx ;
				}

				// person amounts
				int f1 = aLine.indexOf(Constants.lPAD, t0 + Constants.rPAD.length()) + Constants.lPAD.length() ;
				int t1 = aLine.indexOf(Constants.rPAD, f1) ;
				String embededPersons1 = aLine.substring(f1, t1).trim() ;
				String sNewLine1 = padAmountString(sNames, embededPersons1, unpaddedLine) ;
				debugLine = "f1 = " + f1 + ", t1 = " + t1 + ", embededPersons1 = " + embededPersons1 + ", sNewLine1 = [" + sNewLine1 + "]" ;

				// individual amounts
				int f2 = aLine.indexOf(Constants.lPAD, t1 + Constants.rPAD.length()) + Constants.lPAD.length() ;
				int t2 = aLine.indexOf(Constants.rPAD, f2) ;
				String embededPersons2 = aLine.substring(f2, t2).trim() ;
				String sNewLine2 = padAmountString(sNames, embededPersons2, unpaddedLine) ;
				debugLine = "f2 = " + f2 + ", t2 = " + t2 + ", embededPersons2 = " + embededPersons2 + ", sNewLine2 = [" + sNewLine2 + "]" ;

				//checksum
				int f11 = t1 + Constants.rPAD.length() ;
				int t11 = aLine.indexOf(Constants.lPAD, f11) ;
				debugLine = "f11 = " + f11 + ", t11 = " + t11;
				String checkSum = aLine.substring(f11, t11).trim() ;

				//indchecksum
				int f21 = t1 + Constants.rPAD.length() ;
				int t21 = aLine.indexOf(Constants.lPAD, f21) ;
				debugLine = "f21 = " + f21 + ", t21 = " + t21;
				String indcheckSum = aLine.substring(f21, t21).trim() ;

				//individual paid
				int f3 = aLine.indexOf(Constants.lPAD, t21 + Constants.rPAD.length()) + Constants.lPAD.length() ;
				int t3 = aLine.indexOf(Constants.rPAD, f3) ;
				String embededPersons3 = aLine.substring(f3, t3).trim() ;
				String sNewLine3 = padAmountString(sNames, embededPersons3, unpaddedLine) ;
				debugLine = "f3 = " + f3 + ", t3 = " + t3 + ", embededPersons3 = " + embededPersons3 + ", sNewLine3 = [" + sNewLine3 + "]" ;
				//System.out.println("debugLine: " + debugLine);

				String sToFile = "" ;
				String outLine = "" ;

				outLine += aLine.substring(0, fx - Constants.lPAD.length()) + sNewLinex ;
				if (Utils.m_bSys == true) outLine += sNewLine0  + Constants._TAB_SEPARATOR ;
				outLine += sNewLine1 + checkSum;
				outLine += Constants._TAB_SEPARATOR + sNewLine2 + indcheckSum ;
				outLine += Constants._TAB_SEPARATOR + sNewLine3 ;
				sToFile = outLine ;

				out.write(sToFile);		out.newLine();

				// sheetProperties ------------
				rowCounter++ ;
				// sheetProperties ------------
			}
			out.close();		//Close the output stream

			// sheetProperties ------------
			sp.maxRows = rowCounter ;
			sp.build() ;
			// sheetProperties ------------

			// close all file handles
			out.close() ;
			fstream.close() ;
			fw.close() ;
			foS.close() ;
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	public void exportToCSVGroup(String fileName)
	{
		_SheetProperties sp = null ;
		Enumeration<String> keysGroup = Utils.m_GroupCollection.keys();
		while(keysGroup.hasMoreElements()) {
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(groupName) ;

			sp = new _SheetProperties() ;
			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				exportToCSV(fileName, groupName, sp) ;
			}

			// update map
			groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(groupName) ;
			csvFileJSON csvFile = new csvFileJSON() ;
			csvFile = sp.toCsvFileJSON();
			Utils.m_grpCsvJsonMap.addItem(groupName, cj._sCSVFile, cj._sCSVJSONFile, csvFile, sp) ;
		}
	}

}
