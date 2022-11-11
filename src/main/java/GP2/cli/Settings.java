package GP2.cli;

import GP2.utils.Constants;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;

public class Settings {

    private File[]  inputs;
    _CLIParamater   _xls    = new _CLIParamater(); 
    _CLIParamater   _map    = new _CLIParamater();
    _CLIParamater   _dir    = new _CLIParamater();
    _CLIParamater   _clean  = new _CLIParamater();
    _CLIParamater   _export = new _CLIParamater();
    _CLIParamater   _json   = new _CLIParamater();

    public File[] getInputs() {
        return inputs;
    }
    public void setInputs(File[] inputs) {
        this.inputs = inputs;
    }

    public _CLIParamater getPropertyXLS() {
        return _xls;
    }
    public _CLIParamater setPropertyXLS(String n, String v, Boolean b, File h) {
        if (_xls == null) _xls = new _CLIParamater() ;
        if (n != null) _xls.sPropertyName = n ;
        if (v != null) _xls.sPropertyValue = v ;
        _xls.bPropertyUsed = b ;
    	if (h != null) _xls.hFile = h ;
    	return _xls ;
    }

    public _CLIParamater getPropertyMapFile() {
        return _map;
    }
    public _CLIParamater setPropertyMapFile(String n, String v, Boolean b, File h) {
        if (_map == null) _map = new _CLIParamater() ;
        if (n != null) _map.sPropertyName = n ;
        if (v != null) _map.sPropertyValue = v ;
        _map.bPropertyUsed = b ;
    	if (h != null) _map.hFile = h ;
    	return _map ;
    }

    public _CLIParamater getPropertyDir() {
        return _dir;
    }
    public _CLIParamater setPropertyDir(String n, String v, Boolean b, File h) {
        if (_dir == null) _dir = new _CLIParamater() ;
        if (n != null) _dir.sPropertyName = n ;
        if (v != null) _dir.sPropertyValue = v ;
        _dir.bPropertyUsed = b ;
    	if (h != null) _dir.hFile = h ;
    	return _dir ;
    }

    public _CLIParamater getPropertyClean() {
        return _clean;
    }
    public _CLIParamater setPropertyClean(String n, String v, Boolean b) {
        if (_clean == null) _clean = new _CLIParamater() ;
        if (n != null) _clean.sPropertyName = n ;
        if (v != null) _clean.sPropertyValue = v ;
        _clean.bPropertyUsed = b ;
    	return _clean ;
    }

    public _CLIParamater getPropertyExport() {
        return _export;
    }
    public _CLIParamater setPropertyExport(String n, Boolean bV, Boolean b) {
        if (_export == null) _export = new _CLIParamater() ;
        if (n != null) _export.sPropertyName = n ;
        _export.bPropertyValue = bV ;
        _export.bPropertyUsed = b ;
    	return _export ;
    }

    public _CLIParamater getPropertyJson() {
        return _json;
    }
    public _CLIParamater setPropertyJson(String n, Boolean bV, Boolean b) {
        if (_json == null) _json = new _CLIParamater() ;
        if (n != null) _json.sPropertyName = n ;
        _json.bPropertyValue = bV ;
        _json.bPropertyUsed = b ;
    	return _json ;
    }
    public String getDirToUse() {
        String sDir = Constants.OUT_FOLDER ;
        if ( (this._dir.bPropertyUsed) && (this._dir.sPropertyValue != null) ) sDir = this._dir.sPropertyValue ;
        return sDir ;
    }

    public String getCleanPatterns() {
        String sPattern =   Constants.DELETE_FILE_WC1 + Constants._ITEM_SEPARATOR +
                            Constants.DELETE_FILE_WC2 + Constants._ITEM_SEPARATOR +
                            Constants.DELETE_FILE_WC3 + Constants._ITEM_SEPARATOR +
                            Constants.DELETE_FILE_WC4 + Constants._ITEM_SEPARATOR +
                            Constants.DELETE_FILE_WC5 ;
        if ( (this._clean.bPropertyUsed) && (this._clean.sPropertyValue != null) ) sPattern = this._clean.sPropertyValue ;
        return sPattern ;
    }    
    
	private String makeMapFileName (String csvFile) {
		return (csvFile + Constants.OUT_MAP_EXTENSION) ;
	}

    public String getMapFileToUse(String fName) {
        String sMap = makeMapFileName(fName) ; 
        if ( (this._map.bPropertyUsed) && (this._map.sPropertyValue != null) ) sMap = this._map.sPropertyValue ;
        return sMap ;
    }

    public String getXLSToUse(String fName) {
        String sXLS = fName + Constants.OUT_XLS_EXTENSION ;
        if ( (this._xls.bPropertyUsed) && (this._xls.sPropertyValue != null) ) sXLS = this._xls.sPropertyValue ;
        return sXLS ;
    }    

    public Boolean getExportToUse() {
        Boolean b = true;
        if ( (this._export.bPropertyUsed) ) b = this._export.bPropertyValue ;
        return b ;
    }

    public Boolean getJsonToUse() {
        Boolean b = true;
        if ( (this._json.bPropertyUsed) ) b = this._json.bPropertyValue ;
        return b ;
    }

    public void dump() {
        System.out.print("inputs:[");
        for (int i = 0; i < inputs.length; i++) System.out.print("," + inputs[i]);
        System.out.print("]");
        //System.out.print(" xlsFile:[" + xlsFile + "]");
        if (_xls != null) _xls.dump() ;
        if (_map != null) _map.dump() ;
        if (_dir != null) _dir.dump() ;
        if (_clean != null) _clean.dump() ;       
        System.out.println("");
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public class _CLIParamater {
        String  sPropertyName = "" ;
        String  sPropertyValue = null ;
        Boolean bPropertyValue = false;
        Boolean bPropertyUsed = false ;
        File    hFile = null ;

        private _CLIParamater() {
        }

        private _CLIParamater(String n, String v, Boolean b, File h, Boolean bV) {
            sPropertyName = n ;
            if (v != null) sPropertyValue = new String(v) ;
            bPropertyUsed = b ;
            if (h != null) hFile = h ;
        }

        public boolean IsPropertyUsed() {
            return bPropertyUsed;
        }

        public void dump () {
            System.out.print(" [" + sPropertyName + "," + sPropertyValue + "," + bPropertyUsed + "," + hFile + "," + bPropertyValue + "]");
        }
    }

}
