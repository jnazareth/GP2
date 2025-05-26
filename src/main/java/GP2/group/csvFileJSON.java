package GP2.group;

import GP2.xls._SheetProperties;
import GP2.json.JSONKeys;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class csvFileJSON {

    private String csvFileName;
    private Long headers;
    private HashMap<String, String> table;
    private HashMap<String, String> format;
    private HashMap<String, Object> pivot;

    public csvFileJSON() {
        this.headers = null;
        this.table = new HashMap<>();
        this.format = new HashMap<>();
        this.pivot = new HashMap<>();
    }

    public void setCSVFileName(String name) {
        this.csvFileName = name;
    }

    public String getCSVFileName() {
        return csvFileName;
    }

    public void setHeaders(Long headers) {
        this.headers = headers;
    }

    public Long getHeaders() {
        return headers;
    }

    public void setTable(String rows, String columns, String format) {
        if (rows != null) this.table.put(JSONKeys.keyRows, rows);
        if (columns != null) this.table.put(JSONKeys.keyColumns, columns);
        if (format != null) this.table.put(JSONKeys.keyFormat, format);
    }

    public HashMap<String, String> getTable() {
        return table;
    }

    public void setFormat(String columns, String format) {
        if (columns != null) this.format.put(JSONKeys.keyColumns, columns);
        if (format != null) this.format.put(JSONKeys.keyFormat, format);
    }

    public HashMap<String, String> getFormat() {
        return format;
    }

    public void setPivot(String header, String area, HashMap<String, Object> format) {
        if (header != null) this.pivot.put(JSONKeys.keyHeader, header);
        if (area != null) this.pivot.put(JSONKeys.keyArea, area);
        if (format != null) this.pivot.put(JSONKeys.keyFormat, format);
    }

    public HashMap<String, Object> getPivot() {
        return pivot;
    }

    public csvFileJSON createCSVFileJSON(String json) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            csvFileJSON csvSettings = new csvFileJSON();
            csvSettings.setCSVFileName((String) jsonObject.get(JSONKeys.keyCsvFile));
            csvSettings.setHeaders((Long) jsonObject.get(JSONKeys.keyHeaders));

            parseTable(jsonObject, csvSettings);
            parsePivot(jsonObject, csvSettings);

            return csvSettings;
        } catch (Exception e) {
            return null;
        }
    }

    private void parseTable(JSONObject jsonObject, csvFileJSON csvSettings) {
        Map<String, String> table = (Map<String, String>) jsonObject.get(JSONKeys.keyTable);
        table.forEach((key, value) -> {
            if (key.equalsIgnoreCase(JSONKeys.keyRows)) {
                csvSettings.setTable(value, null, null);
            } else if (key.equalsIgnoreCase(JSONKeys.keyColumns)) {
                csvSettings.setTable(null, value, null);
            } else if (key.equalsIgnoreCase(JSONKeys.keyFormat)) {
                csvSettings.setTable(null, null, value);
            }
        });
    }

    private void parsePivot(JSONObject jsonObject, csvFileJSON csvSettings) {
        Map<String, Object> pivot = (Map<String, Object>) jsonObject.get("pivot");
        pivot.forEach((key, value) -> {
            if (key.equalsIgnoreCase(JSONKeys.keyHeader)) {
                csvSettings.setPivot((String) value, null, null);
            } else if (key.equalsIgnoreCase(JSONKeys.keyArea)) {
                csvSettings.setPivot(null, (String) value, null);
            } else if (key.equalsIgnoreCase(JSONKeys.keyFormat)) {
                JSONObject formatObject = (JSONObject) value;
                HashMap<String, Object> formatMap = new HashMap<>();
                formatObject.forEach((formatKey, formatValue) -> {
                    formatMap.put((String) formatKey, formatValue);
                });
                csvSettings.setPivot(null, null, formatMap);
            }
        });
    }

    public csvFileJSON createCSVFileJSON(_SheetProperties sp) {
        csvFileJSON csvSettings = new csvFileJSON();
        csvSettings.setCSVFileName(sp.getCsvFileName());
        csvSettings.setHeaders(Long.valueOf(sp.getlHeaders()));
        csvSettings.setTable(sp.getsRowsAsString(), sp.getsColumnsAsString(), sp.getsFormat());
        csvSettings.setFormat(sp.getsFormatColumnAsString(), sp.getsFormatFormatAsString());
        // Convert HashMap<String, String> to HashMap<String, Object>
        HashMap<String, Object> formatObjectMap = new HashMap<>();
        csvSettings.getFormat().forEach(formatObjectMap::put);
        csvSettings.setPivot(sp.getsHeaderAsString(), sp.getsArea(), formatObjectMap);
        return csvSettings;
    }

    public _SheetProperties toSheetProperties() {
        _SheetProperties sp = new _SheetProperties();
        sp.setCsvFileName(this.getCSVFileName());
        sp.setlHeaders(this.getHeaders().intValue());
        sp.setsRows(this.getTable().get(JSONKeys.keyRows));
        sp.setsColumns(this.getTable().get(JSONKeys.keyColumns));
        sp.setsFormat(this.getTable().get(JSONKeys.keyFormat));
        sp.setsHeader((String) this.getPivot().get(JSONKeys.keyHeader));
        sp.setsArea((String) this.getPivot().get(JSONKeys.keyArea));
        sp.setsFormatColumn((String) this.getFormat().get(JSONKeys.keyColumns));
        sp.setsFormatFormat((String) this.getFormat().get(JSONKeys.keyFormat));
        return sp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(headers);
        table.forEach((k, v) -> sb.append(k).append(":").append(v));
        pivot.forEach((k, v) -> sb.append(k).append(":").append(v));
        return sb.toString();
    }
}