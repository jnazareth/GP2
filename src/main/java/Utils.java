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
	public static Hashtable<String, Person2> /* m_Persons, replaced by Group */ m_System ;
	public static Hashtable<String, Hashtable<String, Person2>> m_GroupCollection ;
	//public static Hashtable<String, ArrayList<String>> m_exportLinesGroup ;

	public static Hashtable<String, Person3> /* m_Persons, replaced by Group */ m_System3 ;
	public static Hashtable<String, Hashtable<String, Person3>> m_GroupCollection3 ;
	public static boolean m_bSys = false ;

	//CONSTANTS
	// Actions
	public static final String ADD_ITEM = "*" ;
	public static final String ENABLE_ITEM = "+" ;
	public static final String DISABLE_ITEM = "-" ;

	//calculation direction
	public static final int _FR = 0 ;
	public static final int	_TO = 1 ;

	// control row
	public static final char 	CONTROL = '@' ;
	//public static final char 	USE_COLUMN = '+' ;
	//public static final char 	SKIP_COLUMN = '-' ;

	public static final String S_ITEM = "item" ;
	public static final String S_CATEGORY = "category" ;
	public static final String S_VENDOR = "vendor" ;
	public static final String S_DESC = "description" ;
	public static final String S_AMOUNT = "amount" ;
	public static final String S_FROM = "from" ;
	public static final String S_TO = "to" ;
	public static final String S_GROUP = "group" ;
	public static final String S_ACTION = "action" ;
	public static final String S_ACTION_QUOTE = "\"" ;

	/**/
	public static int	P_ITEM ;
	public static int	P_CATEGORY ;
	public static int 	P_VENDOR ;
	public static int	P_DESC ;
	public static int 	P_AMOUNT ;
	public static int	P_FROM ;
	public static int 	P_TO ;
	public static int	P_GROUP ;
	public static int	P_ACTION ;
	/**/

	//formatting strings
	public static final String lPAD = "lPad[" ;
	public static final String rPAD = "]rPad" ;
	public static final String lBr = "{" ;
	public static final String rBr = "}" ;


	//out file strings
	public static final String IN_HELP = "-h" ; 
	public static final String IN_VERSION = "-version" ; 
	public static final String IN_CLEAN = "-clean" ; 
	public static final String IN_FALSE = "false" ; 
	public static final String IN_TRUE = "true" ; 

	//out file strings
	public static final String OUT_FILESEP = "." ;
	public static final String OUT_EXTENSION = ".csv" ;
	public static final String OUT_FILE = ".out" ;
	public static final String OUT_FOLDER = "." ;
	
	// output headers
	public static final String H_TRANSACTION_AMOUNTS = "transaction amounts" ;
	public static final String H_OWE = "(you owe) / owed to you" ;
	public static final String H_INDIVIDUAL_TOTALS = "individual \"spent\"" ;
	public static final String H_ITEM = "Item" ;
	public static final String H_CATEGORY = "Category" ;
	public static final String H_VENDOR = "Vendor" ;
	public static final String H_DESCRIPTION = "Description" ;
	public static final String H_AMOUNT = "Amount" ;
	public static final String H_FROM = "From" ;
	public static final String H_TO = "To" ;
	public static final String H_ACTION = "Action" ;
	public static final String H_CHECKSUM = "CheckSum" ;
	public static final String H_INDCHECKSUM = "IndCheckSum" ;
	public static final String H_INDIVIDUAL_PAID = "individual \"paid\"" ;

    // transaction indicators
	public static final String _REM = "rem" ;
	public static final String _ALL = "all" ;
	public static final String _SYS = "sys" ;
	public static final String _INDIV = "indiv" ;
	public static final String _UNKNOWN = "unknown" ;
	public static final String _CLEARING = "$" ;
	public static final String _PERCENTAGE = "%" ;
	public static final char 	_COMMENT = '#' ;

	// keys
	public static final String _REM_key = "_REM" ;
	public static final String _ALL_key = "_ALL" ;
	public static final String _SYS_key = "_SYS" ;
	public static final String _INDIV_key = "_INDIV" ;
	public static final String _UNKNOWN_key = "_UNKNOWN" ;

    // separators
	public static final String _AMT_INDICATOR = ":" ;
	public static final String _ITEM_SEPARATOR = "," ;
	public static final String _TAB_SEPARATOR = "\t" ;
	public static final String _REGEX_SEPARATOR = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" ;
	public static final String _READ_SEPARATOR = _REGEX_SEPARATOR ;
	public static final String _DUMP_SEPARATOR = " | " ;

	// id markers
	public static final String _ID_SEPARATOR = ":" ;
	public static final String _SELF = ":self" ;
	public static final String _GROUP = ":group" ;
	public static final String _ID_lR = "(" ;
	public static final String _ID_rR = ")" ;
	public static final String _DEFAULT_GROUP = "default" ;

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