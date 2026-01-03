package GP2.xls;

import GP2.group.csvFileJSON;
import GP2.xls._Coordinates;

import java.util.HashSet;
import java.util.Optional;
import java.util.HashMap;

public class _SheetProperties {
    private String csvFileName;
    private int lHeaders;
    private _Coordinates sRows;
    private _Coordinates sColumns;
    private String sFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)";
    private _Coordinates sHeader;
    private String sArea = "[R2:R18][C1:C18]"; // hardcoded for now
    private _Coordinates sFormatColumn;
    private String sFormatFormat = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)";

    public int maxColums;
    public int maxRows;
    public int amountLocation;
    public int personLocation;
    public int pivotColumnStart;
    public int pivotColumnEnd;

    private static final String LEFT_BR = "[";
    private static final String RIGHT_BR = "]";
    private static final String RANGE_SEPARATOR = ":";
    private static final String ROW_PREFIX = "R";
    private static final String COLUMN_PREFIX = "C";
    private static final String RANGE_DELIMITER = ",";

    public _SheetProperties build() {
        sRows = new _Coordinates(buildRange(ROW_PREFIX, lHeaders + 1, maxRows + lHeaders));
        sColumns = new _Coordinates(buildColumnsRange());
        sHeader = new _Coordinates(buildRange(ROW_PREFIX, lHeaders, lHeaders));
        sFormatColumn = new _Coordinates(buildRange(COLUMN_PREFIX, pivotColumnStart, pivotColumnEnd));
        return this;
    }

    private String buildRange(String prefix, int start, int end) {
        return LEFT_BR + prefix + start + RANGE_SEPARATOR + prefix + end + RIGHT_BR;
    }

    private String buildColumnsRange() {
        return buildRange(COLUMN_PREFIX, amountLocation, amountLocation) +
               RANGE_DELIMITER +
               buildRange(COLUMN_PREFIX, pivotColumnStart, maxColums);
    }

    public _SheetProperties setCsvFileName(String sFN) {
        this.csvFileName = sFN;
        return this;
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setlHeaders(int lH) {
        this.lHeaders = lH;
    }

    public int getlHeaders() {
        return lHeaders;
    }

    public _SheetProperties setsRows(String sR) {
        this.sRows = new _Coordinates(sR);
        return this;
    }

    public HashSet<Integer> getsRows() {
        return getCoordinatesSet(sRows);
    }

    public String getsRowsAsString() {
        return getCoordinatesString(sRows);
    }

    public _SheetProperties setsColumns(String sC) {
        this.sColumns = new _Coordinates(sC);
        return this;
    }

    public HashSet<Integer> getsColumns() {
        return getCoordinatesSet(sColumns);
    }

    public String getsColumnsAsString() {
        return getCoordinatesString(sColumns);
    }

    public void setsFormat(String sF) {
        this.sFormat = sF;
    }

    public String getsFormat() {
        return sFormat;
    }

    public void setsHeader(String sH) {
        this.sHeader = new _Coordinates(sH);
    }

    public HashSet<Integer> getsHeader() {
        return getCoordinatesSet(sHeader);
    }

    public String getsHeaderAsString() {
        return getCoordinatesString(sHeader);
    }

    public _SheetProperties setsArea(String sA) {
        this.sArea = sA;
        return this;
    }

    public String getsArea() {
        return sArea;
    }

    public _SheetProperties setsFormatColumn(String sFH) {
        this.sFormatColumn = new _Coordinates(sFH);
        return this;
    }

    public HashSet<Integer> getsFormatColumn() {
        return getCoordinatesSet(sFormatColumn);
    }

    public String getsFormatColumnAsString() {
        return getCoordinatesString(sFormatColumn);
    }

    public void setsFormatFormat(String sFF) {
        this.sFormatFormat = sFF;
    }

    public String getsFormatFormatAsString() {
        return Optional.ofNullable(sFormatFormat).orElse("7"); // pivot table value for "$" format
    }

    public Integer getsFormatFormat() {
        return Optional.ofNullable(sFormatFormat).map(format -> 7).orElse(null);
    }

    public csvFileJSON toCsvFileJSON() {
        try {
            csvFileJSON aJSON = new csvFileJSON();
            aJSON.setCSVFileName(this.getCsvFileName());
            aJSON.setHeaders((long) this.getlHeaders());
            aJSON.setTable(this.getsRowsAsString(), this.getsColumnsAsString(), this.getsFormat());
            aJSON.setFormat(this.getsFormatColumnAsString(), this.getsFormatFormatAsString());
            HashMap oFormat = aJSON.getFormat();
            aJSON.setPivot(this.getsHeaderAsString(), this.getsArea(), oFormat);
            return aJSON;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        final String sSep = "|";
        return "[" + csvFileName + sSep + lHeaders + sSep + getsRowsAsString() + sSep + getsColumnsAsString() + sSep + getsFormat() + sSep + getsHeaderAsString() + sSep + getsFormatColumnAsString() + sSep + getsFormatFormat() + "]";
    }

    private HashSet<Integer> getCoordinatesSet(_Coordinates coordinates) {
        return Optional.ofNullable(coordinates).map(_Coordinates::toCoordsSet).orElse(new HashSet<>());
    }

    private String getCoordinatesString(_Coordinates coordinates) {
        return Optional.ofNullable(coordinates).map(_Coordinates::toCoordsString).orElse("");
    }
}