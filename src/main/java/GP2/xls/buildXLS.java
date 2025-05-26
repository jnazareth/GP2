package GP2.xls;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.group.groupCsvJsonMapping;
import GP2.json.ReadJson2;
import GP2.format.FormatXLS;
import GP2.group.csvFileJSON;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.util.*;

public class buildXLS {

    private static void setFormatDataField(XSSFPivotTable pivotTable, long fieldIndex, long numFmtId) {
        Optional.ofNullable(pivotTable.getCTPivotTableDefinition().getDataFields())
                .map(CTDataFields::getDataFieldList)
                .ifPresent(dataFields -> dataFields.stream()
                        .filter(dataField -> dataField.getFld() == fieldIndex)
                        .findFirst()
                        .ifPresent(dataField -> dataField.setNumFmtId(numFmtId)));
    }

    private boolean cellInRange(int row, int column, _SheetProperties sProperties) {
        return sProperties.getsRows().contains(row) && sProperties.getsColumns().contains(column);
    }

    private _SheetProperties getSheetProperties() {
        _SheetProperties sp = new _SheetProperties();
        sp.setCsvFileName("default.sep.mint.out.csv");
        sp.setlHeaders(2);
        sp.setsRows("[R3:R42]");
        sp.setsColumns("[C5:C5],[C9:C18]");
        sp.setsFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)");
        sp.setsHeader("[R2:R2]");
        sp.setsFormatColumn("[C9:C10]");
        sp.setsFormatFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)");
        return sp ;
    }

    private void getCSVData(XSSFWorkbook wb, XSSFSheet sheet, String outCSVFile, _SheetProperties sProperties) {
        try (BufferedReader buffReader = new BufferedReader(new FileReader(new File(Utils.m_settings.getDirToUse(), outCSVFile)))) {
            XSSFCellStyle cellStyle = createCellStyle(wb, sProperties.getsFormat());
            String sLine;
            int rowNum = 0;
            while ((sLine = buffReader.readLine()) != null) {
                Row currentRow = sheet.createRow(rowNum);
                String[] nextLine = sLine.split(Constants._TAB_SEPARATOR);
                for (int i = 0; i < nextLine.length; i++) {
                    Cell cell = currentRow.createCell(i);
                    if (NumberUtils.isCreatable(nextLine[i])) {
                        cell.setCellValue(Double.parseDouble(nextLine[i]));
                    } else {
                        cell.setCellValue(nextLine[i]);
                    }
                    if (cellInRange(rowNum, i, sProperties)) {
                        cell.setCellStyle(cellStyle);
                    }
                }
                rowNum++;
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + outCSVFile);
        }
    }

    private XSSFCellStyle createCellStyle(XSSFWorkbook wb, String format) {
        XSSFCellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat(format));
        return cellStyle;
    }

    private void buildPivot(XSSFSheet sheet, _SheetProperties sProperties) {
        int headerRow1 = sProperties.getlHeaders() - 1;
        int numberofHeaderRowsToSkip = sProperties.getlHeaders() - headerRow1;

        TreeSet<Integer> tFCSet = new TreeSet<>(sProperties.getsFormatColumn());

        int firstRow = sheet.getFirstRowNum() + numberofHeaderRowsToSkip;
        int lastRow = sheet.getLastRowNum();
        int firstCol = sheet.getRow(headerRow1).getFirstCellNum();
        /*int numberofColstoInclude = 6 + 3 ;*/     // this should have been lastCol, but this consistently throws errors.
                                                    // so instead using a sufficient number of columns that will
                                                    // include the pivot columns
        int numberofColstoInclude = tFCSet.last() ; // 0 based, so use (end -1)
                                                    // this number is tricky. works with:
                                                    // 6 + 3 = 9 (hard coded). pivotStart[0] also happens to be 9.
                                                    // what if this were 10, 11, 12 ??
                                                    // the error is that the XLS opens but no pivot is created.

        CellReference topLeft = new CellReference(firstRow, firstCol);
        CellReference botRight = new CellReference(lastRow, firstCol + numberofColstoInclude);
        AreaReference source = new AreaReference(topLeft.formatAsString() + ":" + botRight.formatAsString(), SpreadsheetVersion.EXCEL2007);
        CellReference position = new CellReference(lastRow + 2, firstCol + 1);

        XSSFPivotTable pivotTable = sheet.createPivotTable(source, position);
        pivotTable.addRowLabel(1);

        final int numberFormat = sProperties.getsFormatFormat();
        final String sAmtFormat = sProperties.getsFormatFormatAsString();

        for (Map.Entry<Integer, String> entry : getPivotColumns(sheet, headerRow1, tFCSet).entrySet()) {
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, entry.getKey(), entry.getValue(), sAmtFormat);
        }
    }

    private Map<Integer, String> getPivotColumns(XSSFSheet sheet, int headerRow1, TreeSet<Integer> tFCSet) {
        Map<Integer, String> pivotColumns = new HashMap<>();
        for (int i = tFCSet.first(); i <= tFCSet.last(); i++) {
            String colName = sheet.getRow(headerRow1).getCell(i).getStringCellValue();
            pivotColumns.put(i, colName);
        }
        return pivotColumns;
    }

    private void dumpPivotTable(XSSFSheet sheet) {
        List<XSSFPivotTable> pTables ;
        pTables = sheet.getPivotTables() ;

        XSSFPivotTable pivotTable = pTables.get(0);
        CTPivotFields pivotFields = pivotTable.getCTPivotTableDefinition().getPivotFields();
        for(CTPivotField ctPivotField : pivotFields.getPivotFieldList()){
            //ctPivotField.setAutoShow(false);
            //ctPivotField.setOutline(false);
            //ctPivotField.setSubtotalTop(false);
            //ctPivotField.setSubtotalCaption("x");
            //ctPivotField.setSumSubtotal(true);

            //ctPivotField.dump();
            //ctPivotField.setShowAll(true);
            CTItems ctPivotFieldItems  = ctPivotField.getItems();
            //System.out.println(ctPivotField.toString());

            // this is returning null !
            List<org.openxmlformats.schemas.spreadsheetml.x2006.main.CTItem> arrItems =  ctPivotFieldItems.getItemList();
            for(CTItem aItem : arrItems) {
                System.out.println(aItem.toString());
            }
        }
    }

    public void constructXLS(String outCSVFile, String xlsFile, String groupName, _SheetProperties sp)
            throws IOException, InvalidFormatException {
        try (FileInputStream fileIn = new FileInputStream(new File(xlsFile));
             XSSFWorkbook workBook = new XSSFWorkbook(fileIn)) {

            XSSFSheet sheet = workBook.createSheet(groupName);
            getCSVData(workBook, sheet, outCSVFile, sp);

            new FormatXLS().formatTable(workBook, sheet, sp, groupName);
            buildPivot(sheet, sp);

            try (FileOutputStream fileOut = new FileOutputStream(xlsFile)) {
                workBook.write(fileOut);
            }
        }
    }

    private void buildXLSFile(String fName, File f, String key, groupCsvJsonMapping._CSV_JSON cj, _SheetProperties sp) {
        try {
            constructXLS(cj._sCSVFile, f.getName(), key, sp);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    public void readFromJSON(String fName, File f) {
        if (Utils.m_grpCsvJsonMap == null) return;

        try {
            ReadJson2 jFileR = new ReadJson2();
            String mapFile = Utils.m_settings.getMapFileToUse(fName);
            Utils.m_grpCsvJsonMap = jFileR.readJSONMapFile(mapFile);

            for (String key : Utils.customSort(Utils.m_grpCsvJsonMap._groupMap.keySet())) {
                groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key);
                csvFileJSON ocj = jFileR.readJSON(cj._sCSVJSONFile);
                _SheetProperties sp = ocj.toSheetProperties();
                buildXLSFile(fName, f, key, cj, sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFromMap(String fName, File f) {
        if (Utils.m_grpCsvJsonMap == null) return;

        try {
            for (String key : Utils.customSort(Utils.m_grpCsvJsonMap._groupMap.keySet())) {
                groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key);
                _SheetProperties sp = cj._sheetProperties;
                buildXLSFile(fName, f, key, cj, sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File initializeXLS(String fName) {
        try {
            String xlsFile = Utils.m_settings.getXLSToUse(fName);
            File f = new File(xlsFile);

            if (f.exists() && !f.delete()) {
                System.out.println("Failed to delete: " + f.getName());
            }

            File f2 = new File(f.getName());
            Utils.m_settings.setPropertyXLS("xls", f.getName(), true, f2);

            try (XSSFWorkbook workBook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(f2)) {
                workBook.write(fileOut);
            }
            return f2;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}