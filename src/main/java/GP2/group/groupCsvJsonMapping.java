package GP2.group;

import GP2.xls._SheetProperties;

import java.util.HashMap;

public class groupCsvJsonMapping extends Object{
	public HashMap<String, _CSV_JSON> 	_groupMap = null ; // String/key = groupName

    // constructors
	private void groupCsvJsonMapping (String group, _CSV_JSON cj) {
        if (_groupMap == null) _groupMap = new HashMap<String, _CSV_JSON>() ;
        _groupMap.put(group, cj) ;
	}

    public HashMap addItem (String g, String c, String j, csvFileJSON cj, _SheetProperties sp) {
        if (_groupMap == null) _groupMap = new HashMap<String, _CSV_JSON>() ;
		_groupMap.put(g, new _CSV_JSON(c, j, cj, sp)) ;
		return _groupMap ;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this._groupMap != null) _groupMap.forEach( (k, v) -> sb.append(_groupMap.get(k).toString()) ) ;
		return sb.toString();
	}

	public groupCsvJsonMapping orElse(Object object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'orElse'");
	}

    // --------------------------------------------------------------------------
    public class _CSV_JSON extends Object {
        public String _sCSVFile ; 					// exists if bExport == true
        public String _sCSVJSONFile ;				// exists if bViaJson == true
        public csvFileJSON _oCSVFileJSON ;			// exists if bViaJson == true
		public _SheetProperties _sheetProperties ;	// exists if xlsFile != null

        public _CSV_JSON (String sCsvFile, String sCsvJsonFile, csvFileJSON oCsvJson, _SheetProperties sp){
            _sCSVFile = sCsvFile;
            _sCSVJSONFile = sCsvJsonFile;
            _oCSVFileJSON = oCsvJson;
			_sheetProperties = sp;
        }

		@Override public String toString() {
            return "\t[" + _sCSVFile + "][" + _sCSVJSONFile + "][" + _oCSVFileJSON.toString() + "]" ;
		}
    }
}
