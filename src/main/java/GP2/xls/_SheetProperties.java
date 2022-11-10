package GP2.xls;

import GP2.group.csvFileJSON;
import GP2.xls._Coordinates;

import java.util.HashSet;
import java.util.HashMap;

public class _SheetProperties {
        private String csvFileName ;            //= "default.sep.mint.out.csv" ;
		private int lHeaders ;                  //= 2L;
        // table fields
        private _Coordinates sRows ;            //= "[R3:R18]" ;
        private _Coordinates sColumns ;         //= "[C5:C5], [C9:C18]" ;
		private String sFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;
        // pivot fields
        private _Coordinates sHeader ;          //= "[R2:R2]" ;
        private String sArea         = "[R2:R18][C1:C18]" ; // hardcoded for now
        private _Coordinates sFormatColumn ;    //= "[C9:C10]" ;
        private String sFormatFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;

        public int maxColums ; 
        public int maxRows ;
        public int amountLocation ; 
        public int personLocation ; 
        public int pivotColumnStart ; 
        public int pivotColumnEnd ; 

        final String sLeftBR = "[" ;
        final String sRightBR = "]" ;
        final String sRangeSeparator = ":" ;
        final String sRow = "R" ;
        final String sColumn = "C" ;
        final String sRangeDelimitter = "," ;

        // Constants
        /*interface _AREA {
            _AREA_ROW = 0 ;
            _AREA_COL = 1 ;
        } ;*/

    public _SheetProperties build () {
        /*
        sRows = sLeftBR + sRow + String.valueOf((lHeaders+1)) + sRangeSeparator + sRow + String.valueOf(maxRows+lHeaders) + sRightBR ;
        sColumns = sLeftBR + sColumn + String.valueOf(amountLocation) + sRangeSeparator + sColumn + String.valueOf(amountLocation) + sRightBR ;
        sColumns += sRangeDelimitter + sLeftBR + sColumn + String.valueOf(pivotColumnStart) + sRangeSeparator + sColumn + String.valueOf(maxColums) + sRightBR ;
        sHeader = sLeftBR + sRow + String.valueOf(lHeaders) + sRangeSeparator + sRow + String.valueOf(lHeaders) + sRightBR ;
        sFormatColumn = sLeftBR + sColumn + String.valueOf(pivotColumnStart) + sRangeSeparator + sColumn + String.valueOf(pivotColumnEnd) + sRightBR ;
        */

        String sR = sLeftBR + sRow + String.valueOf((lHeaders+1)) + sRangeSeparator + sRow + String.valueOf(maxRows+lHeaders) + sRightBR ;
        sRows = new _Coordinates(sR);

        String  sC = sLeftBR + sColumn + String.valueOf(amountLocation) + sRangeSeparator + sColumn + String.valueOf(amountLocation) + sRightBR ;
                sC += sRangeDelimitter + sLeftBR + sColumn + String.valueOf(pivotColumnStart) + sRangeSeparator + sColumn + String.valueOf(maxColums) + sRightBR ;
        sColumns = new _Coordinates(sC);

        String sH = sLeftBR + sRow + String.valueOf(lHeaders) + sRangeSeparator + sRow + String.valueOf(lHeaders) + sRightBR ;
        sHeader = new _Coordinates(sH);

        String sF = sLeftBR + sColumn + String.valueOf(pivotColumnStart) + sRangeSeparator + sColumn + String.valueOf(pivotColumnEnd) + sRightBR ;
        sFormatColumn = new _Coordinates(sF);

        return this ;
    }

    public _SheetProperties setCsvFileName (String sFN) {
        csvFileName = sFN;
        return this ;
    }
    public String getCsvFileName() {
        return csvFileName;
    }
        
    public void setlHeaders (int lH) {
        lHeaders = lH;
    }
    public int getlHeaders() {
        return lHeaders;
    }

    public _SheetProperties setsRows (String sR) {
        sRows = new _Coordinates(sR);
        return this ;
    }
    public HashSet<Integer> getsRows() {
        return sRows.toCoordsSet();
    }
    public String getsRowsAsString() {
        return sRows.toCoordsString();
    }

    public _SheetProperties setsColumns (String sC) {
        sColumns = new _Coordinates(sC);
        return this ;
    }
    public HashSet<Integer> getsColumns() {
        return sColumns.toCoordsSet();
    }
    public String getsColumnsAsString() {
        return sColumns.toCoordsString();
    }

    public void setsFormat (String sF) {
        sFormat = sF;
    }
    public String getsFormat() {
        return sFormat;
    }

    public void setsHeader (String sH) {
        sHeader = new _Coordinates(sH);
    }
    public HashSet<Integer> getsHeader() {
        return sHeader.toCoordsSet();
    }
    public String getsHeaderAsString() {
        return sHeader.toCoordsString();
    }

    public _SheetProperties setsArea (String sA) {
        sArea = sA;
        return this ;
    }
    public  String getsArea() {
        return sArea;
    }

    public _SheetProperties setsFormatColumn (String sFH) {
        sFormatColumn = new _Coordinates(sFH);
        return this ;
    }
    public HashSet<Integer> getsFormatColumn() {
        return sFormatColumn.toCoordsSet();
    }
    public String getsFormatColumnAsString() {
        return sFormatColumn.toCoordsString();
    }

    public void setsFormatFormat (String sFF) {
        sFormatFormat = sFF;
    }
    public String getsFormatFormatAsString() {
        return sFormatFormat;   // pivot table value for "$" format
    }
    public Integer getsFormatFormat() {
        return 7;             // pivot table value for "$" format
    }

    public csvFileJSON toCsvFileJSON() {
        csvFileJSON aJSON = null;
        try{
            aJSON = new csvFileJSON();
            /*
            //String sFormatColumn = "[C9:C10]" ;
            String sFormatColumn = "" ;
            sFormatColumn = "[C" + String.valueOf(columnList.get(0)) + ":C" + String.valueOf(columnList.get(columnList.size()-1)) + "]";
            //System.out.println("sFormatColumn::"+ sFormatColumn);

            //String sCSVFileName = "default.sep.mint.out.csv" ;
            Long lHeaders = 2L;
            String sRows = "[R3 - Rn]" ;
            String sColumns = "[C5, C9 - Cn]" ;
            String sFormat = "$#,##0.00_);($#,##0.00)" ;
            
            String sHeader = "[R2]" ;
            String sArea = "[R2:C1][Rn:Cn]" ;
            String sFormatFormat =  "$#,##0.00_);($#,##0.00)" ; //sFormat ;
            */

            aJSON.setCSVFileName(this.getCsvFileName());
            aJSON.setHeaders(Long.valueOf(this.getlHeaders()));
            aJSON.setTable(this.getsRowsAsString(), this.getsColumnsAsString(), this.getsFormat());
            aJSON.setFormat(this.getsFormatColumnAsString(), this.getsFormatFormatAsString());

            HashMap oFormat = aJSON.getFormat();
            //System.out.println("oFormat::"+ oFormat.toString());

            aJSON.setPivot(this.getsHeaderAsString(), this.getsArea(), oFormat);

            return aJSON;
        } catch (Exception e) {
            return aJSON ; 
        }
    }

    public void dump() {
//        System.out.println("dump::_SheetProperties");
        final String sSep = "|";
        System.out.println(csvFileName + sSep + lHeaders + sSep + getsRowsAsString() + sSep + getsColumnsAsString() + sSep + getsFormat() + sSep + getsHeaderAsString() + sSep + getsFormatColumnAsString() + sSep + getsFormatFormat()) ;
    }
}