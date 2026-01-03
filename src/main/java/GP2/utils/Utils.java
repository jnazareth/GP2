package GP2.utils;

import GP2.person.Person;
import GP2.group.Groups;
import GP2.group.groupCsvJsonMapping;
import GP2.cli.Settings;
import GP2.input.FTType.FromToTypes;

import java.util.Hashtable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

public class Utils {
    // declarations

	// members
	public static Hashtable<String, Person> /* m_Persons, replaced by Group */ m_System ;
	public static Groups m_Groups ;
	public static boolean m_bSys = false ;

	public static groupCsvJsonMapping m_grpCsvJsonMap = null;
	public static Settings m_settings = null;

	// methods
	public static String removeQuotes(String inString)
	{
		// strip quotes from inString
		StringBuilder sb = new StringBuilder(inString);
		int q = -1 ;
		while ((q = sb.indexOf(Constants.S_ACTION_QUOTE)) != -1) sb.deleteCharAt(q) ;
		return sb.toString();
	}

	public static String flipCreditDebit(String inString)
	{
		final String _QUOTE = "\"";
		final String _LEFT_ROUND = "(";
		final String _RIGHT_ROUND = ")";
		final String _DOLLAR = "$";
		final String _CREDIT = "-";

		int l = inString.indexOf(_QUOTE);
		int r = inString.lastIndexOf("\"");
		if ((l != -1) && (r != -1)) inString = inString.substring(l+1, r) ;		// "
		//System.out.print("\tq" + inString);

		int lr = inString.indexOf(_LEFT_ROUND);
		int rr = inString.indexOf(_RIGHT_ROUND);
		if ((lr != -1) && (rr != -1)) inString = inString.substring(lr+1, rr) ;		// debit
		//System.out.println("\tr" + inString);

		int n = inString.indexOf(_CREDIT);
		if ((n != -1)) inString = inString.substring(n+1, inString.length()).trim() ;		// -
		//System.out.print("\tn" + inString);

		int d = inString.indexOf(_DOLLAR);
		if ((d != -1)) inString = inString.substring(d+1, inString.length()).trim() ;		// $
		//System.out.print("\td" + inString);

		if ((lr == -1) && (rr == -1) && (n == -1)) inString = _CREDIT + inString;			// credit
		//System.out.println("\tc" + inString);

		return inString;
	}

	public static String stripPercentge(float amt, String in) {
        StringBuilder sOut = new StringBuilder();
        String[] sIn = in.split(Constants._ITEM_SEPARATOR);

        for (String item : sIn) {
            String[] sEach = item.split(Constants._AMT_INDICATOR);
            for (String part : sEach) {
                int pLoc = part.indexOf(FromToTypes.Percentage);
                if (pLoc == -1) {
                    try {
                        @SuppressWarnings("unused")
						float eachPer = Float.parseFloat(part);
                        sOut.append(Constants._AMT_INDICATOR).append(part);
                    } catch (NumberFormatException e) {
                        if (sOut.length() == 0) {
                            sOut.append(part);
                        } else {
                            sOut.append(Constants._ITEM_SEPARATOR).append(part);
                        }
                    }
                } else {
                    float eachPer = Float.parseFloat(part.substring(0, pLoc));
                    float fAmt = amt * eachPer / 100;
                    sOut.append(Constants._AMT_INDICATOR).append(fAmt);
                }
            }
        }
        return sOut.toString();
	}


	public static String roundAmount(float f)
	{
		try {
			int decimalPlace = 2 ;
			BigDecimal bd = new BigDecimal(f);
			bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
			return bd.toString() ;
		} catch (NumberFormatException e) {
			return e.getMessage() ;
		}
	}

	public static Double truncate (float d) {
		//truncate to 2 decimals
		int base = 10, power = 2;
		Double dFactor = Math.pow(base, power) ;
		return (Math.round(d * dFactor ) / dFactor) ;
	}

	public static String applyXRate (String a, Double r) {
		Float amount = Float.valueOf(a) ;
		Double xAmount = amount * r ;
		return String.valueOf(xAmount) ;
	}

	public static String applyXRateF(float a, Double r) {
		Double xAmount = a * r ;
		return String.valueOf(xAmount) ;
	}

	public static List<String> customSort(Set<String> hm) {
		List<String> sortedMapKeys = new ArrayList<String>(hm);
		try {
			final String sDefault = Constants._DEFAULT_GROUP ;
			if (! hm.contains(sDefault) ) {		// not found, regular sort
				Collections.sort(sortedMapKeys);
				return sortedMapKeys ;
			}

			boolean b = sortedMapKeys.remove(sDefault);
			if (!b) return sortedMapKeys ;

			List<String> sortedMapKeys2 = new ArrayList<String>(hm.size());
			sortedMapKeys2.add(sDefault);
			Collections.sort(sortedMapKeys);
			for (String i : sortedMapKeys)  sortedMapKeys2.add(i);

			return sortedMapKeys2 ;
		} catch (Exception e) {
			System.err.println("Exception::" + e.getMessage());
		}
		return sortedMapKeys ;
	}
}