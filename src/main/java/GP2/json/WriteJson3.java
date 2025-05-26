package GP2.json;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import GP2.group.csvFileJSON;
import GP2.utils.fileUtils;

public class WriteJson3 implements Runnable {
    private String _fName;
    private csvFileJSON _cjJSON;

    public WriteJson3(String taskName, csvFileJSON cjJSON) {
        this._fName = taskName;
        this._cjJSON = cjJSON;
    }

    @Override
    public void run() {
		long mStartTime = System.currentTimeMillis();
        this.writeJSON(_fName, _cjJSON);

        long mEndTime = System.currentTimeMillis();
		long mTime = (mEndTime - mStartTime);
		long d = 1; // to seconds
        double t = (double) mTime / d;
        //System.out.println(LocalTime.now()+ "::Completed: " + _fName + ", " + Thread.currentThread().getName() + ", elapsed: " + t);
    }

    private void writeJSON(String fName, csvFileJSON cjJSON) {
        String csvFileName = cjJSON.getCSVFileName();
        Long headersCount = cjJSON.getHeaders();

        HashMap<String, String> tableMap = cjJSON.getTable();
        HashMap<String, Object> pivotMap = cjJSON.getPivot();

        String rows = getValueFromMap(tableMap, JSONKeys.keyRows);
        String columns = getValueFromMap(tableMap, JSONKeys.keyColumns);
        String format = getValueFromMap(tableMap, JSONKeys.keyFormat);

        String header = getValueFromMap(pivotMap, JSONKeys.keyHeader);
        String area = getValueFromMap(pivotMap, JSONKeys.keyArea);
        HashMap<String, Object> formatMap = (HashMap<String, Object>) pivotMap.get(JSONKeys.keyFormat);
        String formatColumn = getValueFromMap(formatMap, JSONKeys.keyColumns);
        String formatFormat = getValueFromMap(formatMap, JSONKeys.keyFormat);

        JsonObject jsonObject = Json.createObjectBuilder()
                .add(JSONKeys.keyCsvFile, csvFileName)
                .add(JSONKeys.keyHeaders, headersCount)
                .add(JSONKeys.keyTable, Json.createObjectBuilder()
                        .add(JSONKeys.keyRows, rows)
                        .add(JSONKeys.keyColumns, columns)
                        .add(JSONKeys.keyFormat, format))
                .add(JSONKeys.keyPivot, Json.createObjectBuilder()
                        .add(JSONKeys.keyHeader, header)
                        .add(JSONKeys.keyArea, area)
                        .add(JSONKeys.keyFormat, Json.createObjectBuilder()
                                .add(JSONKeys.keyColumns, formatColumn)
                                .add(JSONKeys.keyFormat, formatFormat)))
                .build();

        fileUtils.writeJsonToFile(fName, jsonObject);
    }

    private String getValueFromMap(Map<String, ?> map, String key) {
        return map.containsKey(key) ? map.get(key).toString() : null;
    }
}
