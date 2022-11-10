package GP2.json;

import GP2.group.csvFileJSON;
import GP2.utils.Utils;
import GP2.xls._SheetProperties;
import GP2.group.groupCsvJsonMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils ;
import java.io.FileInputStream ;
import java.io.File;

public class ReadJson {
    public csvFileJSON readJSON(String fName) {
        String sCSVFileName = null;
		Long lHeaders = null;
        String sRows = null ;
        String sColumns = null ;
        String sFormat = null ;

        String sHeader  = null;
        String sArea = null ;
        String sFormatColumn = null ;
        String sFormatFormat = null ;

        csvFileJSON csvSettings = new csvFileJSON() ; 
        JSONParser jsonParser = new JSONParser();

//        try (FileReader reader = new FileReader(fName))
        try {
			String inFilename = fName;
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, inFilename);

            FileInputStream fiS = FileUtils.openInputStream(f) ;
            FileReader fr = new FileReader(fiS.getFD()) ;
            FileReader reader = fr ;

            //Read JSON file
            Object oParser = jsonParser.parse(reader);
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
	           	//System.out.println("table::" + pair.getKey() + ":\t" + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase("rows")) {
                    csvSettings.setTable(pair.getValue().toString(), null, null);
					sRows = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase("columns")) {
                    csvSettings.setTable(null, pair.getValue().toString(), null);
					sColumns = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase("format")) {
                    csvSettings.setTable(null, null, pair.getValue().toString());
					sFormat = pair.getValue().toString();
                }
            }

            // getting pivot 
            Map pivot = ((Map)jo.get("pivot"));          
            Iterator<Map.Entry> itr2 = pivot.entrySet().iterator(); 
            while (itr2.hasNext()) { 
                Map.Entry pair = itr2.next(); 
				//System.out.println("pivot::" + pair.getKey() + ":\t" + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase("header")) {
                    csvSettings.setPivot(pair.getValue().toString(), null, null);
					sHeader = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase("area")) {
                    csvSettings.setPivot(null, pair.getValue().toString(), null);
					sArea = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase("format")) {
                    JSONObject jo2 = (JSONObject) pair.getValue();

					org.json.JSONObject jObj = new org.json.JSONObject(jo2) ;
					for (String keyStr : jObj.keySet()) {
						Object keyvalue = jObj.get(keyStr);

						//System.out.println("key:"+ keyStr + ",value:\t" + keyvalue);
						if (keyStr.toString().equalsIgnoreCase("columns")){
                            csvSettings.setFormat(keyvalue.toString(), null);
							sFormatColumn  = keyvalue.toString();
						} else if (keyStr.toString().equalsIgnoreCase("format")){
                            csvSettings.setFormat(null, keyvalue.toString());
							sFormatFormat = keyvalue.toString();
						}
					}
	                HashMap oFormat = csvSettings.getFormat();
                    csvSettings.setPivot(null, null, oFormat);                
                }
            }

            // close all file handles
			reader.close();
			fr.close() ;
			fiS.close() ;

            return csvSettings ; 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return csvSettings;
    }

    public groupCsvJsonMapping readJSONMapFile(String mapFile) {
        groupCsvJsonMapping gMapping = new groupCsvJsonMapping() ;

        JSONParser jsonParser = new JSONParser();

//        try (FileReader reader = new FileReader(mapFile))
        try {
            String inFilename = mapFile;
            String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, inFilename);
    
            FileInputStream fiS = FileUtils.openInputStream(f) ;
            FileReader fr = new FileReader(fiS.getFD()) ;
            FileReader reader = fr ;

            //Read JSON file
            Object oParser = jsonParser.parse(reader);
            org.json.simple.JSONObject jo = (org.json.simple.JSONObject) oParser;
            
            String csvFile  = (String) jo.get("filename"); 
            /*Iterator<String> keys = jo.keySet().iterator();
            while (keys.hasNext()) { 
                System.out.println("value: " + keys.next());
            }*/

            JSONArray joMap ; 
            joMap = (JSONArray)jo.get("mapping");
            for (int i = 0; i < joMap.size(); i++) {  
                //System.out.println(joMap.get(i));  
                JSONObject item = (JSONObject)joMap.get(i);
                String gName = (String)item.get("groupName");
                String sFile = (String)item.get("csvFile");
                String sJSON = (String)item.get("csvJSONFile");
                //System.out.println("gName:" + gName + "\t\tsFile:" + sFile + "\t\tsJSON:" + sJSON);  

                csvFileJSON cj = new csvFileJSON();
                _SheetProperties sp = new _SheetProperties() ;
                gMapping.addItem(gName, sFile, sJSON, cj, sp);
            }
            //gMapping.dumpCollection();

            // close all file handles
			reader.close();
			fr.close() ;
			fiS.close() ;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return gMapping ;
    }    
}
