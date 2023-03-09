package GP2.cli;

import GP2.account.account;
import GP2.utils.Constants;
import GP2.utils.fileUtils;
import GP2.utils.Utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class gpcli {
    final String LAUNCHER = System.getProperty("application.launcher", "java -jar GP2.jar");
    final String UNKNOWN_VERSION = "?.?";

    public Settings parseCommandLine(String[] args) {
        Utils.m_settings = new Settings();

        String version = versionFrom("/META-INF/maven/GP2/gp2/pom.properties");
                new _CommandLine(
                    LAUNCHER,
                    version,
                    LAUNCHER + " " + version + System.lineSeparator() +
                        "Breaks down group expenses. Each group's broken down output may be automatically formatted to a XLSX;" +
                        "with each group in separate sheets and a pivot table holding expense categories & aggregations " +
                        "for each individual in the group.",
                    "Exit status:" + System.lineSeparator() +
                        "0  : if OK" + System.lineSeparator() +
                        "1  : if an error occurs" + System.lineSeparator() +
                        "2  : on a syntax error" + System.lineSeparator()
                )
                .withOption (
                        new _CommandLine._Option("x", "Generated Excel file. [default = <filename>.xls]")
                                .longOption("xlsx")
                                .notRequired()
                                .args(1)
                                .optionalArg(true),
                        new _CommandLine._Option.Handler() {
                            @Override
                            public void onOption(_CommandLine._Option option, String[] values, Properties properties) {
                                final String nullVal = "null" ;
                                final String sKey = "xls" ;   // Property:xls
                                if ( (values.length == 1) && (values[0].equalsIgnoreCase(nullVal)) )
                                    Utils.m_settings.setPropertyXLS(sKey, null, true, null);    // used but not set
                                else
                                    Utils.m_settings.setPropertyXLS(sKey, values[0], true, new File(values[0]));
                            }
                        }
                )
                .withOption (
                        new _CommandLine._Option("P", "Properties: -map <mapfile>, -dir <outdir>, -clean <regEx>")
                                .longOption("P")
                                .argName("property=value")
                                .hasArgs()
                                .notRequired()
                                .args(3)       //map,dir,clean = 3
                                //.optionalArg(true)
                                .valueSeparator('='),
                        new _CommandLine._Option.Handler() {
                            @Override
                            public void onOption(_CommandLine._Option option, String[] values, Properties properties) {
                                String sKey = "map" ;   // Property:map
                                final String sSingle = "true" ;
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertyMapFile(sKey, null, true, null);   // value not specified
                                    else Utils.m_settings.setPropertyMapFile(sKey, c, true, new File(c));
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertyMapFile(sKey, null, false, null);  // not used
                                }
                                sKey = "dir" ;  // Property:dir
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertyDir(sKey, null, true, null);   // value not specified
                                    else Utils.m_settings.setPropertyDir(sKey, c, true, new File(c));
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertyDir(sKey, null, false, null);  // not used
                                }
                                sKey = "clean" ;    // Property:clean
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertyClean(sKey, null, true);   // value not specified
                                    else Utils.m_settings.setPropertyClean(sKey, c, true);
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertyClean(sKey, null, false);  // not used
                                }
                            }
                        }
                )
                .withOption (
                        new _CommandLine._Option("F", "Flags: -export <true/false>, -json <true/false>, , -suppressCS <true/false>")
                        .longOption("F")
                                .argName("boolean=value")
                                .hasArgs()
                                .notRequired()
                                .args(3)       //export=false (=2 params)
                                //.optionalArg(true)
                                .valueSeparator('='),
                        new _CommandLine._Option.Handler() {
                            @Override
                            public void onOption(_CommandLine._Option option, String[] values, Properties properties) {
                                String sKey = "export" ;   // Property:export
                                final String sSingle = "true" ;
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    Boolean bV = Boolean.valueOf(c);
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertyExport(sKey, true, false);   // value not specified
                                    else Utils.m_settings.setPropertyExport(sKey, bV, true);
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertyExport(sKey, true, false);  // not used
                                }
                                sKey = "json" ;   // Property:json
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    Boolean bV = Boolean.valueOf(c);
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertyJson(sKey, true, false);   // value not specified
                                    else Utils.m_settings.setPropertyJson(sKey, bV, true);
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertyJson(sKey, true, false);  // not used
                                }

                                sKey = "suppressCS" ;   // Property:suppressCS
                                try {
                                    String c = properties.getProperty(sKey).toString() ;
                                    Boolean bV = Boolean.valueOf(c);
                                    if (c.equalsIgnoreCase(sSingle)) Utils.m_settings.setPropertySuppressCS(sKey, true, false);   // value not specified
                                    else Utils.m_settings.setPropertySuppressCS(sKey, bV, true);
                                } catch (NullPointerException ne) {
									Utils.m_settings.setPropertySuppressCS(sKey, true, false);  // not used
                                }
                            }
                        }
                )
                .onAdditionalArgs(new _CommandLine._Option.Handler2() {
                    @Override
                    public void onOption(_CommandLine._Option option, String[] values) {
                        File[] inputs = new File[values.length];
                        for (int i = 0; i < values.length; i++) inputs[i] = new File(values[i]);
                        Utils.m_settings.setInputs(inputs);
                    }
                })
                .parse(args);

        return Utils.m_settings ;
    }

    private boolean processClean() {
        try {
            if (!Utils.m_settings._clean.IsPropertyUsed()) return false ;    // not used

            String dirToUse = Utils.m_settings.getDirToUse() ;
            String sPatterns[] = Utils.m_settings.getCleanPatterns().split(Constants._ITEM_SEPARATOR);
            for (int i = 0; i < sPatterns.length; i++) fileUtils.deleteFile(dirToUse, sPatterns[i]) ;

            // delete directory, if specifed & empty
            Path path = Paths.get(dirToUse);
            if ( (Utils.m_settings._dir.IsPropertyUsed()) && (fileUtils.isEmpty(path)) ) {
                return fileUtils.deleteDirectory(path.toFile()) ;
            }
        } catch (IOException ioe) {
			System.err.println("Exception::" + ioe.getMessage()) ;
        }
        return true;
    }

    private boolean processCommandLineFlags() {
        boolean bXLSSpecified   = Utils.m_settings.getPropertyXLS().IsPropertyUsed();
        boolean bExport         = Utils.m_settings.getExportToUse();
        boolean bJSON           = Utils.m_settings.getJsonToUse();        

        //System.out.println("flags:: XLS|bExport|bJSON:\t" + bXLSSpecified + "|" + bExport + "|" + bJSON) ;

        final String sSetExportMessage = "set -Fexport=true";
        final String sXLSMessage = (bXLSSpecified ? "-x specified" : "-x not specified") ;
		if (bXLSSpecified) {
            if (!bExport)  {
                System.out.println("Invalid Input:: " + sXLSMessage + ", " + sSetExportMessage + " for xls to be generated") ;
                return false ;
            }
        }
        if (bJSON) {
            if (!bExport)  {
                System.out.println("Invalid Input:: -Fjson=" + bJSON + ", " + sSetExportMessage + " for json to be generated") ;
                return false ;
            }
        }
        return true ; 
    }

    public void processCommandLine() {
		account myAccount = new account() ;
        if (!processCommandLineFlags()) return ;

		File[] inputs = Utils.m_settings.getInputs();
		for (File filename : inputs) {
			myAccount.ReadAndProcessTransactions(filename.getName()) ;
            myAccount.writeJson(filename.getName()) ;
            myAccount.buildXLS(filename.getName()) ;
		}
        processClean() ;
    }

    private String versionFrom(String resourceInClasspath) {
        InputStream in = null;
        try {
            in = gpcli.class.getResourceAsStream(resourceInClasspath);
            Properties props = new Properties();
            props.load(in);
            return StringUtils.defaultString(props.getProperty("version"), UNKNOWN_VERSION);
        } catch (IOException e) {
            return UNKNOWN_VERSION;
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }
}
