import java.util.Hashtable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import javax.swing.filechooser.FileFilter;
import java.io.FilenameFilter;

public class Utils {
    // declarations

	// members
	public static Hashtable<String, Person> /* m_Persons, replaced by Group */ m_System ;
	public static Hashtable<String, Hashtable<String, Person>> m_GroupCollection ;
	public static boolean m_bSys = false ;
	
	// methods
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
}