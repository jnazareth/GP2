package GP2.format;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import GP2.format.Export.ExportKeys;
import GP2.format.Export.RowLayout;
import GP2.xls._SheetProperties;

public class FormatXLS {
    public FormatXLS() {
    }

    private void SetBorder (BorderStyle bs, CellRangeAddress range, XSSFSheet sheet) {
        try {
            RegionUtil.setBorderLeft(bs, range, sheet);
            RegionUtil.setBorderTop(bs, range, sheet);
            RegionUtil.setBorderBottom(bs, range, sheet);
            RegionUtil.setBorderRight(bs, range, sheet);
            RegionUtil.setBottomBorderColor(IndexedColors.BLACK.getIndex(), range, sheet);
        } catch (Exception e) {
			System.err.println("SetBorder::Error: " + e.getMessage());
        }
    }

    private int getPosition(Export export, String key) {
        RowLayout.CellLayout cl = export.header0.getCell(key);
        if (cl != null) {
            return cl.xlsPosition;
        }
        else
            return 0;
    }

    private XSSFCellStyle defineStyle(XSSFWorkbook workBook, XSSFSheet sheet){
        try {
			XSSFCellStyle cellStyle = workBook.createCellStyle();
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            return cellStyle;
        } catch (Exception e) {
			System.err.println("defineStyle::Error: " + e.getMessage());
        }
        return null;
    }

    private void formatBlock(XSSFWorkbook workBook, XSSFSheet sheet, Export export, formatCoordinates fcB, String ekStart, String ekEnd) {
        try {
            final int lAdj = 1, rAdj = 2;
            int l = (ekStart == null)   ? fcB.left      : (getPosition(export, ekStart) - lAdj) ;
            int r = (ekEnd == null)     ? fcB.right-1   : (getPosition(export, ekEnd) - rAdj) ;

            formatCoordinates fc = new formatCoordinates(l, fcB.top, r, fcB.bottom);
            SetBorder(BorderStyle.THIN, fc.toCellRangeAddress(), sheet);
            try {
                sheet.addMergedRegion(new formatCoordinates(l, fcB.top, r, fcB.top).toCellRangeAddress());
            } catch (IllegalStateException ise) {}
        } catch (Exception e) {
			System.err.println("formatBlock::Error: " + e.getMessage());
        }
    }

    private void formatBlocks(formatCoordinates fcB, XSSFWorkbook workBook, XSSFSheet sheet, _SheetProperties sProperties, String group) {
        try {
            Export export = new Export() ;
            export.buildHeaders(group);

            formatBlock(workBook, sheet, export, fcB, null, ExportKeys.keyTransactions);
            formatBlock(workBook, sheet, export, fcB, ExportKeys.keyTransactions, ExportKeys.keyOwe);
            formatBlock(workBook, sheet, export, fcB, ExportKeys.keyOwe, ExportKeys.keySpent);
            formatBlock(workBook, sheet, export, fcB, ExportKeys.keySpent, ExportKeys.keyPaid);
            formatBlock(workBook, sheet, export, fcB, ExportKeys.keyPaid, null);
        } catch (Exception e) {
			System.err.println("formatBlocks::Error: " + e.getMessage());
        }
    }

    private formatCoordinates getTableBoundaries(XSSFSheet sheet, _SheetProperties sProperties) {
        try {
            int headerRow1 = (int)sProperties.getlHeaders() - 1 ;      // last header is formatting header

            int firstHeaderRow = sheet.getFirstRowNum() ;
            int lastHeaderRow = headerRow1 ;
            int firstHeaderCol = firstHeaderRow ; 
            int lastHeaderCol = firstHeaderCol ;
            int lastRow = sheet.getLastRowNum();

            for (int r = firstHeaderRow; r <= lastHeaderRow; r++) {
				Row row = sheet.getRow(r);
				lastHeaderCol = Math.max(lastHeaderCol, row.getLastCellNum()) ;
            }
            return new formatCoordinates(firstHeaderCol, firstHeaderRow, lastHeaderCol, lastRow);
        } catch (Exception e) {
			System.err.println("getTableBoundaries::Error: " + e.getMessage());
        }
        return null;
    }

    private void colorHeaderRows(XSSFWorkbook workBook, XSSFSheet sheet, int lastHeaderRow){
        try {
            // color header rows
            XSSFCellStyle cs = defineStyle(workBook, sheet);
            int r = lastHeaderRow ;

            Iterator<Row> itr = sheet.iterator();
			while (itr.hasNext()) {
                if (r < 0) break;

                Row row = itr.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
                    if (cell != null) cell.setCellStyle(cs);
                }
                --r ;
            }
        } catch (Exception e) {
			System.err.println("colorHeaderRows::Error: " + e.getMessage());
        }
    }

    private void formatHeader(formatCoordinates fcB, XSSFWorkbook workBook, XSSFSheet sheet, _SheetProperties sProperties) {
        try {
            // header rows
            int headerRow1 = (int)sProperties.getlHeaders() - 1 ;      // last header is formatting header
            int lastHeaderRow = headerRow1 ;

            formatCoordinates fC = new formatCoordinates(fcB.left, fcB.top, fcB.right-1, lastHeaderRow);
            colorHeaderRows(workBook, sheet, lastHeaderRow);
            SetBorder(BorderStyle.DOUBLE, fC.toCellRangeAddress(), sheet);

            // full table border
            // commented because setting block border overwrites this border
            /*fC = new formatCoordinates(fcB.left, fcB.top, fcB.right-1, fcB.bottom);
            SetBorder(BorderStyle.THICK, fC.toCellRangeAddress(), sheet);*/
        } catch (Exception e) {
			System.err.println("formatHeader::Error: " + e.getMessage());
        }
    }

    public void formatTable(XSSFWorkbook workBook, XSSFSheet sheet, _SheetProperties sProperties, String group) {
        try {
            formatCoordinates fcBoundary = getTableBoundaries(sheet, sProperties);
            formatHeader(fcBoundary, workBook, sheet, sProperties) ;
            formatBlocks(fcBoundary, workBook, sheet, sProperties, group) ;
        } catch (Exception e) {
			System.err.println("formatTable::Error: " + e.getMessage());
        }
    }

    /*
     * class formatCoordinates
     */

     private class formatCoordinates {
        int left;       int top;
        int right;      int bottom;

        public formatCoordinates(int l, int t, int r, int b) {
            left = l;   top = t;
            right = r;  bottom = b;
        }

        private CellRangeAddress toCellRangeAddress() {
            return new CellRangeAddress(top, bottom, left, right);
        }
        @Override public String toString() {
            return "formatCoordinates::[l,t,r,b]:" + this.left + "," +  this.top + "," + this.right + "," + this.bottom;
        }
     }
}
