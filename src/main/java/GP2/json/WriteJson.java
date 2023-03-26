package GP2.json;

import GP2.group.csvFileJSON;
import GP2.group.groupCsvJsonMapping;
import GP2.utils.fileUtils;

import java.util.Map;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.io.FileWriter;
import java.io.IOException;

import javax.json.JsonObject ;
import javax.json.Json;

public class WriteJson {
    public void writeJSON(String fName, csvFileJSON cjJSON) {
        String csvFileName = null;
		Long lHeaders = null;
        String sRows = null ;
        String sColumns = null ;
        String sFormat = null ;
        String sHeader  = null ;
        String sArea = null ;
        String sFormatColumn = null ;
        String sFormatFormat = null ;

		try {
			csvFileName = cjJSON.getCSVFileName();
			lHeaders = cjJSON.getHeaders() ;

			// getting table 
			HashMap<String, String> mapTable = cjJSON.getTable() ;
			for (Map.Entry<String,String> pair : mapTable.entrySet()) {  
				//System.out.println("Key = " + pair.getKey() + ", Value = " + pair.getValue()); 
				if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyRows)) {
					sRows = pair.getValue().toString();
				} else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyColumns)) {
					sColumns = pair.getValue().toString();
				} else if (pair.getKey().toString().equalsIgnoreCase(JSONKeys.keyFormat)) {
					sFormat = pair.getValue().toString();
				}
			}

			// getting pivot 
			HashMap<String, Object> mapPivot = cjJSON.getPivot() ;
			for (Map.Entry<String,Object> pair2 : mapPivot.entrySet()) {  
					if (pair2.getKey().toString().equalsIgnoreCase(JSONKeys.keyHeader)) {
					sHeader = pair2.getValue().toString();
				} else if (pair2.getKey().toString().equalsIgnoreCase(JSONKeys.keyArea)) {
					sArea = pair2.getValue().toString();
				} else if (pair2.getKey().toString().equalsIgnoreCase(JSONKeys.keyFormat)) {
					HashMap<String, Object> f = (HashMap<String, Object>) pair2.getValue();
					for (Map.Entry<String,Object> entry : f.entrySet())  {
						//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 	
						if (entry.getKey().toString().equalsIgnoreCase(JSONKeys.keyColumns)){
							sFormatColumn  = entry.getValue().toString();
						} else if (entry.getKey().toString().equalsIgnoreCase(JSONKeys.keyFormat)){
							sFormatFormat = entry.getValue().toString();
						}
					}
				}
			}

			JsonObject joCSV2 = Json.createObjectBuilder()
								.add(JSONKeys.keyCsvFile, csvFileName)
								.add(JSONKeys.keyHeaders, lHeaders)
								.add(JSONKeys.keyTable, Json.createObjectBuilder()
									.add(JSONKeys.keyRows, sRows)
									.add(JSONKeys.keyColumns, sColumns)
									.add(JSONKeys.keyFormat, sFormat))
								.add(JSONKeys.keyPivot, Json.createObjectBuilder()
									.add(JSONKeys.keyHeader, sHeader)
									.add(JSONKeys.keyArea, sArea)
									.add(JSONKeys.keyFormat, Json.createObjectBuilder()
										.add(JSONKeys.keyColumns, sFormatColumn)
										.add(JSONKeys.keyFormat, sFormatFormat)
										))
								.build();

			//Write JSON file
			try {
				FileWriter file = fileUtils.getFileWriter(fName) ;
				JSONObject jo = new JSONObject(joCSV2);
				file.write(jo.toJSONString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// do nothing
		}
	}

    public String writeJSONMapFile(String mapFile, String sFilename, groupCsvJsonMapping jsonMap) {
	    JSONObject joFileMap = new JSONObject();
		joFileMap.put(JSONKeys.keyFilename, sFilename);

		Map<String, String> mapFiles = new HashMap<>();
        JSONArray joArray = new JSONArray();
        for(String sKey: jsonMap._groupMap.keySet()) {
            groupCsvJsonMapping._CSV_JSON cj = jsonMap._groupMap.get(sKey) ;

			mapFiles.clear();
            mapFiles.put(JSONKeys.keyGroupName, sKey);
			mapFiles.put(JSONKeys.keyCsvFile, cj._sCSVFile);
			mapFiles.put(JSONKeys.keyCsvJSONFile, cj._sCSVJSONFile);
		    JSONObject joMap = new JSONObject(mapFiles);
			joArray.add(joMap);
        }
        joFileMap.put(JSONKeys.keyMapping, joArray);

        String fName = mapFile ;
        //Write JSON file
		try {
			FileWriter file = fileUtils.getFileWriter(fName) ;
            JSONObject jo = new JSONObject(joFileMap);
			file.write(jo.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return fName ;
    }
}