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

    public void dumpCollection () {
		try {
			if (this._groupMap != null) {
				//System.out.println("dumpCollection ============") ;
				for(String key: _groupMap.keySet()) {
					System.out.println(key);
					_CSV_JSON cj = _groupMap.get(key) ;
					cj.dumpCollection() ;
				}
			}
		} catch (Exception e) {
			System.err.println("Error:dumpCollection2::" + e.getMessage()) ;
		}
		return ;
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

        public void dumpCollection() {
            System.out.print("\t[" + _sCSVFile + "][" + _sCSVJSONFile + "][") ;
            _oCSVFileJSON.dump("dump") ;
            System.out.print("]") ;
			return ;
		}
    }
}
