package org.eclipse.dash.bom;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Settings {
	private static final String CD_URL_DEFAULT = "https://api.clearlydefined.io/definitions";
	private static final String EF_URL_DEFAULT = "https://www.eclipse.org/projects/services/license_check.php";
	private static final String CD_URL_OPTION = "cd";
	private static final String EF_URL_OPTION = "ef";
	private static final String BATCH_OPTION = "batch";
	private CommandLine commandLine;

	public int getBatchSize() {
		if (!commandLine.hasOption(BATCH_OPTION)) return 1000;
		try {
			return (int) commandLine.getParsedOptionValue(BATCH_OPTION);
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	public String getLicenseCheckUrl() {
		//String url = "http://localhost/projects/services/license_check.php?XDEBUG_SESSION_START=ECLIPSE_DBGP&KEY=15743965682411";
        return commandLine.getOptionValue(EF_URL_OPTION, EF_URL_DEFAULT);
	}

	String getClearlyDefinedDefinitionsUrl() {
		return commandLine.getOptionValue(CD_URL_OPTION, CD_URL_DEFAULT);
	}
	
	private Settings(CommandLine commandLine) {
		this.commandLine = commandLine;
	}
	
	public static Settings getSettings(String[] args) {
		CommandLine commandLine = getCommandLine(args);
		if (commandLine == null) {
			printUsage(System.out);
			return null;
		}
		
		if (commandLine.hasOption("help")) {
			printHelp(System.out);
			return null;
		}
		
		return new Settings(commandLine);
	}
	
	private static CommandLine getCommandLine(final String[] args)	{
		final CommandLineParser parser = new DefaultParser();

		try {
			return parser.parse(getOptions(), args);
		} catch (ParseException parseException) {
			return null;
		}
	}
	
	private static Options getOptions()	{
		final Options options = new Options();

		options.addOption(Option.builder(EF_URL_OPTION)
			.longOpt("foundation-api")
			.required(false)
			.hasArg()
			.desc("Eclipse Foundation license check API URL.")
			.build());

		options.addOption(Option.builder(CD_URL_OPTION)
			.longOpt("clearly-defined-api")
			.required(false)
			.hasArg()
			.desc("Clearly Defined API URL")
			.build());
		
		options.addOption(Option.builder(BATCH_OPTION)
			.required(false)
			.hasArg()
			.type(Integer.class)
			.desc("Batch size (number of entries sent per API call)")
			.build());

		options.addOption(Option.builder("help")
			.longOpt("help")
			.required(false)
			.hasArg(false)
			.desc("Display help")
			.build());
		
		return options;
	}

	public static void printUsage(PrintStream out) {
	   final HelpFormatter formatter = new HelpFormatter();
	   final String syntax = "LicenseFinder";

	   final PrintWriter writer  = new PrintWriter(out);
	   formatter.printUsage(writer, 80, syntax, getOptions());
	   writer.flush();
	}

	public static void printHelp(PrintStream out) {
		// TODO Fix this ugly mess.
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = "LicenseFinder [options] <file> ...";
		final String usageHeader = "Sort out the licenses and approval of dependencies.";
		final String usageFooter = "\n" 
				+ "\n<file> is the path to a file, or \"-\" to indicate stdin. "
				+ "Multiple files may be provided" + "\ne.g.,"
				+ "\nnpm list | grep -Poh \"\\S+@\\d+(?:\\.\\d+){2}\" | sort | uniq | LicenseFinder -";

		formatter.printHelp(syntax, usageHeader, getOptions(), usageFooter);
	}
}
