package GP2.xls;

import java.util.HashSet;

class _Coordinates {
    private HashSet<Integer> range;
    private String coords;

    public _Coordinates() {
        this.range = new HashSet<>();
    }

    public _Coordinates(String sR) {
        this.range = buildRange(sR);
        this.coords = sR;
    }

    HashSet<Integer> toCoordsSet() {
        return range;
    }

    String toCoordsString() {
        return coords;
    }

    private void getStartEndRange(String sFormatColumn, int[] start, int[] end) {
        // CONSTANTS
        final String LEFT_BR = "[";
        final String RIGHT_BR = "]";
        final String RANGE = ":";

        int nStart = sFormatColumn.indexOf(LEFT_BR);
        int nRange = sFormatColumn.indexOf(RANGE);
        int nEnd = sFormatColumn.indexOf(RIGHT_BR);

        if (nStart != -1 && nRange != -1 && nEnd != -1) {
            String sStart = sFormatColumn.substring(nStart + 2, nRange);
            String sEnd = sFormatColumn.substring(nRange + 2, nEnd);

            start[0] = Integer.parseInt(sStart);
            end[0] = Integer.parseInt(sEnd);
        }
    }

    private HashSet<Integer> buildRange(String sRange) {
        HashSet<Integer> aRange = new HashSet<>();
        final String ITEM_SEPARATOR = ",";

        String[] pieces = sRange.split(ITEM_SEPARATOR);
        for (String piece : pieces) {
            int[] nStart = {0}, nEnd = {0};
            getStartEndRange(piece.trim(), nStart, nEnd);
            for (int i = nStart[0]; i <= nEnd[0]; i++) {
                aRange.add(i - 1); // 0 based
            }
        }
        return aRange;
    }

    private boolean cellInRange(int n) {
        return range != null && range.contains(n);
    }
}