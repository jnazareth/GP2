package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xcur.CrossCurrency;
import GP2.xcur.CrossCurrency.Currencies;
import GP2.person.Person;
import GP2.xls._SheetProperties;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.format.Export.RowLayout;
import GP2.group.GroupAccount._AGroup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils ;
import java.io.FileOutputStream ;
import java.io.File;

public class GPFormatter {
    private Export m_export = new Export() ;

    private String getGroupFormat(String sGroupName) {
        String format = Constants._CURRENCY_FORMAT;
        try {
            _AGroup aG = Utils.m_GroupCollection.get(sGroupName);
            if (aG != null) {
                CrossCurrency cc = aG.m_ccurrency ;
                if (cc != null) {
                    Currencies xCurrency = cc.m_xcurrency;
                    Currencies cCurrency = cc.m_currency;
                    format = ((cCurrency.compareTo(xCurrency) != 0) ? xCurrency.format : cCurrency.format) ;
                }
            }
            return format ;
        } catch (Exception e) {
			System.err.println("getGroupFormat::Error: " + e.getMessage());
            return format ;
        }
    }

    private Double getGroupRate(String sGroupName) {
        Double rate = 1.0d;
        try {
            _AGroup aG = Utils.m_GroupCollection.get(sGroupName);
            if (aG != null) {
                CrossCurrency cc = aG.m_ccurrency ;
                if (cc != null) {
                    Currencies cBase = cc.m_xcurrency;
                    Currencies cCurrency = cc.m_currency;
                    rate = ((cBase.compareTo(cCurrency) != 0) ? cc.m_rate : rate) ;
                }
            }
            return rate ;
        } catch (Exception e) {
			System.err.println("getGroupRate::Error: " + e.getMessage());
            return rate ;
        }
    }

    public void prepareToExportGroup(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action) {
        try {
            Double rate = getGroupRate(group);
            RowLayout rl = m_export.putRow(item, category, vendor, desc, amt, from, to, group, action, rate);
        } catch (Exception e) {
			System.err.println("prepareToExportGroup::Error: " + e.getMessage());
        }
    }

    private _SheetProperties buildSP(String group, int rC, int hC) {
        _SheetProperties sp = new _SheetProperties();
        try {
            String outFilename = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile ;
            sp.setCsvFileName(outFilename) ;
            sp.setlHeaders(hC) ;
            RowLayout.CellLayout cl = m_export.header1.getCell(Export.ExportKeys.keyAmount) ;
            if (cl != null) sp.amountLocation = cl.xlsPosition;
            sp.maxColums = m_export.header1.length();
            String sAmountFormat = getGroupFormat(group) ;
            sp.setsFormat(sAmountFormat);
            sp.setsFormatFormat(sAmountFormat);
            ArrayList<String> persons = m_export.getSortedPersons(group);

            //System.out.println("persons.size:" + persons.size());
            cl = m_export.header1.getCell(Export.ExportKeys.keyTransactions + Constants._ID_SEPARATOR + persons.get(1)) ;
            if (cl != null) {
                sp.personLocation = cl.xlsPosition-1;
                sp.pivotColumnStart = sp.personLocation ;
                sp.pivotColumnEnd = sp.pivotColumnStart + (persons.size()-1) ;
            }
            sp.maxRows = rC ;
            sp.build() ;

            return sp ;
        } catch (Exception e) {
            System.err.println("buildSP::Error: " + e.getMessage());
            return sp;
        }
    }

    private _SheetProperties exportToCSV(String group) {
		String outFilename = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile ;
        _SheetProperties sp = new _SheetProperties();

        try {
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;

			// Create file
			FileWriter fstream = fw;
			BufferedWriter out = new BufferedWriter(fstream);
			int rowCounter = 0, headerCounter = 0 ;

            m_export.buildHeaders(group);
            out.write(m_export.toCSVLine(m_export.header0));	out.newLine();  headerCounter++ ;
			out.write(m_export.toCSVLine(m_export.header1));    out.newLine();  headerCounter++ ;

            ArrayList<RowLayout> exportLines = m_export.m_exportLinesGroup.get(group) ;
			for (RowLayout rowToExport : exportLines) {
                RowLayout rl = m_export.buildRow(group, rowToExport) ;
                out.write(m_export.toCSVLine(rl));		out.newLine();
                rowCounter++ ;
            }
            sp = buildSP(group, rowCounter, headerCounter);

            // close all file handles
			out.close();    fstream.close() ;   fw.close() ;    foS.close() ;
        } catch (Exception e){
			System.err.println("exportToCSV::Error: " + e.getMessage());
		}
        return sp ;
    }

	public void exportToCSVGroup(String fileName) {
		if (Utils.m_GroupCollection == null) return ;

		_SheetProperties sp = null ;
		Enumeration<String> keysGroup = Utils.m_GroupCollection.keys();
		while(keysGroup.hasMoreElements()) {
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(groupName) ;

            // TEST: only process default group
            //if (!groupName.equalsIgnoreCase("default")) continue;
            //System.out.println("groupName:" + groupName);
            sp = exportToCSV(groupName) ;

            // update map
			groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(groupName) ;
			csvFileJSON csvFile = new csvFileJSON() ;
			csvFile = sp.toCsvFileJSON();
			Utils.m_grpCsvJsonMap.addItem(groupName, cj._sCSVFile, cj._sCSVJSONFile, csvFile, sp) ;
		}
    }
}