package GP2.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import GP2.format.Export.RowLayout;
import GP2.group.Groups.Group;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xcur.CrossCurrency;
import GP2.xcur.CrossCurrency.Currencies;
import GP2.xls._SheetProperties;

public class GPFormatThread implements Runnable {
    private String m_group;
    private Export m_export;

    // Constructor implementation
    public GPFormatThread(String group) {
        this.m_group = group;
        this.m_export = new Export() ;
    }

    private String getGroupFormat(String sGroupName) {
        String format = Constants._CURRENCY_FORMAT;
        try {
            Group aG = Utils.m_Groups.get(sGroupName);
            if (aG != null) {
                CrossCurrency cc = aG.m_ccurrency ;
                if (cc != null) {
                    Currencies xCurrency = cc.xCurrency;
                    Currencies cCurrency = cc.currency;
                    format = ((cCurrency.compareTo(xCurrency) != 0) ? xCurrency.format : cCurrency.format) ;
                }
            }
            return format ;
        } catch (Exception e) {
			System.err.println("getGroupFormat::Error: " + e.getMessage());
            return format ;
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
    
    private void updateGroupMap(String groupName, _SheetProperties sp) {
            // update map
			groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(groupName) ;
			csvFileJSON csvFile = new csvFileJSON() ;
			csvFile = sp.toCsvFileJSON();
			Utils.m_grpCsvJsonMap.addItem(groupName, cj._sCSVFile, cj._sCSVJSONFile, csvFile, sp) ;
    }

    @Override
    public void run() {
        try {
            _SheetProperties sp = new _SheetProperties();

    		String outFilename = Utils.m_grpCsvJsonMap._groupMap.get(m_group)._sCSVFile ;
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;

			// Create file
			FileWriter fstream = fw;
			BufferedWriter out = new BufferedWriter(fstream);
			int rowCounter = 0, headerCounter = 0 ;

            m_export.buildHeaders(m_group);
            out.write(m_export.toCSVLine(m_export.header0));	out.newLine();  headerCounter++ ;
			out.write(m_export.toCSVLine(m_export.header1));    out.newLine();  headerCounter++ ;

            ArrayList<RowLayout> exportLines = m_export.m_exportLinesGroup.get(m_group) ;
			for (RowLayout rowToExport : exportLines) {
                RowLayout rl = m_export.buildRow(m_group, rowToExport) ;
                out.write(m_export.toCSVLine(rl));		out.newLine();
                rowCounter++ ;
            }
            sp = buildSP(m_group, rowCounter, headerCounter);

            // close all file handles
			out.close();    fstream.close() ;   fw.close() ;    foS.close() ;

            updateGroupMap(m_group, sp) ;
        } catch (Exception e) {
            System.err.println("GPFormatThread::Error:" + e.getMessage());
        }
    }
}
