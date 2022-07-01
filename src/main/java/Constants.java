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

	// Actions
	String ADD_ITEM = "*" ;
	String ENABLE_ITEM = "+" ;
	String DISABLE_ITEM = "-" ;

	// id markers
	String _ID_SEPARATOR = ":" ;
	String _SELF = ":self" ;
	String _GROUP = ":group" ;
	String _ID_lR = "(" ;
	String _ID_rR = ")" ;
	String _DEFAULT_GROUP = "default" ;
	String S_ACTION_QUOTE = "\"" ;

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