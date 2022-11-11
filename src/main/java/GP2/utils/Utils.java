package GP2.utils;

import GP2.person.Person;
import GP2.group.groupCsvJsonMapping;
import GP2.cli.Settings;

import java.util.Hashtable;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class Utils {
    // declarations

	// members
	public static Hashtable<String, Person> /* m_Persons, replaced by Group */ m_System ;
	public static Hashtable<String, Hashtable<String, Person>> m_GroupCollection ;
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