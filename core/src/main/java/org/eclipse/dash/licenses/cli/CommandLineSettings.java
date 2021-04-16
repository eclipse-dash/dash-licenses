/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.dash.licenses.ISettings;

public class CommandLineSettings implements ISettings {
	private static final String HELP_OPTION = "help";
	private static final String CD_URL_OPTION = "cd";
	private static final String EF_URL_OPTION = "ef";
	private static final String TIMEOUT_OPTION = "timeout";
	private static final String APPROVED_LICENSES_URL_OPTION = "lic";
	private static final String BATCH_OPTION = "batch";
	private static final String CONFIDENCE_OPTION = "confidence";
	private static final String SUMMARY_OPTION = "summary";
	private static final String REVIEW_OPTION = "review";
	private static final String TOKEN_OPTION = "token";
	private static final String PROJECT_OPTION = "project";

	private CommandLine commandLine;

	@Override
	public int getBatchSize() {
		if (!commandLine.hasOption(BATCH_OPTION))
			return ISettings.DEFAULT_BATCH;
		try {
			return ((Number) commandLine.getParsedOptionValue(BATCH_OPTION)).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getLicenseCheckUrl() {
		return commandLine.getOptionValue(EF_URL_OPTION, ISettings.DEFAULT_IPZILLA_URL);
	}

	@Override
	public String getClearlyDefinedDefinitionsUrl() {
		return commandLine.getOptionValue(CD_URL_OPTION, ISettings.DEFAULT_CLEARLYDEFINED_URL);
	}

	@Override
	public int getTimeout() {
		// FIXME This gets called multiple times; consider caching.
		if (!commandLine.hasOption(TIMEOUT_OPTION))
			return ISettings.super.getTimeout();

		try {
			return ((Number) commandLine.getParsedOptionValue(TIMEOUT_OPTION)).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getApprovedLicensesUrl() {
		return commandLine.getOptionValue(APPROVED_LICENSES_URL_OPTION, ISettings.DEFAULT_APPROVED_LICENSES_URL);
	}

	@Override
	public int getConfidenceThreshold() {
		if (!commandLine.hasOption(CONFIDENCE_OPTION))
			return ISettings.DEFAULT_THRESHOLD;
		try {
			return ((Number) commandLine.getParsedOptionValue(CONFIDENCE_OPTION)).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getIpLabToken() {
		return commandLine.getOptionValue(TOKEN_OPTION);
	}

	public boolean isValid() {
		if (commandLine == null)
			return false;

		if (isShowHelp())
			return true;

		if (getFileNames().length == 0)
			return false;

		if (isReview()) {
			if (getIpLabToken() == null || getIpLabToken().isEmpty())
				return false;
			if (getProjectId() == null)
				return false;
		}

		// TODO validate URLs etc.
		try {
			if (commandLine.hasOption(TIMEOUT_OPTION))
				if (((Number) commandLine.getParsedOptionValue(TIMEOUT_OPTION)).intValue() < 1)
					return false;
			// TODO Extend to deal with valid ranges
			if (commandLine.hasOption(BATCH_OPTION))
				commandLine.getParsedOptionValue(BATCH_OPTION);
			if (commandLine.hasOption(CONFIDENCE_OPTION))
				commandLine.getParsedOptionValue(CONFIDENCE_OPTION);

			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Answer the file names. Any value entered on the command line that is not
	 * otherwise recognized as a parameter is is considered a file name.
	 *
	 * @return An array of file names.
	 */
	public String[] getFileNames() {
		return commandLine.getArgs();
	}

	public boolean isShowHelp() {
		return commandLine.hasOption(HELP_OPTION);
	}

	public boolean isReview() {
		return commandLine.hasOption(REVIEW_OPTION);
	}

	private CommandLineSettings(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	public static CommandLineSettings getSettings(String[] args) {
		CommandLine commandLine = getCommandLine(args);

		return new CommandLineSettings(commandLine);
	}

	private static CommandLine getCommandLine(final String[] args) {
		final CommandLineParser parser = new DefaultParser();

		try {
			return parser.parse(getOptions(), args);
		} catch (ParseException parseException) {
			return null;
		}
	}

	private static Options getOptions() {
		final Options options = new Options();

		// @formatter:off
		options.addOption(Option.builder(EF_URL_OPTION)
			.longOpt("foundation-api")
			.required(false)
			.hasArg()
			.argName("url")
			.desc("Eclipse Foundation license check API URL.")
			.build());

		options.addOption(Option.builder(CD_URL_OPTION)
			.longOpt("clearly-defined-api")
			.required(false)
			.hasArg()
			.argName("url")
			.desc("Clearly Defined API URL")
			.build());
		
		options.addOption(Option.builder(TIMEOUT_OPTION)
			.required(false)
			.hasArg()
			.argName("seconds")
			.type(Number.class)
			.desc("Timeout for HTTP calls (in seconds)")
			.build());

		options.addOption(Option.builder(APPROVED_LICENSES_URL_OPTION)
			.longOpt("licenses")
			.required(false)
			.hasArg()
			.argName("url")
			.desc("Approved Licenses List URL")
			.build());

		options.addOption(Option.builder(BATCH_OPTION)
			.required(false)
			.hasArg()
			.argName("int")
			.type(Number.class)
			.desc("Batch size (number of entries sent per API call)")
			.build());

		options.addOption(Option.builder(CONFIDENCE_OPTION)
			.required(false)
			.hasArg()
			.argName("int")
			.type(Number.class)
			.desc("Confidence threshold expressed as integer percent (0-100)")
			.build());

		options.addOption(Option.builder(SUMMARY_OPTION)
			.required(false)
			.hasArg()
			.argName("file")
			.type(String.class)
			.desc("Output a summary to a file")
			.build());

		options.addOption(Option.builder(REVIEW_OPTION)
			.required(false)
			.hasArg(false)
			.desc("Must also specify the project and token")
			.build());

		options.addOption(Option.builder(TOKEN_OPTION)
			.required(false)
			.hasArg()
			.argName("token")
			.type(String.class)
			.desc("The GitLab authentication token")
			.build());
		
		options.addOption(Option.builder(PROJECT_OPTION)
			.required(false)
			.hasArg()
			.argName("shortname")
			.type(String.class)
			.desc("Process the request in the context of an Eclipse project (e.g., technology.dash)")
			.build());

		options.addOption(Option.builder(HELP_OPTION)
			.longOpt(HELP_OPTION)
			.required(false)
			.hasArg(false)
			.desc("Display help")
			.build());
		// @formatter:on

		return options;
	}

	public static void printUsage(PrintStream out) {
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = Main.class.getName();

		final PrintWriter writer = new PrintWriter(out);
		formatter.printUsage(writer, 80, syntax, getOptions());
		writer.flush();
	}

	public static void printHelp(PrintStream out) {
		// TODO Fix this ugly mess.
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = String.format("%s [options] <file> ...", Main.class.getName());
		final String usageHeader = "Sort out the licenses and approval of dependencies.";
		final String usageFooter = "\n" + "\n<file> is the path to a file, or \"-\" to indicate stdin. "
				+ "Multiple files may be provided" + "\ne.g.,"
				+ "\nnpm list | grep -Poh \"\\S+@\\d+(?:\\.\\d+){2}\" | sort | uniq | LicenseFinder -";

		formatter.printHelp(syntax, usageHeader, getOptions(), usageFooter);
	}

	@Override
	public String getSummaryFilePath() {
		return commandLine.getOptionValue(SUMMARY_OPTION, null);
	}

	@Override
	public String getProjectId() {
		return commandLine.getOptionValue(PROJECT_OPTION, null);
	}
}
