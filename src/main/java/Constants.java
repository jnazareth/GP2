interface Constants {
	//out file strings
    String IN_HELP = "-h" ;
	String IN_VERSION = "-version" ; 
	String IN_CLEAN = "-clean" ; 

	//out file strings
	String OUT_FILESEP = "." ;
	String OUT_EXTENSION = ".csv" ;
	String OUT_FILE = ".out" ;
	String OUT_FOLDER = "." ;

    // transaction indicators
	String _REM = "rem" ;
	String _ALL = "all" ;
	String _SYS = "sys" ;
	String _INDIV = "indiv" ;
	String _UNKNOWN = "unknown" ;
	String _CLEARING = "$" ;
	String _PERCENTAGE = "%" ;
	char 	_COMMENT = '#' ;
	
	//formatting strings
	String lPAD = "lPad[" ;
	String rPAD = "]rPad" ;
	String lBr = "{" ;
	String rBr = "}" ;

    // separators
	String _ITEM_SEPARATOR = "," ;
	String _AMT_INDICATOR = ":" ;
	String _TAB_SEPARATOR = "\t" ;
	String _REGEX_SEPARATOR = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" ;
	String _READ_SEPARATOR = _REGEX_SEPARATOR ;
	String _DUMP_SEPARATOR = " | " ;

	// keys
	String _REM_key = "_REM" ;
	String _ALL_key = "_ALL" ;
	String _SYS_key = "_SYS" ;
	String _INDIV_key = "_INDIV" ;
	String _UNKNOWN_key = "_UNKNOWN" ;
}