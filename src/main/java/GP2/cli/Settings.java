package GP2.cli;

import GP2.utils.Constants;
import java.io.File;

public class Settings {

    private File[] inputs;
    private final _CLIParamater _xls = new _CLIParamater();
    private final _CLIParamater _map = new _CLIParamater();
    private final _CLIParamater _dir = new _CLIParamater();
    private final _CLIParamater _clean = new _CLIParamater();
    private final _CLIParamater _export = new _CLIParamater();
    private final _CLIParamater _json = new _CLIParamater();
    private final _CLIParamater _suppressCS = new _CLIParamater();

    public boolean bCheckSumTransaction = false;
    public boolean bCheckSumGroupTotals = false;
    public boolean bCheckSumIndividualTotals = false;

    public File[] getInputs() {
        return inputs;
    }

    public void setInputs(File[] inputs) {
        this.inputs = inputs;
    }

    public _CLIParamater getPropertyXLS() {
        return _xls;
    }

    public _CLIParamater setPropertyXLS(String name, String value, Boolean used, File file) {
        return setProperty(_xls, name, value, used, file, null);
    }

    public _CLIParamater getPropertyMapFile() {
        return _map;
    }

    public _CLIParamater setPropertyMapFile(String name, String value, Boolean used, File file) {
        return setProperty(_map, name, value, used, file, null);
    }

    public _CLIParamater getPropertyDir() {
        return _dir;
    }

    public _CLIParamater setPropertyDir(String name, String value, Boolean used, File file) {
        return setProperty(_dir, name, value, used, file, null);
    }

    public _CLIParamater getPropertyClean() {
        return _clean;
    }

    public _CLIParamater setPropertyClean(String name, String value, Boolean used) {
        return setProperty(_clean, name, value, used, null, null);
    }

    public _CLIParamater getPropertyExport() {
        return _export;
    }

    public _CLIParamater setPropertyExport(String name, Boolean value, Boolean used) {
        return setProperty(_export, name, null, used, null, value);
    }

    public _CLIParamater getPropertyJson() {
        return _json;
    }

    public _CLIParamater setPropertyJson(String name, Boolean value, Boolean used) {
        return setProperty(_json, name, null, used, null, value);
    }

    public _CLIParamater getPropertySuppressCS() {
        return _suppressCS;
    }

    public _CLIParamater setPropertySuppressCS(String name, Boolean value, Boolean used) {
        return setProperty(_suppressCS, name, null, used, null, value);
    }

    public boolean getSuppressCStoUse() {
        return _suppressCS.bPropertyUsed ? _suppressCS.bPropertyValue : true;
    }

    public String getDirToUse() {
        return _dir.bPropertyUsed && _dir.sPropertyValue != null ? _dir.sPropertyValue : Constants.OUT_FOLDER;
    }

    public String getCleanPatterns() {
        String defaultPattern = String.join(Constants._ITEM_SEPARATOR,
                Constants.DELETE_FILE_WC1, Constants.DELETE_FILE_WC2,
                Constants.DELETE_FILE_WC3, Constants.DELETE_FILE_WC4, Constants.DELETE_FILE_WC5);
        return _clean.bPropertyUsed && _clean.sPropertyValue != null ? _clean.sPropertyValue : defaultPattern;
    }

    private String makeMapFileName(String csvFile) {
        return csvFile + Constants.OUT_MAP_EXTENSION;
    }

    public String getMapFileToUse(String fName) {
        return _map.bPropertyUsed && _map.sPropertyValue != null ? _map.sPropertyValue : makeMapFileName(fName);
    }

    public String getXLSToUse(String fName) {
        return _xls.bPropertyUsed && _xls.sPropertyValue != null ? _xls.sPropertyValue : fName + Constants.OUT_XLS_EXTENSION;
    }

    public Boolean getExportToUse() {
        return _export.bPropertyUsed ? _export.bPropertyValue : true;
    }

    public Boolean getJsonToUse() {
        return _json.bPropertyUsed ? _json.bPropertyValue : true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("inputs:[");
        if (inputs != null) {
            for (File input : inputs) {
                sb.append(",").append(input);
            }
        }
        sb.append("]");
        appendParamaterToString(sb, _xls, _map, _dir, _clean);
        sb.append("\n");
        return sb.toString();
    }

    private void appendParamaterToString(StringBuilder sb, _CLIParamater... params) {
        for (_CLIParamater param : params) {
            if (param != null) {
                sb.append(param);
            }
        }
    }

    private _CLIParamater setProperty(_CLIParamater param, String name, String value, Boolean used, File file, Boolean boolValue) {
        if (name != null) param.sPropertyName = name;
        if (value != null) param.sPropertyValue = value;
        param.bPropertyUsed = used;
        if (file != null) param.hFile = file;
        if (boolValue != null) param.bPropertyValue = boolValue;
        return param;
    }

    public class _CLIParamater {
        String sPropertyName = "";
        String sPropertyValue = null;
        Boolean bPropertyValue = false;
        Boolean bPropertyUsed = false;
        File hFile = null;

        public boolean IsPropertyUsed() {
            return bPropertyUsed;
        }

        @Override
        public String toString() {
            return " [" + sPropertyName + "," + sPropertyValue + "," + bPropertyUsed + "," + hFile + "," + bPropertyValue + "]";
        }
    }
}