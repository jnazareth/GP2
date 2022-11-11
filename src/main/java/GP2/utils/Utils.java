package GP2.utils;

import GP2.person.Person;
import GP2.group.groupCsvJsonMapping;
import GP2.cli.Settings;

import java.util.Hashtable;
import java.math.BigDecimal;

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
}