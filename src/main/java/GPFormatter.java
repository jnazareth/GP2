import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

public class GPFormatter {

	//public Hashtable<String, Hashtable<String, Person3>> m_gpCollectionToFormat = null ;
	private Hashtable<String, ArrayList<String>> m_exportLinesGroup = null ;

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
		String	sTabs = Utils._TAB_SEPARATOR ;
		String aLine ;
		aLine = item + Utils._TAB_SEPARATOR + category + Utils._TAB_SEPARATOR + vendor + Utils._TAB_SEPARATOR + desc + Utils._TAB_SEPARATOR + amt + Utils._TAB_SEPARATOR + from + Utils._TAB_SEPARATOR + to + Utils._TAB_SEPARATOR + action ;
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
				sSysAmt += lBr + aPer.m_name + Utils._AMT_INDICATOR + roundAmount(aPer.m_amount[aPer.SYS_SUM]) + rBr ;
			}
			sSysAmt = Utils.lPAD + sSysAmt + Utils.rPAD + Utils._TAB_SEPARATOR;
		}
		//sSysAmt = Utils.lPAD + sSysAmt + Utils.rPAD  + Utils._TAB_SEPARATOR ;
		////System.out.println("sSysAmt = " + sSysAmt);
		*/

		// person account
		String	sPerAmt = "", sIndAmt = "", sTransAmt = "", sIndPaid = "" ;
		float cs = 0, indcs = 0 ;
		/* sort persons, :get the iterator & sort it */
		Hashtable<String, Person> aGroup = Utils.m_GroupCollection3.get(group) ;
		List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
		Collections.sort(mapKeys);
	    iter = mapKeys.iterator();
		while (iter.hasNext()) {
			Person person = aGroup.get(iter.next());
			sTransAmt += Utils._ITEM_SEPARATOR + Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANS_AMT)) + Utils.rBr ;
			sPerAmt += Utils._ITEM_SEPARATOR + Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SYS_SUM)) + Utils.rBr ;
			sIndAmt += Utils._ITEM_SEPARATOR + Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_SUM)) + Utils.rBr ;
			sIndPaid += Utils._ITEM_SEPARATOR + Utils.lBr + person.m_name + Utils._AMT_INDICATOR + Utils.roundAmount(person.m_amount.get(Person.AccountEntry.IND_PAID)) + Utils.rBr ;
			cs = person.m_amount.get(Person.AccountEntry.CHK_SUM) ;
			indcs = person.m_amount.get(Person.AccountEntry.CHK_INDSUM) ;
		}
		//System.out.println("prepareToExportGroup::sIndPaid = " + sIndPaid);

		sTransAmt = Utils.lPAD + sTransAmt.substring(Utils._ITEM_SEPARATOR.length(), sTransAmt.length()) + Utils.rPAD ;
		sPerAmt = Utils.lPAD + sPerAmt.substring(Utils._ITEM_SEPARATOR.length(), sPerAmt.length()) + Utils.rPAD ;
		sIndAmt = Utils.lPAD + sIndAmt.substring(Utils._ITEM_SEPARATOR.length(), sIndAmt.length()) + Utils.rPAD ;
		sIndPaid = Utils.lPAD + sIndPaid.substring(Utils._ITEM_SEPARATOR.length(), sIndPaid.length()) + Utils.rPAD ;
		//System.out.println("prepareToExportGroup::sIndPaid = " + sIndPaid);

		aLine += Utils._TAB_SEPARATOR + sTransAmt + Utils._TAB_SEPARATOR + sSysAmt /*+ Utils._TAB_SEPARATOR*/ + sPerAmt + Utils.roundAmount(cs)+ Utils._TAB_SEPARATOR + sIndAmt + Utils.roundAmount(indcs) + Utils._TAB_SEPARATOR + sIndPaid  ;

		/*
		if (m_exportLines == null) {
			m_exportLines = new ArrayList<String>() ;
			m_exportLines.add(aLine) ;
		}
		else
			m_exportLines.add(aLine) ;
		*/

		// group impl {
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
		// group impl }
	}

	private String padAmountString(String sNames[], String embededPers, String unpaddedLine)
	{
		try {
			//System.out.println("sNames: " + sNames + ", embededPers: " + embededPers + ", unpaddedLine: " + unpaddedLine);

			for (int i = 0; i < sNames.length; i++) {
				int fPos = embededPers.indexOf(sNames[i]) ;
				//System.out.println("sNames[i]: " + sNames[i]);
				//System.out.println("fPos: " + fPos);
				if (fPos == -1) /* not found */ {
					unpaddedLine = unpaddedLine.replaceFirst(sNames[i], "") ;
					//System.out.println("unpaddedLine: " + unpaddedLine);
				} else {		/* found */
					int tPos = embededPers.indexOf(Utils.rBr, fPos) ;
					//System.out.println("tPos: " + tPos);
					String sAmt = embededPers.substring(fPos + sNames[i].length() + Utils._AMT_INDICATOR.length(), tPos) ;
					//System.out.println("sAmt: " + sAmt);
					unpaddedLine = unpaddedLine.replaceFirst(sNames[i], sAmt) ;
					//System.out.println("unpaddedLine: " + unpaddedLine);
				}
			}
			return unpaddedLine ;
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			return unpaddedLine ;
		}
	}

	private String makeOutFilename(String fileName, String group)
	{
		String outFilename = "" ;
		int fileExt = fileName.lastIndexOf(Utils.OUT_FILESEP) ;
		if (fileExt == -1) // not found
			outFilename += Utils.OUT_EXTENSION ;
		else
			outFilename = group + Utils.OUT_FILESEP + fileName.substring(0, fileExt) + Utils.OUT_FILE + Utils.OUT_EXTENSION ;
			//outFilename = fileName.substring(0, fileExt) + OUT_FILE + fileName.substring(fileExt, fileName.length()) ;

		return outFilename ;
	}

	private String padHeader(int nTabs)
	{
		String xHeader = "" ;
		while (nTabs > 0) {
				xHeader += Utils._TAB_SEPARATOR ;
				nTabs-- ;
		}
		return xHeader ;
	}

	private void exportToCSV(String fileName, String group)
	{
		String outFilename = makeOutFilename(fileName, group) ;
		////System.out.println("outFilename: " + outFilename);

		try {
			// Create file
			FileWriter fstream = new FileWriter(outFilename);
			BufferedWriter out = new BufferedWriter(fstream);

			// begin Header {
			String	sTabs = "" ; //Utils._TAB_SEPARATOR ;
			String	sPersons = "" ;

			/* sort persons, :get the iterator & sort it */
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection3.get(group) ;
			List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
			Collections.sort(mapKeys);
			Iterator<String> iter = mapKeys.iterator();
			while (iter.hasNext()) {
				Person person = aGroup.get(iter.next());
				sPersons += sTabs + person.m_name ;
				sTabs = Utils._TAB_SEPARATOR ;
			}

			String sHeader0, sHeader01, sHeader02, sHeader03, sHeader ;
			sHeader01 = Utils.H_TRANSACTION_AMOUNTS + padHeader(aGroup.size()) ;
			if (Utils.m_bSys == true)
				//sHeader02 = Utils.H_OWE + padHeader(m_Persons.size() + 1 + 1 /*CheckSum*/) ;
				sHeader02 = Utils.H_OWE + padHeader(aGroup.size() + 1 + 1 /*CheckSum*/) ;
			else
				sHeader02 = Utils.H_OWE + padHeader(aGroup.size() + 1 /*CheckSum*/) ;
			sHeader03 = Utils.H_INDIVIDUAL_TOTALS + padHeader(aGroup.size() - 1 + 1 /*IndCheckSum*/) + Utils._TAB_SEPARATOR + Utils.H_INDIVIDUAL_PAID ;
			sHeader0 = padHeader(2 /* v15 */ + 6) + sHeader01 + sHeader02 + sHeader03 ;
			sHeader = Utils.H_ITEM + Utils._TAB_SEPARATOR + Utils.H_CATEGORY + Utils._TAB_SEPARATOR + Utils.H_VENDOR + Utils._TAB_SEPARATOR + Utils.H_DESCRIPTION + Utils._TAB_SEPARATOR + Utils.H_AMOUNT + Utils._TAB_SEPARATOR + Utils.H_FROM + Utils._TAB_SEPARATOR + Utils.H_TO + Utils._TAB_SEPARATOR + Utils.H_ACTION ;

			if (Utils.m_bSys == true)
				sHeader += Utils._TAB_SEPARATOR  + sPersons + Utils._TAB_SEPARATOR + Utils._SYS  + Utils._TAB_SEPARATOR + sPersons + Utils._TAB_SEPARATOR + Utils.H_CHECKSUM + Utils._TAB_SEPARATOR + sPersons + Utils._TAB_SEPARATOR + Utils.H_INDCHECKSUM + Utils._TAB_SEPARATOR + sPersons;
			else
				sHeader += Utils._TAB_SEPARATOR /*Utils._SYS + Utils._TAB_SEPARATOR*/ + sPersons + Utils._TAB_SEPARATOR + sPersons + Utils._TAB_SEPARATOR + Utils.H_CHECKSUM + Utils._TAB_SEPARATOR + sPersons + Utils._TAB_SEPARATOR + Utils.H_INDCHECKSUM + Utils._TAB_SEPARATOR + sPersons ;
			out.write(sHeader0);	out.newLine();
			out.write(sHeader);		out.newLine();
			// } end Header

			sPersons += Utils._TAB_SEPARATOR ;
			//System.out.println("sPersons: " + sPersons);

			ArrayList<String> exportLines = m_exportLinesGroup.get(group) ;
			for (String aLine : exportLines) {
				//String unpaddedLine = sPersons.substring(Utils._TAB_SEPARATOR.length(), sPersons.length()) ;
				String unpaddedLine = sPersons.substring(0, sPersons.length()) ;
				//System.out.println("unpaddedLine: " + unpaddedLine);

				String sNames[] = unpaddedLine.split(Utils._TAB_SEPARATOR) ;

				//System.out.println("aLine = " + aLine);
				//out.write(aLine);		out.newLine();
				String debugLine ;

				// trans amount
				int fx = aLine.indexOf(Utils.lPAD) + Utils.lPAD.length() ;
				int tx = aLine.indexOf(Utils.rPAD, fx) ;
				String embededPersonsx = aLine.substring(fx, tx).trim() ;
				String sNewLinex = padAmountString(sNames, embededPersonsx, unpaddedLine) ;
				debugLine = "fx = " + fx + ", tx = " + tx + ", embededPersonsx = " + embededPersonsx + ", sNewLinex = [" + sNewLinex + "]" ;
				//out.write(debugLine);		out.newLine();

				// sys amount
				int f0 = 0, t0 = 0 ;
				String sNewLine0 = "" ;
				if (Utils.m_bSys == true) {
					f0 = aLine.indexOf(Utils.lPAD, tx + Utils.rPAD.length()) + Utils.lPAD.length() ;
					t0 = aLine.indexOf(Utils.rPAD, f0) ;
					String embededPersons0 = aLine.substring(f0, t0).trim() ;
					String[] sysAC = new String[] { Utils._SYS };
					sNewLine0 = padAmountString(sysAC, embededPersons0, Utils._SYS) ;
					debugLine = "f0 = " + f0 + ", t0 = " + t0 + ", embededPersons0 = " + embededPersons0 + ", sNewLine0 = [" + sNewLine0 + "]" ;
					//out.write(debugLine);		out.newLine();
				} else {
					f0 = fx ;
					t0 = tx ;
				}

				// person amounts
				int f1 = aLine.indexOf(Utils.lPAD, t0 + Utils.rPAD.length()) + Utils.lPAD.length() ;
				int t1 = aLine.indexOf(Utils.rPAD, f1) ;
				String embededPersons1 = aLine.substring(f1, t1).trim() ;
				String sNewLine1 = padAmountString(sNames, embededPersons1, unpaddedLine) ;
				debugLine = "f1 = " + f1 + ", t1 = " + t1 + ", embededPersons1 = " + embededPersons1 + ", sNewLine1 = [" + sNewLine1 + "]" ;
				//System.out.println("debugLine: " + debugLine);
				//out.write(debugLine);		out.newLine();

				// individual amounts
				int f2 = aLine.indexOf(Utils.lPAD, t1 + Utils.rPAD.length()) + Utils.lPAD.length() ;
				int t2 = aLine.indexOf(Utils.rPAD, f2) ;
				String embededPersons2 = aLine.substring(f2, t2).trim() ;
				String sNewLine2 = padAmountString(sNames, embededPersons2, unpaddedLine) ;
				debugLine = "f2 = " + f2 + ", t2 = " + t2 + ", embededPersons2 = " + embededPersons2 + ", sNewLine2 = [" + sNewLine2 + "]" ;
				//out.write(debugLine);		out.newLine();
				//System.out.println("debugLine: " + debugLine);

				//checksum
				int f11 = t1 + Utils.rPAD.length() ;
				int t11 = aLine.indexOf(Utils.lPAD, f11) ;
				debugLine = "f11 = " + f11 + ", t11 = " + t11;
				String checkSum = aLine.substring(f11, t11).trim() ;
				//System.out.println("checkAum: " + debugLine + ":" + checkSum);

				//indchecksum
				int f21 = t1 + Utils.rPAD.length() ;
				int t21 = aLine.indexOf(Utils.lPAD, f21) ;
				debugLine = "f21 = " + f21 + ", t21 = " + t21;
				String indcheckSum = aLine.substring(f21, t21).trim() ;
				//System.out.println("indcheckSum: " + debugLine + ":" + indcheckSum);

				//individual paid
				int f3 = aLine.indexOf(Utils.lPAD, t21 + Utils.rPAD.length()) + Utils.lPAD.length() ;
				int t3 = aLine.indexOf(Utils.rPAD, f3) ;
				String embededPersons3 = aLine.substring(f3, t3).trim() ;
				String sNewLine3 = padAmountString(sNames, embededPersons3, unpaddedLine) ;
				debugLine = "f3 = " + f3 + ", t3 = " + t3 + ", embededPersons3 = " + embededPersons3 + ", sNewLine3 = [" + sNewLine3 + "]" ;
				//System.out.println("debugLine: " + debugLine);

				////System.out.println("here.0");
				////System.out.println("substring(0, fx - Utils.lPAD.length()," + fx + ", " + Utils.lPAD.length() + aLine.substring(0, fx - Utils.lPAD.length()));
				//int i1 = tx + Utils.rPAD.length() ;
				//int i2 = f0 - Utils.lPAD.length() ;
				////System.out.println("aLine.substring(tx + Utils.rPAD.length(), f0 - Utils.lPAD.length()," + i1 + "," + i2 /*+ ", " + aLine.substring(tx + Utils.rPAD.length(), f0 - Utils.lPAD.length())*/);

				String sToFile = "" ;
				String outLine = "" ;
				/*
				out.write("0: newLines:");		out.newLine();
				out.write(sNewLinex);		out.newLine();
				if (m_bSys == true) {out.write(sNewLine0);		out.newLine();}
				out.write(sNewLine1);		out.newLine();
				out.write(sNewLine2);		out.newLine();
				*/

				outLine += aLine.substring(0, fx - Utils.lPAD.length()) + sNewLinex ;
				if (Utils.m_bSys == true) outLine += sNewLine0  + Utils._TAB_SEPARATOR ;
				outLine += sNewLine1 + checkSum;
				outLine += Utils._TAB_SEPARATOR + sNewLine2 + indcheckSum ;
				outLine += Utils._TAB_SEPARATOR + sNewLine3 ;
				sToFile = outLine ;

				/*
				//sToFile += aLine.substring(0, fx - Utils.lPAD.length()) + sNewLinex + aLine.substring(tx + Utils.rPAD.length(), f0 - Utils.lPAD.length()) ;
				sToFile += aLine.substring(0, fx - Utils.lPAD.length()) + sNewLinex + aLine.substring(f0 - Utils.lPAD.length(), tx + Utils.rPAD.length()) ;
				debugLine = aLine.substring(0, fx - Utils.lPAD.length()) ;
				out.write("1:" + debugLine);		out.newLine();
				int x = f0 - Utils.lPAD.length() ; int y = tx + Utils.rPAD.length() ;
				out.write("1.1:" + x + "," + y);		out.newLine();
				debugLine = aLine.substring(f0 - Utils.lPAD.length(), tx + Utils.rPAD.length()) ;
				out.write("2:" + debugLine);		out.newLine();
				if (m_bSys == true) { sToFile += sNewLine0 + aLine.substring(t0 + Utils.rPAD.length(), f1 - Utils.lPAD.length()) ;
					////System.out.println("sToFile1 = [" + sToFile + "]");
					//out.write(sToFile);		out.newLine();
				}

				////System.out.println("here.1");
				sToFile += sNewLine1 + aLine.substring(t1 + Utils.rPAD.length(), f2 - Utils.lPAD.length()) ;
				////System.out.println("sToFile2 = [" + sToFile + "]");
				out.write(sToFile);		out.newLine();

				sToFile += sNewLine2 + aLine.substring(t2 + Utils.rPAD.length(), aLine.length()) ;
				////System.out.println("sToFile3 = [" + sToFile + "]");
				//out.write(sToFile);		out.newLine();
				*/

				out.write(sToFile);		out.newLine();
			}
			out.close();		//Close the output stream
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	public void exportToCSVGroup(String fileName)
	{
		Enumeration<String> keysGroup = Utils.m_GroupCollection3.keys();
		while(keysGroup.hasMoreElements()){
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection3.get(groupName) ;

			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				exportToCSV(fileName, groupName) ;
			}
		}
	}

}
