package GP2.utils;

import GP2.person.Person;
import GP2.group.GroupAccount;
import GP2.group.groupCsvJsonMapping;
import GP2.cli.Settings;
import GP2.input.FTType;
import GP2.input.FTType.FromToTypes;

import java.util.Hashtable;
import java.math.BigDecimal;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

public class Utils {
    // declarations

	// members
	public static Hashtable<String, Person> /* m_Persons, replaced by Group */ m_System ;
	//public static Hashtable<String, Hashtable<String, Person>> m_GroupCollection ;
	public static GroupAccount m_GroupCollection ;
	public static boolean m_bClearing = false ;
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

	public static String stripAmount(String inString)
	{
		final String _QUOTE = "\"";
		final String _LEFT_ROUND = "(";
		final String _RIGHT_ROUND = ")";
		final String _DOLLAR = "$";

		int l = inString.indexOf(_QUOTE);
		int r = inString.lastIndexOf("\"");
		if ((l != -1) && (r != -1)) inString = inString.substring(l+1, r) ;		// "
		//System.out.print("\tq" + inString);

		l = inString.indexOf(_LEFT_ROUND);
		r = inString.indexOf(_RIGHT_ROUND);
		if ((l != -1) && (r != -1)) inString = inString.substring(l+1, r) ;		// debit
		//System.out.print("\tb" + inString);

		l = inString.indexOf(_DOLLAR);
		if ((l != -1)) inString = inString.substring(l+1, inString.length()).trim() ;		// $

		return inString;
	}

	public static String stripPercentge(float amt, String in) {
		String sOut = "" ;
		String sIn[] = in.split(Constants._ITEM_SEPARATOR) ;

		String eachName = "" ;
		float eachPer = 0.0f ;
		for (int i = 0; i < sIn.length; i++) {
			eachName = "";	eachPer = 0 ;
			String sEach[] = sIn[i].split(Constants._AMT_INDICATOR) ;
			for (int k = 0; k < sEach.length; k++) {
				int pLoc = -1 ;
				if ((pLoc = sEach[k].indexOf(FromToTypes.Percentage)) == -1) {
					try {
						eachPer = Float.parseFloat(sEach[k]) ;
						sOut += Constants._AMT_INDICATOR + sEach[k] ;
					} catch (NumberFormatException e) {
						eachName = sEach[k].trim() ;
						if (sOut == "") sOut += sEach[k] ;
						else sOut += Constants._ITEM_SEPARATOR + sEach[k] ;
					}
				} else {
					eachPer = Float.parseFloat(sEach[k].substring(0, pLoc)) ;
					float fAmt = amt * eachPer / 100 ;
					sOut += Constants._AMT_INDICATOR + String.valueOf(fAmt) ;
				}
			}
			return sOut ;
		}
		return sOut ;
	}


	public static String roundAmount(float f)
	{
		try {
			int decimalPlace = 2 ;
			BigDecimal bd = new BigDecimal(f);
			bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
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