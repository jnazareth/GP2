package GP2.cli;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class _CommandLine {

    public static final String OPTION_HELP = "?";
    public static final String OPTION_VERSION = "v";

    private final String applicationLauncher;
    private final String applicationVersion;
    private final String helpHeader;
    private final String helpFooter;

    private final Map<_Option, _Option.Handler> options = new LinkedHashMap<>();
    private final Map<_Option, Option> optionsMap = new LinkedHashMap<>();
    private _Option.Handler2 additionalArgsHandler;

    public _CommandLine(String applicationLauncher, String applicationVersion, String helpHeader, String helpFooter) {
        this.applicationLauncher = applicationLauncher;
        this.applicationVersion = applicationVersion;
        this.helpHeader = helpHeader;
        this.helpFooter = helpFooter;
    }

    public _CommandLine withOption(_Option option, _Option.Handler handler) {
        options.put(option, handler);
        return this;
    }

    public void parse(String... args) {
        try {
            CommandLineParser parser = new DefaultParser();
            Options cliOptions = buildOptions();

            CommandLine cli = parser.parse(cliOptions, args);
            handleParsedOptions(cli, cliOptions, args);
        } catch (ParseException e) {
            System.err.println("ParseException::" + e.getMessage());
            System.exit(2);
        }
    }

    private Options buildOptions() {
        Options cliOptions = new Options();
        for (Map.Entry<_Option, _Option.Handler> entry : options.entrySet()) {
            _Option option = entry.getKey();
            Option opt = buildOption(option);
            cliOptions.addOption(opt);
            optionsMap.put(option, opt);
        }
        cliOptions.addOption(buildVersionOption());
        cliOptions.addOption(buildHelpOption());
        return cliOptions;
    }

    private Option buildOption(_Option option) {
        /*Option.Builder builder = Option.builder(option.optString)
                .desc(option.description)
                .optionalArg(option.optionalArg)
                .hasArgs(option.hasArgs != Option.UNINITIALIZED)
                .argName(option.argName);

        if (option.args > 0) builder.args(option.args);
        if (option.longOption != null) builder.longOpt(option.longOption);
        if (!Character.toString(option.separator).isEmpty()) builder.valueSeparator(option.separator);

        return builder.build();*/

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
        return opt;
    }

    private Option buildVersionOption() {
        return Option.builder()
                .desc("Show version")
                .longOpt("version")
                .option(OPTION_VERSION)
                .build();
    }

    private Option buildHelpOption() {
        return Option.builder()
                .desc("Show this help")
                .longOpt("help")
                .option(OPTION_HELP)
                .build();
    }

    private void handleParsedOptions(CommandLine cli, Options cliOptions, String[] args) {
        if (args.length == 0) {
            showHelp(cliOptions, System.err);
            System.exit(2);
        } else if (cli.hasOption(OPTION_HELP)) {
            showHelp(cliOptions, System.out);
            System.exit(0);
        }

        if (cli.hasOption(OPTION_VERSION)) {
            System.out.println(applicationLauncher + " " + applicationVersion);
            System.exit(0);
        }

        processOptions(cli);
        handleAdditionalArgs(cli.getArgs());
    }

    private void processOptions(CommandLine cli) {
        for (Map.Entry<_Option, Option> entry : optionsMap.entrySet()) {
            _Option option = entry.getKey();
            String longOption = entry.getValue().getLongOpt();

            if (cli.hasOption(longOption)) {
                handleOptionWithArgs(cli, option, longOption);
            } else if (option.required) {
                System.err.println("Missing required option -" + option.optString + " (--" + option.longOption + ")");
                System.exit(1);
            }
        }
    }

    private void handleOptionWithArgs(CommandLine cli, _Option option, String longOption) {
        if (option.args > 0) {
            _Option.Handler handler = options.get(option);
            try {
                String[] optionArgs = cli.getOptionValues(longOption);
                Properties properties = cli.getOptionProperties(longOption);
                handler.onOption(option, optionArgs, properties);
            } catch (NullPointerException npe) {
                String[] oArgs = new String[]{"null"};
                handler.onOption(option, oArgs, null);
            }
        }
    }

    private void handleAdditionalArgs(String[] additionalArgs) {
        if (additionalArgs.length > 0) {
            if (additionalArgsHandler != null) {
                additionalArgsHandler.onOption(null, additionalArgs);
            } else {
                System.err.println("unhandled args " + StringUtils.join(additionalArgs, ", "));
                System.exit(2);
            }
        }
    }

    private void showHelp(Options cliOptions, PrintStream printStream) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(Comparator.comparingInt(this::indexOfOption));

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

    private int indexOfOption(Option o) {
        int pos = 0;
        for (Map.Entry<_Option, Option> entry : optionsMap.entrySet()) {
            if (entry.getValue() == o) break;
            pos++;
        }
        return pos;
    }

    public _CommandLine onAdditionalArgs(_Option.Handler2 handler) {
        this.additionalArgsHandler = handler;
        return this;
    }

    public static class _Option {

        private final String optString;
        private final String description;

        private boolean required = true;
        private String longOption;
        private int args = 0;
        private boolean optionalArg = false;
        private char separator;
        private String argName;
        private int hasArgs = Option.UNINITIALIZED;

        public _Option(String optS, String description) {
            this.optString = optS;
            this.description = description;
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
            this.separator = c;
            return this;
        }

        public _Option argName(String n) {
            this.argName = n;
            return this;
        }

        public _Option hasArgs() {
            this.hasArgs = Option.UNLIMITED_VALUES;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public interface Handler2 {
            void onOption(_Option option, String[] values);
        }

        public interface Handler {
            void onOption(_Option option, String[] values, Properties properties);
        }
    }
}