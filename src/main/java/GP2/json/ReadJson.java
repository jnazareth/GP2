package GP2.json;

import GP2.group.csvFileJSON;
import GP2.xls._SheetProperties;
import GP2.group.groupCsvJsonMapping;
import GP2.utils.fileUtils;

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

        try {
			FileReader reader = fileUtils.getFileReader(fName) ;

            //Read JSON file
            Object oParser = jsonParser.parse(reader);
            JSONObject jo = (JSONObject) oParser;

            sCSVFileName = (String) jo.get(JSONKeys.keyCsvFile);
            csvSettings.setCSVFileName(sCSVFileName);
            lHeaders = (Long) jo.get(JSONKeys.keyHeaders);
            csvSettings.setHeaders(lHeaders);

            // getting table
            Map table = ((Map)jo.get(JSONKeys.keyTable));
            Iterator<Map.Entry> itr1 = table.entrySet().iterator();
            while (itr1.hasNext()) {
                Map.Entry pair = itr1.next();
	           	//System.out.println("table::" + pair.getKey() + ":\t" + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyRows)) {
                    csvSettings.setTable(pair.getValue().toString(), null, null);
					sRows = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyColumns)) {
                    csvSettings.setTable(null, pair.getValue().toString(), null);
					sColumns = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyFormat)) {
                    csvSettings.setTable(null, null, pair.getValue().toString());
					sFormat = pair.getValue().toString();
                }
            }

            // getting pivot
            Map pivot = ((Map)jo.get(JSONKeys.keyPivot));
            Iterator<Map.Entry> itr2 = pivot.entrySet().iterator();
            while (itr2.hasNext()) {
                Map.Entry pair = itr2.next();
				//System.out.println("pivot::" + pair.getKey() + ":\t" + pair.getValue());
                if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyHeader)) {
                    csvSettings.setPivot(pair.getValue().toString(), null, null);
					sHeader = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyArea)) {
                    csvSettings.setPivot(null, pair.getValue().toString(), null);
					sArea = pair.getValue().toString();
                } else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyFormat)) {
                    JSONObject jo2 = (JSONObject) pair.getValue();

					org.json.JSONObject jObj = new org.json.JSONObject(jo2) ;
					for (String keyStr : jObj.keySet()) {
						Object keyvalue = jObj.get(keyStr);

						//System.out.println("key:"+ keyStr + ",value:\t" + keyvalue);
						if (keyStr.toString().equalsIgnoreCase(JSONKeys.keyColumns)){
                            csvSettings.setFormat(keyvalue.toString(), null);
							sFormatColumn  = keyvalue.toString();
						} else if (keyStr.toString().equalsIgnoreCase(JSONKeys.keyFormat)){
                            csvSettings.setFormat(null, keyvalue.toString());
							sFormatFormat = keyvalue.toString();
						}
					}
	                HashMap oFormat = csvSettings.getFormat();
                    csvSettings.setPivot(null, null, oFormat);
                }
            }
			reader.close();

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

        try {
			FileReader reader = fileUtils.getFileReader(mapFile) ;

            //Read JSON file
            Object oParser = jsonParser.parse(reader);
            org.json.simple.JSONObject jo = (org.json.simple.JSONObject) oParser;

            String csvFile  = (String) jo.get(JSONKeys.keyFilename);
            /*Iterator<String> keys = jo.keySet().iterator();
            while (keys.hasNext()) {
                System.out.println("value: " + keys.next());
            }*/

            JSONArray joMap ;
            joMap = (JSONArray)jo.get(JSONKeys.keyMapping);
            for (int i = 0; i < joMap.size(); i++) {
                //System.out.println(joMap.get(i));
                JSONObject item = (JSONObject)joMap.get(i);
                String gName = (String)item.get(JSONKeys.keyGroupName);
                String sFile = (String)item.get(JSONKeys.keyCsvFile);
                String sJSON = (String)item.get(JSONKeys.keyCsvJSONFile);
                //System.out.println("gName:" + gName + "\t\tsFile:" + sFile + "\t\tsJSON:" + sJSON);

                csvFileJSON cj = new csvFileJSON();
                _SheetProperties sp = new _SheetProperties() ;
                gMapping.addItem(gName, sFile, sJSON, cj, sp);
            }
            //gMapping.dumpCollection();
			reader.close();
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
