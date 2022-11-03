package GP2.utils;

import GP2.person.Person;

import java.util.Hashtable;
import java.math.BigDecimal;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;


public class Utils {
    // declarations

	// members
	public static Hashtable<String, Person> /* m_Persons, replaced by Group */ m_System ;
	public static Hashtable<String, Hashtable<String, Person>> m_GroupCollection ;
	public static boolean m_bClearing = false ;
	public static boolean m_bSys = false ;
	
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

	public static boolean inMatches(String in, String toCompare)
	{
		 return ( (in.length() == toCompare.length()) && (in.compareToIgnoreCase(toCompare) == 0) ) ; 
	}

	public static boolean deleteFile (String folder, String extension)
	{
		final File dir = new File(folder) ;
		final File[] list = dir.listFiles( new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				//System.out.println( "name:" + name );
				return (name.endsWith(extension));
			}
		} );
		for ( final File file : list ) {
			if ( !file.delete() ) {
				System.err.println( "Can't remove " + file.getAbsolutePath() );
			}
		}
		return true ; 
	}

	public static File getFile(String fileName)
	throws FileNotFoundException
	{
		File aFile = new File(fileName);
		if (aFile.exists()) return aFile;
		else throw new FileNotFoundException("File  " + fileName + " does not exist.");
	}
}