package GP2.json;

import java.time.LocalTime;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import GP2.group.groupCsvJsonMapping;
import GP2.utils.fileUtils;
    
public class WriteJsonMap implements Runnable {
    private String _mapFile ;
    private String _sFilename ;
    private groupCsvJsonMapping _jsonMap ;

    public WriteJsonMap(String taskName, String sFilename, groupCsvJsonMapping jsonMap) {
        this._mapFile = taskName;
        this._sFilename = sFilename;
        this._jsonMap = jsonMap ;
    }

    @Override
    public void run() {
		long mStartTime = System.currentTimeMillis();
        this.writeJSONMapFile(_mapFile, _sFilename, _jsonMap);

        long mEndTime = System.currentTimeMillis();
		long mTime = (mEndTime - mStartTime);
		long d = 1; // to seconds
        double t = (double) mTime / d;
        //System.out.println(LocalTime.now()+ "::Completed: " + _mapFile + ", " + Thread.currentThread().getName() + ", elapsed: " + t);
    }

    public String writeJSONMapFile(String mapFile, String sFilename, groupCsvJsonMapping jsonMap) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        jsonMap._groupMap.forEach((sKey, cj) -> {
            jsonArrayBuilder.add(Json.createObjectBuilder()
                .add(JSONKeys.keyGroupName, sKey)
                .add(JSONKeys.keyCsvFile, cj._sCSVFile)
                .add(JSONKeys.keyCsvJSONFile, cj._sCSVJSONFile));
        });
        JsonArray jsonArray = jsonArrayBuilder.build();
        JsonObject jsonFileMap = Json.createObjectBuilder()
            .add(JSONKeys.keyFilename, sFilename)
            .add(JSONKeys.keyMapping, jsonArray)
            .build();
        fileUtils.writeJsonToFile(mapFile, jsonFileMap);
        return mapFile;
    }
}
