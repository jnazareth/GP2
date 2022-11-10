package GP2.cli;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Properties;

public class _CommandLine {

    public static final String OPTION_HELP = "?";
    public static final String OPTION_VERSION = "v";

    private final String applicationLauncher;
    private final String applicationVersion;
    private final String helpHeader;
    private final String helpFooter;

    private Map<_Option, _Option.Handler> options;
    private Map<_Option, org.apache.commons.cli.Option> optionsMap;
    private _Option.Handler2 additionalArgsHandler;

    public _CommandLine(String applicationLauncher, String applicationVersion, String helpHeader, String helpFooter) {
        this.applicationLauncher = applicationLauncher;
        this.applicationVersion = applicationVersion;
        this.helpHeader = helpHeader;
        this.helpFooter = helpFooter;

        this.options = new LinkedHashMap<>();
        this.optionsMap = new LinkedHashMap<>();
        this.additionalArgsHandler = null;
    }

    public _CommandLine withOption(_Option option, _Option.Handler handler) {
        this.options.put(option, handler);
        return this;
    }

    public void parse(String... args) {
		// dump
        /*for (Map.Entry<_Option, _Option.Handler> entry: options.entrySet()) {
            System.out.println("option:" + entry.getKey());
        }*/

        try {
            CommandLineParser parser = new DefaultParser();
            org.apache.commons.cli.Options cliOptions = new org.apache.commons.cli.Options();
            for (Map.Entry<_Option, _Option.Handler> entry: this.options.entrySet()) {
                _Option option = entry.getKey();

                Option opt = Option.builder(option.optString).build();
                opt.setDescription(option.description);
                opt.setOptionalArg(option.optionalArg);
                if (option.args > 0) opt.setArgs(option.args);
                if (option.longOption != null) opt.setLongOpt(option.longOption);
                if (!Character.toString(option.separator).isEmpty()) {
                    opt.setValueSeparator(option.separator);
                }
                if (option.argName != null) opt.setArgName(option.argName);
                if (option.hasArgs != Option.UNINITIALIZED) opt.hasArgs() ;
                cliOptions.addOption(opt);
                optionsMap.put(option, opt);
            }

            org.apache.commons.cli.Option version = org.apache.commons.cli.Option.builder().desc("Show version")
                    .longOpt("version")
                    .option(OPTION_VERSION)
                    .build();
            cliOptions.addOption(version);

            org.apache.commons.cli.Option help = org.apache.commons.cli.Option.builder().desc("Show this help")
                    .longOpt("help")
                    .option(OPTION_HELP)
                    .build();
            cliOptions.addOption(help);

            org.apache.commons.cli.CommandLine cli = parser.parse(cliOptions, args);
            if (args.length == 0) {
                showHelp(cliOptions, System.err);
                System.exit(2);
            }
            else if (cli.hasOption(OPTION_HELP)) {
                showHelp(cliOptions, System.out);
                System.exit(0);
            }

            if (cli.hasOption(OPTION_VERSION)) {
                System.out.println(applicationLauncher + " " + applicationVersion);
                System.exit(0);
            }

            for (Map.Entry<_Option, org.apache.commons.cli.Option> entry : optionsMap.entrySet()) {
                _Option option = entry.getKey();
                String longOption = entry.getValue().getLongOpt();
                if (cli.hasOption(longOption)) {
                    if (option.args > 0) {
                        _Option.Handler handler = null; 
                        try {
                            String[] optionArgs = cli.getOptionValues(longOption);
                            Properties properties = cli.getOptionProperties(longOption);
                            // optionalArgs needs to trap null Args (NullPointerException thrown)
                            handler = options.get(option);
                            handler.onOption(option, optionArgs, properties);
                        } catch (NullPointerException npe) {
                            //System.err.println("NullPointerException npe:" + npe.getMessage());
                            // re-try with "null" indicator
                            String[] oArgs = new String[] {"null"};
                            handler.onOption(option, oArgs, null);
                        }
                    }
                } else {
                    if (option.required) {
                        System.err.println("Missing required option -" + option.optString + " (--" + option.longOption + ")");
                        System.exit(1);
                    }
                }
            }

            String[] additionalArgs = cli.getArgs();
            if (additionalArgs.length > 0) {
                if (additionalArgsHandler != null)
                    additionalArgsHandler.onOption(null, additionalArgs);
                else {
                    System.err.println("unhandled args " + StringUtils.join(additionalArgs, ", "));
                    System.exit(2);
                }
            }
        } catch (ParseException e) {
            System.err.println("ParseException::" + e.getMessage());
            System.exit(2);
        }
    }

    private void showHelp(Options cliOptions, PrintStream printStream) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<org.apache.commons.cli.Option>() {
            @Override
            public int compare(org.apache.commons.cli.Option o1, org.apache.commons.cli.Option o2) {
                Integer o1idx = indexOf(o1);
                Integer o2idx = indexOf(o2);
                return o1idx.compareTo(o2idx);
            }

            private int indexOf(org.apache.commons.cli.Option o) {
                int pos = 0;
                for (Map.Entry<_Option, org.apache.commons.cli.Option> entry : optionsMap.entrySet()) {
                    if (entry.getValue() == o)
                        break;
                    pos++;
                }
                return pos;
            }
        });

        StringWriter textContainer = new StringWriter();
        formatter.printHelp(
                new PrintWriter(textContainer),
                80, // width
                applicationLauncher + " [OPTIONS...]",
                helpHeader,
                cliOptions,
                2, 2, // padding
                helpFooter);
        printStream.println(textContainer.toString());
    }

    public _CommandLine onAdditionalArgs(_Option.Handler2 handler) {
        this.additionalArgsHandler = handler;
        return this;
    }

    public static class _Option {

        private final String optString;
        private final String description;

        private boolean required;
        private String longOption;
        private int args;
        private boolean optionalArg;
        private char separator ;
        private String argName;
        private int hasArgs ;

        public _Option(String optS, String description) {
            this.optString = optS;
            this.description = description;

            this.required = true;
            this.longOption = null;
            this.args = 0;
            this.optionalArg = false;
            //this.separator = '='; // not reqd, this is default
            this.argName = null ;
            this.hasArgs = Option.UNINITIALIZED ;
        }

        public _Option required() {
            this.required = true;
            return this;
        }

        public _Option notRequired() {
            this.required = false;
            return this;
        }

        public _Option longOption(String longOption) {
            this.longOption = longOption;
            return this;
        }

        public _Option args(int args) {
            this.args = args;
            return this;
        }

        public _Option optionalArg(Boolean bArgs) {
            this.optionalArg = bArgs;
            return this;
        }

        public _Option valueSeparator(char c) {
            this.separator = c ;
            return this;
        }

        public _Option argName(String n) {
            this.argName = new String(n) ;
            return this;
        }

        public _Option hasArgs() {
            this.hasArgs = Option.UNLIMITED_VALUES ;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public static interface Handler2 {
            void onOption(_Option option, String[] values);
        }

        public static interface Handler {
            void onOption(_Option option, String[] values, Properties properties);
        }
    }
}
