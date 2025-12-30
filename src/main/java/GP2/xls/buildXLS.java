package GP2.xls;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.group.groupCsvJsonMapping;
import GP2.json.ReadJson;
import GP2.format.Export;
import GP2.format.FormatXLS;
import GP2.format.Export.ExportKeys;
import GP2.format.Export.RowLayout;
import GP2.format.Export.XLSHeaders;
import GP2.group.csvFileJSON;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

// debug - read Pivot
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields ;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotField ;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTItem;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataFields;
import org.apache.poi.xssf.usermodel.XSSFCellStyle ;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.Math;

public class buildXLS {

    private static void setFormatDataField(XSSFPivotTable pivotTable, long fieldIndex, long numFmtId) {
        Optional.ofNullable(pivotTable
        .getCTPivotTableDefinition()
        .getDataFields())
        .map(CTDataFields::getDataFieldList)
        .map(List::stream)
        .ifPresent(stream -> stream
        .filter(dataField -> dataField.getFld() == fieldIndex)
        .findFirst()
        .ifPresent(dataField -> dataField.setNumFmtId(numFmtId)));
    }

    private boolean cellInRange2(int row, int column, _SheetProperties sProperties) {
        boolean r = (sProperties.getsRows().contains(row)) ;
        boolean c = (sProperties.getsColumns().contains(column)) ;
        //System.out.println("row, column, contains::" + row + "["+ r + "]," + column + "[" + c + "]");
        return (r && c);
    }

    private _SheetProperties getSheetProperties() {
        String csvFileName = "default.sep.mint.out.csv" ;
		Long lHeaders = 2L;
        // table fields
        String sRows = "[R3:R42]" ;                 // was: [R3 - Rn]
        String sColumns = "[C5:C5],[C9:C18]" ;     // was: [C5, C9 - Cn]
        //String sFormat = "$#,##0.00_);($#,##0.00)" ;
		String sFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;
        // pivot fields
        String sHeader = "[R2:R2]" ;				// was: [R2]
        String sArea = "[R2:R18][C1:C18]" ;			// was: "[R2:C1][R18:C18]"
        String sFormatColumn = "[C9:C10]" ;		    // 2 column pivot
        //String sFormatColumn = "[C9:C11]" ;		// 3 column pivot
        String sFormatFormat = sFormat ;

        _SheetProperties sp = new _SheetProperties();
        sp.setCsvFileName(csvFileName);
        sp.setlHeaders(lHeaders.intValue());
        sp.setsRows(sRows);
        sp.setsColumns(sColumns);
        sp.setsFormat(sFormat);
        sp.setsHeader(sHeader);
        sp.setsFormatColumn(sFormatColumn);
        sp.setsFormatFormat(sFormatFormat);

        return sp ;
    }

    private void getCSVData(XSSFWorkbook wb, XSSFSheet sheet, String outCSVFile, String grpName, _SheetProperties sProperties) {
        FileReader fileReader = null;
        try {
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outCSVFile);

            fileReader = new FileReader(f);
			BufferedReader buffReader = new BufferedReader(fileReader);
			String sLine = "";
            //_SheetProperties sProperties = getSheetProperties() ;

            //createStyle
            XSSFCellStyle cellStyle = wb.createCellStyle();
            DataFormat format = wb.createDataFormat();
            //String amountFormat = "$#,##0.00_);($#,##0.00)";
            //final String accountingFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;
            final String accountingFormat = sProperties.getsFormat() ;
            cellStyle.setDataFormat(format.getFormat(accountingFormat));

            int rowNum = 0;
            try {
                while ((sLine = buffReader.readLine()) != null) {
                    Row currentRow = sheet.createRow(rowNum);

                    String[] nextLine = sLine.split(Constants._TAB_SEPARATOR);
                    for(int i=0; i < nextLine.length; i++) {
                        Cell cell = null;
                        if (NumberUtils.isCreatable(nextLine[i])) {
                            cell = currentRow.createCell(i);
                            cell.setCellValue(Double.parseDouble(nextLine[i]));
                        } else {
                            cell = currentRow.createCell(i);
                            cell.setCellValue(nextLine[i]);
                        }
                        if (cellInRange2(rowNum, i, sProperties)) cell.setCellStyle(cellStyle);
                    }
                    rowNum++ ;
                }
                buffReader.close() ;
                fileReader.close();
            } catch (IOException e) {
                System.out.println("There was a problem reading:" + outCSVFile);
            }
		} catch (FileNotFoundException e) {
			System.out.println("Could not locate a file: " + e.getMessage());
		}
    }

    private void buildPivot(XSSFSheet sheet, _SheetProperties sProperties) {
        int headerRow0 = 0;
        int headerRow1 = (int)sProperties.getlHeaders() - 1 ;      // 1, last header is formatting header
        int numberofHeaderRowsToSkip = (int)sProperties.getlHeaders() - headerRow1 ;

        TreeSet<Integer> tFCSet = new TreeSet<Integer>(sProperties.getsFormatColumn());
        //System.out.println("tFCSet:" + tFCSet);

        int firstRow = sheet.getFirstRowNum() + numberofHeaderRowsToSkip;
        int lastRow = sheet.getLastRowNum();
        int firstCol = sheet.getRow(headerRow1).getFirstCellNum();
        int lastCol = sheet.getRow(headerRow1).getLastCellNum();
        //System.out.println("[firstRow][lastRow][firstCol][lastCol]:" + "[" + firstRow + "][" + lastRow + "][" + firstCol + "][" + lastCol + "]");

        CellReference topLeft = new CellReference(firstRow, firstCol);

        /*int numberofColstoInclude = 6 + 3 ;*/     // this should have been lastCol, but this consistently throws errors.
                                                    // so instead using a sufficient number of columns that will
                                                    // include the pivot columns
        int numberofColstoInclude = tFCSet.last() ; // 0 based, so use (end -1)
                                                    // this number is tricky. works with:
                                                    // 6 + 3 = 9 (hard coded). pivotStart[0] also happens to be 9.
                                                    // what if this were 10, 11, 12 ??
                                                    // the error is that the XLS opens but no pivot is created.

        CellReference botRight = new CellReference(lastRow, (firstCol + numberofColstoInclude)); /* lastCol -1 */
        String strSource = topLeft.formatAsString() + ":" + botRight.formatAsString() ;

        int pivotRelativePositionX = 1,  pivotRelativePositionY = 2 ;
        AreaReference source = new AreaReference(strSource, SpreadsheetVersion.EXCEL2007);
        CellReference position = new CellReference(lastRow + pivotRelativePositionY, firstCol + pivotRelativePositionX);

        // populate map with column # and names
        int nSize = (tFCSet.last() - tFCSet.first() + 1);
        HashMap<Integer, String> pivotColumns = new HashMap<Integer, String>(nSize) ;
        for (int i = tFCSet.first(); i <= tFCSet.last(); i++) {
            String nColName = sheet.getRow(headerRow1).getCell(i).getStringCellValue() ;
            pivotColumns.put(i, nColName) ;
        }

        XSSFPivotTable pivotTable = sheet.createPivotTable(source, position);
        int SubCategoryCol = 2 ;
        pivotTable.addRowLabel(SubCategoryCol);

        final int numberFormat = sProperties.getsFormatFormat();	//7 ;
        final String sAmtFormat = sProperties.getsFormatFormatAsString();
        //System.out.println("sAmtFormat:" + sAmtFormat);

        for (Map.Entry<Integer, String> entry : pivotColumns.entrySet()) {
            int col = entry.getKey();
            String cName = entry.getValue();
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, col, cName, sAmtFormat);
            // commented off after implementation of format groups specified via CrossCurrency
            // plus added sAmtFormat to addColumnLabel call (is optional)
            //setFormatDataField(pivotTable, col, numberFormat); //set format of value field numFmtId=3 # ##0
        }
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
	throws FileNotFoundException, IOException, InvalidFormatException {
		try {
            File f = new File(xlsFile);
            FileInputStream fileIn = new FileInputStream(f);
			XSSFWorkbook workBook = new XSSFWorkbook(fileIn);

            String sheetName = groupName ;
            XSSFSheet sheet = workBook.createSheet(sheetName);
            getCSVData(workBook, sheet, outCSVFile, sheetName, sp) ;

            FormatXLS fTable = new FormatXLS() ;
            fTable.formatTable(workBook, sheet, sp, groupName);

            buildPivot(sheet, sp) ;
            //dumpPivotTable(sheet);

            try (FileOutputStream fileOut = new FileOutputStream(f)) {
                workBook.write(fileOut);
                fileOut.close() ;
            }
		} catch (FileNotFoundException nfe) {
			nfe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	private void buildXLSFile(String fName, File f, String key, groupCsvJsonMapping._CSV_JSON cj, _SheetProperties sp) {
		try {
			String xlsFile = f.getName() ;
            String outCSVFile = cj._sCSVFile ;
			constructXLS(outCSVFile, xlsFile, key, sp) ;
        } catch (FileNotFoundException fnf) {
        } catch (IOException ioe) {
        } catch (InvalidFormatException ife) {
        }
	}

    public void readFromJSON(String fName, File f) {
        try {
            if (Utils.m_grpCsvJsonMap == null) return ;

            ReadJson jFileR = new ReadJson() ;
            String mapFile = Utils.m_settings.getMapFileToUse(fName) ;
            Utils.m_grpCsvJsonMap = jFileR.readJSONMapFile(mapFile) ;

            List<String> sortedMapKeys = Utils.customSort(Utils.m_grpCsvJsonMap._groupMap.keySet()) ;
            for (String key: sortedMapKeys) {
    			// ONLY process one group = testing
                /*if (!(	(key.equalsIgnoreCase("default"))   ||
                      	(key.equalsIgnoreCase("medical"))   ||
                      	(key.equalsIgnoreCase("planesTrainsAutos"))   ))
                    continue;
                System.out.println("key:" + key);*/
                groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key) ;
                csvFileJSON ocj = jFileR.readJSON(cj._sCSVJSONFile) ;
                _SheetProperties sp = ocj.toSheetProperties() ;
				buildXLSFile(fName, f, key, cj, sp);
            }
        } catch (Exception e) {
        }
    }

    public void readFromMap(String fName, File f) {
        try {
            if (Utils.m_grpCsvJsonMap == null) return ;

            List<String> sortedMapKeys = Utils.customSort(Utils.m_grpCsvJsonMap._groupMap.keySet()) ;
            for (String key: sortedMapKeys) {
                // ONLY process one group = testing
                /*if (!(	(key.equalsIgnoreCase("default"))   ||
                      	(key.equalsIgnoreCase("medical"))   ||
                      	(key.equalsIgnoreCase("planesTrainsAutos"))   ))
                    continue;
                System.out.println("key:" + key);*/
                groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key) ;
                _SheetProperties sp = cj._sheetProperties ;
				buildXLSFile(fName, f, key, cj, sp);
            }
        } catch (Exception e) {
        }
	}

    public File InitializeXLS(String fName) {
        File f = null ;
		try {
            String xlsFile = Utils.m_settings.getXLSToUse(fName) ;

			f = new File(xlsFile);
            String filetoRecreate = f.getName();
            boolean dFile = false;
			if (f.exists()) {
                dFile = f.delete();
				if (!dFile)
					System.out.println("failed to delete:" + filetoRecreate);
            }
			File f2 = new File(filetoRecreate);
			final String sKey = "xls" ;   // Property:xls
			Utils.m_settings.setPropertyXLS(sKey, filetoRecreate, true, f2);
			f = f2;
			XSSFWorkbook workBook = new XSSFWorkbook();
			try (FileOutputStream fileOut = new FileOutputStream(f)) {
				workBook.write(fileOut);
				workBook.close();
				fileOut.close() ;
			}
			return f;
        } catch (FileNotFoundException fnf) {
        } catch (IOException ioe) {
        }
        return f;
	}
}
