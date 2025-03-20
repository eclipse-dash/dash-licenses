/*************************************************************************
 * Copyright (c) 2019 The Eclipse Foundation and others.
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
	private static final String TIMEOUT_OPTION = "timeout";
	private static final String BATCH_OPTION = "batch";
	private static final String CONFIDENCE_OPTION = "confidence";
	private static final String SUMMARY_OPTION = "summary";
	private static final String REVIEW_OPTION = "review";
	private static final String EXCLUDE_SOURCES_OPTION = "excludeSources";
	private static final String TOKEN_OPTION = "token";
	private static final String PROJECT_OPTION = "project";
	
	private static final String REPO_OPTION = "repo";

	private CommandLine commandLine;

	@Override
	public int getBatchSize() {
		try {
			return commandLine.getParsedOptionValue(BATCH_OPTION, () -> ISettings.super.getBatchSize()).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getTimeout() {
		try {
			return commandLine.getParsedOptionValue(TIMEOUT_OPTION, () -> ISettings.super.getTimeout()).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getConfidenceThreshold() {
		try {
			return commandLine.getParsedOptionValue(CONFIDENCE_OPTION, () -> ISettings.super.getConfidenceThreshold()).intValue();
		} catch (ParseException e) {
			// TODO Deal with this
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getIpLabToken() {
		return commandLine.getOptionValue(TOKEN_OPTION, () -> ISettings.super.getIpLabToken());
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

		options.addOption(Option.builder(TIMEOUT_OPTION)
			.required(false)
			.hasArg()
			.argName("seconds")
			.type(Integer.class)
			.desc("Timeout for HTTP calls (in seconds)")
			.build());

		options.addOption(Option.builder(BATCH_OPTION)
			.required(false)
			.hasArg()
			.argName("int")
			.type(Integer.class)
			.desc("Batch size (number of entries sent per API call)")
			.build());

		options.addOption(Option.builder(CONFIDENCE_OPTION)
			.required(false)
			.hasArg()
			.argName("int")
			.type(Integer.class)
			.desc("The minimum licence score to approve components based on licence data received from ClearlyDefined, "
					+ "expressed as integer percent (0-100). Use this option carefully.")
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

		options.addOption(Option.builder(EXCLUDE_SOURCES_OPTION)
			.required(false)
			.hasArg(true)
			.argName("sources")
			.desc("Exclude values from specific sources")
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

		options.addOption(Option.builder(REPO_OPTION)
			.required(false)
			.hasArg()
			.argName("url")
			.type(String.class)
			.desc("The Eclipse Project repository that is the source of the request")
			.build());
		
		options.addOption(Option.builder(HELP_OPTION)
			.longOpt(HELP_OPTION)
			.required(false)
			.hasArg(false)
			.desc("Display help")
			.build());

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
		final String usageFooter = "\n<file> is the path to a file, or \"-\" to indicate stdin. "
				+ "\nFor more help and examples, see https://github.com/eclipse-dash/dash-licenses";

		formatter.printHelp(syntax, usageHeader, getOptions(), usageFooter);
	}

	@Override
	public String getSummaryFilePath() {
		return commandLine.getOptionValue(SUMMARY_OPTION, () -> ISettings.super.getSummaryFilePath());
	}

	@Override
	public String getProjectId() {
		return commandLine.getOptionValue(PROJECT_OPTION, () -> ISettings.super.getProjectId());
	}
	
	@Override
	public String getRepository() {
		return commandLine.getOptionValue(REPO_OPTION, () -> ISettings.super.getRepository());
	}

	public String getExcludedSources() {
		return commandLine.getOptionValue(EXCLUDE_SOURCES_OPTION, "");
	}
}
