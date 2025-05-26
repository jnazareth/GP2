package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xcur.CrossCurrency;
import GP2.xcur.CrossCurrency.Currencies;
import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.format.Export.RowLayout;
import GP2.group.Groups.Group;
import GP2.thread.GPExecutor;
import GP2.xls._SheetProperties;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;

public class GPFormatter {
    private Export m_export = new Export();

    private String getGroupFormat(String groupName) {
        String format = Constants._CURRENCY_FORMAT;
        try {
            Group group = Utils.m_Groups.get(groupName);
            if (group != null) {
                CrossCurrency crossCurrency = group.m_ccurrency;
                if (crossCurrency != null) {
                    Currencies xCurrency = crossCurrency.xCurrency;
                    Currencies cCurrency = crossCurrency.currency;
                    format = (cCurrency.compareTo(xCurrency) != 0) ? xCurrency.format : cCurrency.format;
                }
            }
        } catch (Exception e) {
            System.err.println("getGroupFormat::Error: " + e.getMessage());
        }
        return format;
    }

    private Double getGroupRate(String groupName) {
        Double rate = 1.0d;
        try {
            Group group = Utils.m_Groups.get(groupName);
            if (group != null) {
                CrossCurrency crossCurrency = group.m_ccurrency;
                if (crossCurrency != null) {
                    Currencies baseCurrency = crossCurrency.xCurrency;
                    Currencies targetCurrency = crossCurrency.currency;
                    rate = (baseCurrency.compareTo(targetCurrency) != 0) ? crossCurrency.rate : rate;
                }
            }
        } catch (Exception e) {
            System.err.println("getGroupRate::Error: " + e.getMessage());
        }
        return rate;
    }

    public void prepareToExportGroup(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action) {
        try {
            Double rate = getGroupRate(group);
            m_export.putRow(item, category, vendor, desc, amt, from, to, group, action, rate);
        } catch (Exception e) {
            System.err.println("prepareToExportGroup::Error: " + e.getMessage());
        }
    }

    private _SheetProperties buildSheetProperties(String group, int rowCount, int headerCount) {
        _SheetProperties sheetProperties = new _SheetProperties();
        try {
            String csvFileName = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile;
            sheetProperties.setCsvFileName(csvFileName);
            sheetProperties.setlHeaders(headerCount);
            setAmountLocation(sheetProperties);
            sheetProperties.maxColums = m_export.header1.length();
            String amountFormat = getGroupFormat(group);
            sheetProperties.setsFormat(amountFormat);
            sheetProperties.setsFormatFormat(amountFormat);
            setPersonLocation(sheetProperties, group);
            sheetProperties.maxRows = rowCount;
            sheetProperties.build();
        } catch (Exception e) {
            System.err.println("buildSheetProperties::Error: " + e.getMessage());
        }
        return sheetProperties;
    }

    private void setAmountLocation(_SheetProperties sheetProperties) {
        RowLayout.CellLayout cellLayout = m_export.header1.getCell(Export.ExportKeys.keyAmount);
        if (cellLayout != null) {
            sheetProperties.amountLocation = cellLayout.xlsPosition;
        }
    }

    private void setPersonLocation(_SheetProperties sheetProperties, String group) {
        ArrayList<String> persons = m_export.getSortedPersons(group);
        RowLayout.CellLayout cellLayout = m_export.header1.getCell(Export.ExportKeys.keyTransactions + Constants._ID_SEPARATOR + persons.get(1));
        if (cellLayout != null) {
            sheetProperties.personLocation = cellLayout.xlsPosition - 1;
            sheetProperties.pivotColumnStart = sheetProperties.personLocation;
            sheetProperties.pivotColumnEnd = sheetProperties.pivotColumnStart + (persons.size() - 1);
        }
    }

    private _SheetProperties exportToCSV(String group) {
        _SheetProperties sheetProperties = new _SheetProperties();
        try {
            String csvFileName = Utils.m_grpCsvJsonMap._groupMap.get(group)._sCSVFile;
            File file = new File(Utils.m_settings.getDirToUse(), csvFileName);
            try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
                 FileWriter fileWriter = new FileWriter(fileOutputStream.getFD());
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                int rowCounter = 0, headerCounter = 0;
                m_export.buildHeaders(group);
                headerCounter = writeHeaders(bufferedWriter, headerCounter);
                rowCounter = writeRows(bufferedWriter, group, rowCounter);
                sheetProperties = buildSheetProperties(group, rowCounter, headerCounter);
            }
        } catch (Exception e) {
            System.err.println("exportToCSV::Error: " + e.getMessage());
        }
        return sheetProperties;
    }

    private int writeHeaders(BufferedWriter bufferedWriter, int headerCounter) throws Exception {
        bufferedWriter.write(m_export.toCSVLine(m_export.header0));
        bufferedWriter.newLine();
        headerCounter++;
        bufferedWriter.write(m_export.toCSVLine(m_export.header1));
        bufferedWriter.newLine();
        headerCounter++;
        return headerCounter;
    }

    private int writeRows(BufferedWriter bufferedWriter, String group, int rowCounter) throws Exception {
        ArrayList<RowLayout> exportLines = m_export.m_exportLinesGroup.get(group);
        for (RowLayout rowToExport : exportLines) {
            RowLayout rowLayout = m_export.buildRow(group, rowToExport);
            bufferedWriter.write(m_export.toCSVLine(rowLayout));
            bufferedWriter.newLine();
            rowCounter++;
        }
        return rowCounter;
    }

    public void exportToCSVGroup() {
        if (Utils.m_Groups == null) return;

        Enumeration<String> groupKeys = Utils.m_Groups.keys();
        while (groupKeys.hasMoreElements()) {
            String groupName = groupKeys.nextElement();
            _SheetProperties sheetProperties = exportToCSV(groupName);
            updateGroupMap(groupName, sheetProperties);
        }
    }

    private void updateGroupMap(String groupName, _SheetProperties sheetProperties) {
        groupCsvJsonMapping._CSV_JSON csvJson = Utils.m_grpCsvJsonMap._groupMap.get(groupName);
        csvFileJSON csvFile = sheetProperties.toCsvFileJSON();
        Utils.m_grpCsvJsonMap.addItem(groupName, csvJson._sCSVFile, csvJson._sCSVJSONFile, csvFile, sheetProperties);
    }

    public void exportToCSVGroup2() {
        if (Utils.m_Groups == null) return;

        GPExecutor executor = new GPExecutor(Utils.m_Groups.size());
        Enumeration<String> groupKeys = Utils.m_Groups.keys();
        while (groupKeys.hasMoreElements()) {
            String groupName = groupKeys.nextElement();
            executor.add(new GPFormatThread(groupName));
        }
        executor.launch();
        executor.collect();
        executor.shutdown();
    }
}