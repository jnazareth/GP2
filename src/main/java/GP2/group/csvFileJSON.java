package GP2.group;

import GP2.xls._SheetProperties ;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class csvFileJSON extends Object {
    /*
    "csvFileName":"default.sep.mint.out.csv",
    "headers":2,
    "pivot":{
        "header":"header value",
        "area":"area value",
        "format":{
                "columns":"columns value",
                "format":"format value"
                }
            },
    "table":{
        "rows":"rows value",
        "columns":"columns value",
        "format":"format value"
        }
     */

    private String sCSVFileName;
    private Long lHeaders ; 
    private HashMap<String, String> mapTable ;
    private HashMap<String, String> mapFormat ;
    private HashMap<String, Object> mapPivot ;

    void csvFileJSON() {
        lHeaders = null ; 
        mapTable = null ;
        mapFormat = null ; 
        mapPivot = null ;  
    }

    public void setCSVFileName (String sName){
        sCSVFileName = sName ; 
    }

    public String getCSVFileName() {
        return sCSVFileName ;
    }

    public void setHeaders (Long l) {
        lHeaders = l;
    }

    public Long getHeaders() {
        return lHeaders ; 
    }

    public void setTable(String r, String c, String f) {
        if (mapTable == null) mapTable = new HashMap<String, String>() ;
        if (r != null) mapTable.put("rows", r);
        if (c != null) mapTable.put("columns", c);
        if (f != null) mapTable.put("format", f); 
    }

    public HashMap getTable() {
        return mapTable ; 
    }

    public void setFormat(String c, String f) {
        if (mapFormat == null) mapFormat = new HashMap<String, String>();
        if (c != null) mapFormat.put("columns", c);
        if (f != null) mapFormat.put("format", f);
    }

    public HashMap getFormat () {
        return mapFormat ; 
    }

    public void setPivot(String h, String a, HashMap<String, Object> f) {
        if (mapPivot == null) mapPivot = new HashMap<String, Object>();
        if (h != null) mapPivot.put("header", h);
        if (a != null) mapPivot.put("area", a);
        if (f != null) mapPivot.put("format", f);
    }

    public HashMap getPivot() {
        return mapPivot ; 
    }

    csvFileJSON createCSVFileJSON(String sJSON) {
        csvFileJSON csvSettings = null ; 

        try{
            csvSettings = new csvFileJSON();

            String sCSVFileName = "" ;
            Long lHeaders = 0L;
            String sRows = "" ;
            String sColumns = "" ;
            String sFormat = "" ;
            
            String sHeader = "" ;
            String sArea = "" ;
            String sFormatColumn = "" ;
            String sFormatFormat = sFormat ;

            JSONParser jsonParser = new JSONParser();
            Object oParser = jsonParser.parse(sJSON);
            JSONObject jo = (JSONObject) oParser;

            sCSVFileName = (String) jo.get("csvFileName");
            csvSettings.setCSVFileName(sCSVFileName);

            lHeaders = (Long) jo.get("headers"); 
            csvSettings.setHeaders(lHeaders);

            // getting table 
            Map table = ((Map)jo.get("table"));          
            Iterator<Map.Entry> itr1 = table.entrySet().iterator(); 
            while (itr1.hasNext()) { 
                Map.Entry pair = itr1.next(); 
                //System.out.println(pair.getKey() + " : " + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase("rows")) {
                    csvSettings.setTable(pair.getValue().toString(), null, null);
                } else if (pair.getKey().toString().equalsIgnoreCase("columns")) {
                    csvSettings.setTable(null, pair.getValue().toString(), null);
                } else if (pair.getKey().toString().equalsIgnoreCase("format")) {
                    csvSettings.setTable(null, null, pair.getValue().toString());
                }
            }

            // getting pivot 
            Map pivot = ((Map)jo.get("pivot"));          
            Iterator<Map.Entry> itr2 = pivot.entrySet().iterator(); 
            while (itr2.hasNext()) { 
                Map.Entry pair = itr2.next(); 
                //System.out.println(pair.getKey() + " : " + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase("header")) {
                    csvSettings.setPivot(pair.getValue().toString(), null, null);
                } else if (pair.getKey().toString().equalsIgnoreCase("area")) {
                    csvSettings.setPivot(null, pair.getValue().toString(), null);
                } else if (pair.getKey().toString().equalsIgnoreCase("format")) {
                    JSONObject jo2 = (JSONObject) pair.getValue();

                    // getting format
                    Map format = ((Map)jo2);
                    Iterator<Map.Entry> itr3 = format.entrySet().iterator();
                    while (itr3.hasNext()) { 
                        Map.Entry pair1 = itr3.next(); 
                        //System.out.println(pair1.getKey() + " : " + pair1.getValue());
                        if (pair.getKey().toString().equalsIgnoreCase("columns")) {
                            csvSettings.setFormat(pair.getValue().toString(), null);
                        } else if (pair.getKey().toString().equalsIgnoreCase("format")) {
                            csvSettings.setFormat(null, pair.getValue().toString());
                        }        
                    }
                    HashMap oFormat = csvSettings.getFormat();
                    csvSettings.setPivot(null, null, oFormat);                
                }
            }
            //csvSettings.dump("Create from String");

            return csvSettings;
        } catch (Exception e) {
            return csvSettings ; 
        }
    }

    csvFileJSON createCSVFileJSON(ArrayList<Integer> columnList, String sCSVFileName) {
        csvFileJSON aJSON = null;
        try{
            aJSON = new csvFileJSON();

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

            aJSON.setCSVFileName(sCSVFileName);
            aJSON.setHeaders(lHeaders);
            aJSON.setTable(sRows, sColumns, sFormat);
            aJSON.setFormat(sFormatColumn, sFormatFormat);

            HashMap oFormat = aJSON.getFormat();
            //System.out.println("oFormat::"+ oFormat.toString());

            aJSON.setPivot(sHeader, sArea, oFormat);

            return aJSON;
        } catch (Exception e) {
            return aJSON ; 
        }
    }

    public csvFileJSON createCSVFileJSON(_SheetProperties sp) {
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
            String sArea = "[R2:C1][Rn:Cn]" ;

            aJSON.setCSVFileName(sp.getCsvFileName());
            aJSON.setHeaders(Long.valueOf(sp.getlHeaders()));
            aJSON.setTable(sp.getsRowsAsString(), sp.getsColumnsAsString(), sp.getsFormat());
            aJSON.setFormat(sp.getsFormatColumnAsString(), sp.getsFormatFormatAsString());

            HashMap oFormat = aJSON.getFormat();
            //System.out.println("oFormat::"+ oFormat.toString());

            aJSON.setPivot(sp.getsHeaderAsString(), sArea, oFormat);

            return aJSON;
        } catch (Exception e) {
            return aJSON ; 
        }
    }

    public _SheetProperties toSheetProperties() {
		String csvFileName = null ;
		Long lHeaders = null ;
        String sRows = null ;
        String sColumns = null ;
        String sFormat = null ;
        String sHeader  = null ;
        String sArea = null ;
        String sFormatColumn = null ;
        String sFormatFormat = null ;

		csvFileName = this.getCSVFileName() ;
		lHeaders = this.getHeaders() ;

		// getting table 
		HashMap<String, String> mapTable = this.getTable() ;
		for (Map.Entry<String,String> pair : mapTable.entrySet()) {  
			//System.out.println("Key = " + pair.getKey() + ", Value = " + pair.getValue()); 
			if (pair.getKey().toString().equalsIgnoreCase("rows")) {
				sRows = pair.getValue().toString();
			} else if (pair.getKey().toString().equalsIgnoreCase("columns")) {
				sColumns = pair.getValue().toString();
			} else if (pair.getKey().toString().equalsIgnoreCase("format")) {
				sFormat = pair.getValue().toString();
			}
		}

		// getting pivot 
		HashMap<String, Object> mapPivot = this.getPivot() ;
		for (Map.Entry<String,Object> pair2 : mapPivot.entrySet()) {  
				if (pair2.getKey().toString().equalsIgnoreCase("header")) {
                    sHeader = pair2.getValue().toString();
			} else if (pair2.getKey().toString().equalsIgnoreCase("area")) {
				sArea = pair2.getValue().toString();
			} else if (pair2.getKey().toString().equalsIgnoreCase("format")) {
				HashMap<String, Object> f = (HashMap<String, Object>) pair2.getValue();

				for (Map.Entry<String,Object> entry : f.entrySet())  {
					//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 	
					if (entry.getKey().toString().equalsIgnoreCase("columns")){
						sFormatColumn  = entry.getValue().toString();
					} else if (entry.getKey().toString().equalsIgnoreCase("format")){
						sFormatFormat = entry.getValue().toString();
					}
				}
			}
		}

        _SheetProperties sp = new _SheetProperties() ; 
        sp.setCsvFileName(csvFileName) ;
        sp.setlHeaders(lHeaders.intValue());
        sp.setsRows(sRows);
        sp.setsColumns(sColumns);
        sp.setsFormat(sFormat);
        sp.setsHeader(sHeader);
        sp.setsArea(sArea);
        sp.setsFormatColumn(sFormatColumn);
        sp.setsFormatFormat(sFormatFormat);
        return sp ;
    }

    void dump(String info) {
        System.out.println("dump::----- " + info);
        System.out.println("lHeaders:"+ lHeaders);
        if (mapTable != null) mapTable.forEach((k, v) -> System.out.println(k + ":" + v));
        if (mapPivot != null) mapPivot.forEach((k, v) -> System.out.println(k + ":" + v));
        /*for (Map.Entry<String,String> mapElement : mapPivot.entrySet()) {
            String key = mapElement.getKey();
            String value = mapElement.getValue();
            System.out.println(key + "=" + value);
        }*/
        System.out.println("dump::----- " + info);
    }
}
