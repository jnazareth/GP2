package GP2.json;

import GP2.group.csvFileJSON;
import GP2.utils.Utils;
import GP2.group.groupCsvJsonMapping;

import java.util.Map;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.io.FileWriter;
import java.io.IOException;

import javax.json.JsonObject ;
import javax.json.Json;

import org.apache.commons.io.FileUtils ;
import java.io.FileOutputStream ;
import java.io.File;

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

        csvFileName = cjJSON.getCSVFileName();
		lHeaders = cjJSON.getHeaders() ;

		// getting table 
		HashMap<String, String> mapTable = cjJSON.getTable() ;
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
		HashMap<String, Object> mapPivot = cjJSON.getPivot() ;
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

        JsonObject joCSV2 = Json.createObjectBuilder()
                            .add("csvFileName", csvFileName)
                            .add("headers", lHeaders)
                            .add("table", Json.createObjectBuilder()
                                .add("rows", sRows)
                                .add("columns", sColumns)
                                .add("format", sFormat))
                            .add("pivot", Json.createObjectBuilder()
                                .add("header", sHeader)
                                .add("area", sArea)
                                .add("format", Json.createObjectBuilder()
                                    .add("columns", sFormatColumn)
                                    .add("format", sFormatFormat)
                                    ))
                            .build();

        //Write JSON file
//		try (FileWriter file = new FileWriter(fName)) {
		try {
			String outFilename = fName;
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;
			FileWriter file = fw;

			//We can write any JSONArray or JSONObject instance to the file
            JSONObject jo = new JSONObject(joCSV2);
			file.write(jo.toJSONString());
			file.flush();

			// close all file handles
			file.close();
			fw.close() ;
			foS.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public String writeJSONMapFile(String mapFile, String sFilename, groupCsvJsonMapping jsonMap) {
	    JSONObject joFileMap = new JSONObject();
		joFileMap.put("filename", sFilename);

		Map<String, String> mapFiles = new HashMap<>();
        JSONArray joArray = new JSONArray();
        for(String sKey: jsonMap._groupMap.keySet()) {
            groupCsvJsonMapping._CSV_JSON cj = jsonMap._groupMap.get(sKey) ;

			mapFiles.clear();
            mapFiles.put("groupName", sKey);
			mapFiles.put("csvFile", cj._sCSVFile);
			mapFiles.put("csvJSONFile", cj._sCSVJSONFile);
		    JSONObject joMap = new JSONObject(mapFiles);
			joArray.add(joMap);
        }
        joFileMap.put("mapping", joArray);

        String fName = mapFile ;
        //Write JSON file
//		try (FileWriter file = new FileWriter(fName)) {
		try {
			String outFilename = fName;
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, outFilename);
            FileOutputStream foS = FileUtils.openOutputStream(f) ;
            FileWriter fw = new FileWriter(foS.getFD()) ;
			FileWriter file = fw;

			//We can write any JSONArray or JSONObject instance to the file
            JSONObject jo = new JSONObject(joFileMap);
			file.write(jo.toJSONString());
			file.flush();

			// close all file handles
			file.close();
			fw.close() ;
			foS.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
        return fName ;
    }
}
