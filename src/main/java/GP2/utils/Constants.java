package GP2.utils;

public interface Constants {
	// command line
	final String IN_HELP 		= "-h" ;
	final String IN_VERSION 	= "-version" ; 
	final String IN_CLEAN 		= "-clean" ; 

	// file extensions
	final String OUT_FILESEP 	= "." ;
	final String OUT_EXTENSION 	= ".csv" ;
	final String OUT_FILE 		= ".out" ;
	final String OUT_FOLDER 	= "." ;
	final String OUT_JSON_EXTENSION = ".json" ;
	final String OUT_MAP_EXTENSION 	= ".map" ;
	final String OUT_XLS_EXTENSION 	= ".xlsx" ;
	
	// clean filters
	final String WC_GLOB 			= "glob:" ;
	final String WC_TRANSACTIONS 	= "transactions." ;
	final String WC_TRANSACTION 	= "transactions" ;
	final String WC_ALL 			= "*" ;
	final String DELETE_FILE_WC1	= WC_GLOB + WC_ALL + OUT_FILE + OUT_EXTENSION ;
	final String DELETE_FILE_WC2	= WC_GLOB + WC_TRANSACTIONS + WC_ALL + OUT_EXTENSION ;
	final String DELETE_FILE_WC3	= WC_GLOB + WC_TRANSACTION + OUT_EXTENSION ;
	final String DELETE_FILE_WC4	= WC_GLOB + WC_ALL + OUT_FILE + OUT_JSON_EXTENSION ;
	final String DELETE_FILE_WC5	= WC_GLOB + WC_ALL + OUT_EXTENSION + OUT_MAP_EXTENSION ;

	// id markers
	final String _ID_SEPARATOR 	= ":" ;
	final String _DEFAULT_GROUP	= "default" ;
	final String S_ACTION_QUOTE	= "\"" ;

	// formatting strings
	final String lBr	= "{" ;
	final String rBr	= "}" ;

    // separators
	final String _ITEM_SEPARATOR	= "," ;
	final String _AMT_INDICATOR		= ":" ;
	final String _TAB_SEPARATOR		= "\t" ;
	final String _REGEX_SEPARATOR	= ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" ;
	final String _READ_SEPARATOR	= _REGEX_SEPARATOR ;
	final String _DUMP_SEPARATOR	= " | " ;
}