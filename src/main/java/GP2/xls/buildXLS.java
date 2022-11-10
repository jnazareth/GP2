package GP2.xls;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.group.groupCsvJsonMapping;
import GP2.json.ReadJson;
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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataFields;
import org.apache.poi.xssf.usermodel.XSSFCellStyle ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import au.com.bytecode.opencsv.CSVReader;
//import com.opencsv.CSVReader;
import org.apache.commons.lang3.math.NumberUtils;

public class buildXLS {

	//HashSet<Integer> m_colRange = null ;
	//HashSet<Integer> m_rowRange = null ;

    /*private int getSheetIndex(XSSFWorkbook workBook, String sheetName) {
        int index = -1 ;
        for(Sheet sheet : workBook) {
            if (sheet.getSheetName().equalsIgnoreCase(sheetName)) {
                index = workBook.getSheetIndex(sheetName) ;
                break;
            }
        }
        return index ;
    }*/

    /*private int getSheetIndex2(XSSFWorkbook workBook, String sheetName) {
        return workBook.getSheetIndex(sheetName) ;
    }*/

    /*private void getStartEndRange(String sFormatColumn, int s[], int e[]) {
        final String sLeftBr = "[";
        final String sRightBr = "]";
        final String sRange = ":";
        final String sSeparator = ",";

        String sStart = "", sEnd = "" ;
        //String sFormatColumn = "[C9:C10]" ;
        int nStart = sFormatColumn.indexOf(sLeftBr) ;
        int nRange = sFormatColumn.indexOf(sRange) ;
        int nEnd = -1 ;
        if ((nStart != -1) && (nRange != -1)) {
            sStart = sFormatColumn.substring(nStart + 2 / * skip column indicator: 'C' * /, nRange) ;

            nEnd = sFormatColumn.indexOf(sRightBr);
            if (nEnd != -1){
                sEnd = sFormatColumn.substring(nRange + 2, nEnd) ;
            }
        }
        //System.out.println("nStart,nRange,nEnd,sStart,sEnd:" + nStart + "," + nRange + "," + nEnd + "," + sStart + "," + sEnd);
        int x = Integer.parseInt(sStart);
        s[0] = x ;
        x = Integer.parseInt(sEnd) ;
        e[0] = x ;
    }*/

	/*private void buildTablesRanges(String rowRange, String colRange) {
		m_colRange = new HashSet<Integer>();
		m_colRange = buildRange(colRange) ;	//"[C5:C5], [C9:C18]"

		m_rowRange = new HashSet<Integer>();
		m_rowRange = buildRange(rowRange) ; //"[R3:R18]"
	}

	private HashSet<Integer> buildRange(String sRange) {
        HashSet<Integer> aRange = new HashSet<Integer>() ;
        final String _ITEM_SEPARATOR = "," ;
        String[] pieces = sRange.split(_ITEM_SEPARATOR);
        for (String p : pieces) {
            int nStart[] = {0}, nEnd[] = {0};
            getStartEndRange(p.trim(), nStart, nEnd) ;
            for (int i = nStart[0]; i <= nEnd[0]; i++) {
                aRange.add(i-1) ;     // 0 based
            }
        }
        return aRange;
    }*/

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
        CSVReader reader = null;
        try {
            String[] nextLine;

			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outCSVFile);
            //System.out.println("outCSVFile:" + outCSVFile);

            //2.3.0
            final char FILE_DELIMITER = '\t';
            reader = new CSVReader(new FileReader(f), FILE_DELIMITER);
            //reader = new CSVReader(new FileReader(f));

            //_SheetProperties sProperties = getSheetProperties() ;

            //createStyle
            XSSFCellStyle cellStyle = wb.createCellStyle();
            DataFormat format = wb.createDataFormat();
            //String amountFormat = "$#,##0.00_);($#,##0.00)";
            //final String accountingFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;
            final String accountingFormat = sProperties.getsFormat() ;
            cellStyle.setDataFormat(format.getFormat(accountingFormat));

            int rowNum = 0;
            while((nextLine = reader.readNext()) != null) {
                Row currentRow = sheet.createRow(rowNum);

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
        } catch(Exception exObj) {
            System.err.println("Exception In convertCsvToXls() Method?=  " + exObj);
        } finally {
            try {
                reader.close();             /**** Closing The CSV File-ReaderObject ****/
            } catch (IOException ioExObj) {
                System.err.println("Exception While Closing I/O Objects In convertCsvToXls() Method?=  " + ioExObj);
            }
        }
    }

    private void buildPivot(XSSFSheet sheet, _SheetProperties sProperties) {
        int headerRow0 = 0;
        int headerRow1 = (int)sProperties.getlHeaders() - 1 ;      // 1, last header is formatting header
        int numberofHeaderRowsToSkip = (int)sProperties.getlHeaders() - headerRow1 ;
        //System.out.println("headerRow1, numberofHeaderRowsToSkip:" + headerRow1 + "\t" + numberofHeaderRowsToSkip);

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
            pivotColumns.put(i /*-1 /* zero based, adjust by 1 */, nColName) ;
        }

        XSSFPivotTable pivotTable = sheet.createPivotTable(source, position);
        pivotTable.addRowLabel(1);

        final int numberFormat = sProperties.getsFormatFormat();	//7 ;
        for (Map.Entry<Integer, String> entry : pivotColumns.entrySet()) {
            int col = entry.getKey();
            String cName = entry.getValue();
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, col, cName);
            setFormatDataField(pivotTable, col, numberFormat); //set format of value field numFmtId=3 # ##0
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
            buildPivot(sheet, sp) ;

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
            //System.out.println("xlsFile, outCSVFile:" + xlsFile + "\t" + outCSVFile);
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

            for(String key: Utils.m_grpCsvJsonMap._groupMap.keySet()) {
                /*System.out.println("key:" + key);
                if (!(	(key.equalsIgnoreCase("default"))   ||
                      	(key.equalsIgnoreCase("medical"))   ||
                      	(key.equalsIgnoreCase("planesTrainsAutos"))   ))
                    continue;*/
                    groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key) ;
                csvFileJSON ocj = jFileR.readJSON(cj._sCSVJSONFile) ;
                _SheetProperties sp = ocj.toSheetProperties() ;
                //sp.dump();
				buildXLSFile(fName, f, key, cj, sp);
            }
        } catch (Exception e) {
        }
    }

    public void readFromMap(String fName, File f) {
        try {
            if (Utils.m_grpCsvJsonMap == null) return ;

            for(String key: Utils.m_grpCsvJsonMap._groupMap.keySet()) {
                /*System.out.println("key:" + key);
                if (!(	(key.equalsIgnoreCase("hcg"))   ||
                      	(key.equalsIgnoreCase("medical"))   ||
                      	(key.equalsIgnoreCase("planesTrainsAutos"))))
                    continue;*/
                groupCsvJsonMapping._CSV_JSON cj = Utils.m_grpCsvJsonMap._groupMap.get(key) ;
                _SheetProperties sp = cj._sheetProperties ;
                //sp.dump() ;
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
			// check for success
			/*if (f.exists())
				System.out.println("recreated:" + filetoRecreate);
			else
				System.out.println("failed to recreat:" + filetoRecreate);*/

			return f;
        } catch (FileNotFoundException fnf) {
        } catch (IOException ioe) {
        } //catch (InvalidFormatException ife) {}
        return f;
	}
}
