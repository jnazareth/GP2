package GP2.json;

import GP2.group.csvFileJSON;
import GP2.xls._SheetProperties;
import GP2.group.groupCsvJsonMapping;
import GP2.utils.fileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadJson2 {
    private static final Logger logger = LogManager.getLogger(ReadJson2.class);

    private JSONParser jsonParser = new JSONParser();

    public csvFileJSON readJSON(String fName) {
        try (FileReader reader = fileUtils.createFileReader(fName)) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            return parseCsvFile(jsonObject);
        } catch (FileNotFoundException e) {
            // Handle exception (e.g., log it)
            logger.error(e.getMessage());
        } catch (IOException | ParseException e) {
            // Handle exception (e.g., log it)
            logger.error(e.getMessage());
        }
        return null;
    }

    private csvFileJSON parseCsvFile(JSONObject jsonObject) {
        csvFileJSON csvSettings = new csvFileJSON();
        csvSettings.setCSVFileName((String) jsonObject.get(JSONKeys.keyCsvFile));
        csvSettings.setHeaders((Long) jsonObject.get(JSONKeys.keyHeaders));

        parseTable((Map<String, Object>) jsonObject.get(JSONKeys.keyTable), csvSettings);
        parsePivot((Map<String, Object>) jsonObject.get(JSONKeys.keyPivot), csvSettings);

        return csvSettings;
    }

    private void parseTable(Map<String, Object> table, csvFileJSON csvSettings) {
        for (Map.Entry<String, Object> entry : table.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue().toString();
            switch (key) {
                case JSONKeys.keyRows:
                    csvSettings.setTable(value, null, null);
                    break;
                case JSONKeys.keyColumns:
                    csvSettings.setTable(null, value, null);
                    break;
                case JSONKeys.keyFormat:
                    csvSettings.setTable(null, null, value);
                    break;
            }
        }
    }

    private void parsePivot(Map<String, Object> pivot, csvFileJSON csvSettings) {
        for (Map.Entry<String, Object> entry : pivot.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.equals(JSONKeys.keyHeader)) {
                csvSettings.setPivot(entry.getValue().toString(), null, null);
            } else if (key.equals(JSONKeys.keyArea)) {
                csvSettings.setPivot(null, entry.getValue().toString(), null);
            } else if (key.equals(JSONKeys.keyFormat)) {
                parseFormat((JSONObject) entry.getValue(), csvSettings);
            }
        }
    }

    private void parseFormat(JSONObject formatObject, csvFileJSON csvSettings) {
        for (Object key : formatObject.keySet()) {
            String keyStr = key.toString().toLowerCase();
            String value = formatObject.get(key).toString();
            if (keyStr.equals(JSONKeys.keyColumns)) {
                csvSettings.setFormat(value, null);
            } else if (keyStr.equals(JSONKeys.keyFormat)) {
                csvSettings.setFormat(null, value);
            }
        }
        HashMap<String, String> formatMap = csvSettings.getFormat();
        HashMap<String, Object> formatMapObject = new HashMap<>(formatMap);
        csvSettings.setPivot(null, null, formatMapObject);
    }

    public groupCsvJsonMapping readJSONMapFile(String mapFile) {
        try (FileReader reader = fileUtils.createFileReader(mapFile)) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            return parseGroupCsvJsonMapping(jsonObject);
        } catch (FileNotFoundException e) {
            // Handle exception (e.g., log it)
            logger.error(e.getMessage());
        } catch (IOException | ParseException e) {
            // Handle exception (e.g., log it)
            logger.error(e.getMessage());
        }
        return null;
    }

    private groupCsvJsonMapping parseGroupCsvJsonMapping(JSONObject jsonObject) {
        groupCsvJsonMapping gMapping = new groupCsvJsonMapping();
        //String csvFile = (String) jsonObject.get(JSONKeys.keyFilename);

		JSONArray joMap = (JSONArray)jsonObject.get(JSONKeys.keyMapping);
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
        return gMapping;
    }
}