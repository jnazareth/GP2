package GP2.xls;

import java.util.HashSet;

class _Coordinates {
	HashSet<Integer> _range = null ;
	String		_coords ;

    public void _Coordinates() {
		_range = new HashSet<Integer>() ;
	}

    public _Coordinates(String sR) {
		if (_range != null) _range = new HashSet<Integer>() ;
		_range = buildRange(sR) ;
        _coords = sR;
    }

	HashSet<Integer> toCoordsSet() {
		return _range ;
	}

	String toCoordsString() {
		return _coords ;
	}

    private void getStartEndRange(String sFormatColumn, int s[], int e[]) {
		// CONSTANTS
        final String sLeftBr = "[";
        final String sRightBr = "]";
        final String sRange = ":";
        final String sSeparator = ",";

        String sStart = "", sEnd = "" ;
        //String sFormatColumn = "[C9:C10]" ;
        int nStart = sFormatColumn.indexOf(sLeftBr) ;
        int nRange = sFormatColumn.indexOf(sRange) ;
        int nEnd = -1 ;
        if ((nStart != -1) && (nRange != -1)) {
            sStart = sFormatColumn.substring(nStart + 2 /* skip column indicator: 'C' */, nRange) ;
            nEnd = sFormatColumn.indexOf(sRightBr);
            if (nEnd != -1){
                sEnd = sFormatColumn.substring(nRange + 2, nEnd) ;
            }
        }
        //System.out.println("nStart,nRange,nEnd,sStart,sEnd:" + nStart + "," + nRange + "," + nEnd + "," + sStart + "," + sEnd);
        int x = Integer.parseInt(sStart);
        s[0] = x ;
        x = Integer.parseInt(sEnd) ;
        e[0] = x ;
    }

	private HashSet<Integer> buildRange(String sRange) {
        HashSet<Integer> aRange = new HashSet<Integer>() ;
        final String _ITEM_SEPARATOR = "," ;
        String[] pieces = sRange.split(_ITEM_SEPARATOR);
        for (String p : pieces) {
            int nStart[] = {0}, nEnd[] = {0};
            getStartEndRange(p.trim(), nStart, nEnd) ;
            for (int i = nStart[0]; i <= nEnd[0]; i++) {
                aRange.add(i-1) ;     // 0 based
            }
        }
        return aRange;
    }

    private boolean cellInRange(int n) {
        if (_range == null) return false ;
        return _range.contains(n) ;
    }
}