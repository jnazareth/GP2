package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.person.Person;
import GP2.xls._SheetProperties;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.format.Export2.RowLayout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils ;
import java.io.FileOutputStream ;
import java.io.File;

public class GPFormatter2 {
    private Export2 m_export2 = new Export2() ;

    public void prepareToExportGroup(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action, String def) {
        RowLayout rl = m_export2.putRow(item, category, vendor, desc, amt, from, to, group, action, def);
    }

    private void exportToCSV(String group, _SheetProperties sp) {
		String outFilename = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile ;
		try {
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;

			// Create file
			FileWriter fstream = fw;
			BufferedWriter out = new BufferedWriter(fstream);
			int rowCounter = 0, headerCounter = 0 ;

            m_export2.buildHeaders(group);
            out.write(m_export2.toCSVLine(m_export2.header0));	out.newLine();  headerCounter++ ;
			out.write(m_export2.toCSVLine(m_export2.header1));  out.newLine();  headerCounter++ ;

			sp.setCsvFileName(outFilename) ;
			sp.setlHeaders(headerCounter) ;
            RowLayout.CellLayout cl = m_export2.header1.getCell(Export2.ExportKeys.keyAmount) ;
            if (cl != null) sp.amountLocation = cl.xlsPosition;
            sp.maxColums = m_export2.header1.length();
            ArrayList<String> persons = m_export2.getSortedPersons(group);
            cl = m_export2.header1.getCell(Export2.ExportKeys.keyTransactions + Constants._ID_SEPARATOR + persons.get(1)) ;
            if (cl != null) {
                sp.personLocation = cl.xlsPosition-1;
                sp.pivotColumnStart = sp.personLocation ;
                sp.pivotColumnEnd = sp.pivotColumnStart + (persons.size()-1) ;
            }

            ArrayList<RowLayout> exportLines = m_export2.m_exportLinesGroup.get(group) ;
			for (RowLayout rowToExport : exportLines) {
                RowLayout rl = m_export2.buildRow(group, rowToExport) ;
				out.write(m_export2.toCSVLine(rl));		out.newLine();

                rowCounter++ ;
            }

            sp.maxRows = rowCounter ;
			sp.build() ;

            // close all file handles
			out.close();    fstream.close() ;   fw.close() ;    foS.close() ;
        } catch (Exception e){
			System.err.println("exportToCSV Error: " + e.getMessage());
		}
    }

	public void exportToCSVGroup(String fileName) {
		_SheetProperties sp = null ;
		Enumeration<String> keysGroup = Utils.m_GroupCollection.keys();
		while(keysGroup.hasMoreElements()) {
			String groupName = keysGroup.nextElement();
			Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(groupName) ;
			sp = new _SheetProperties() ;

            // TEST: only process default group
            //if (!groupName.equalsIgnoreCase("default")) continue;
            //System.out.println("groupName:" + groupName);

			exportToCSV(groupName, sp) ;

			// update map
			groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(groupName) ;
			csvFileJSON csvFile = new csvFileJSON() ;
			csvFile = sp.toCsvFileJSON();
			Utils.m_grpCsvJsonMap.addItem(groupName, cj._sCSVFile, cj._sCSVJSONFile, csvFile, sp) ;
		}
    }
}
