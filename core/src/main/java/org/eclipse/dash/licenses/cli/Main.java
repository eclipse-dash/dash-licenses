/*************************************************************************
 * Copyright (c) 2019,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.context.LicenseToolModule;
import org.eclipse.dash.licenses.review.CreateReviewRequestCollector;
import org.eclipse.dash.licenses.review.GitLabSupport;
import org.eclipse.dash.licenses.validation.EclipseProjectIdValidator;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class provides a CLI entrypoint to determine licenses for content. The
 * tool can be invoked in a few different ways, e.g.
 *
 * <pre>
 * mvn dependency:list -DskipTests -Dmaven.javadoc.skip=true |
 * grep -Poh "\S+:(system|provided|compile) |
 * sort | uniq | java -jar licenses.jar -batch 100 -
 * </pre>
 *
 * or
 *
 * <pre>
 * java -jar licenses.jar -batch 100 package-lock.json
 * </pre>
 *
 * @param args
 */
public class Main {
	/**
	 * Exit code that indicates there was an internal error, orthogonal to
	 * license check results, that prevented `dash-licenses` from successfully
	 * running or completing its work. Depending on the exact problem, a 
	 * re-try might or might no work.
	 */ 
	final static Integer INTERNAL_ERROR = 127;

	public static void main(String[] args) {
		CommandLineSettings settings = CommandLineSettings.getSettings(args);
		if (!settings.isValid()) {
			CommandLineSettings.printUsage(System.out);
			System.exit(INTERNAL_ERROR);
		}

		if (settings.isShowHelp()) {
			CommandLineSettings.printHelp(System.out);
			System.exit(0);
		}

		Injector injector = Guice.createInjector(new LicenseToolModule(settings));

		if (settings.getProjectId() != null) {
			var validator = injector.getInstance(EclipseProjectIdValidator.class);
			if (!validator.validate(settings.getProjectId(), message -> System.out.println(message))) {
				System.exit(INTERNAL_ERROR);
			}
		}

		LicenseChecker checker = injector.getInstance(LicenseChecker.class);

		List<IResultsCollector> collectors = new ArrayList<>();

		// TODO Set up collectors based on command line parameters
		IResultsCollector primaryCollector = new NeedsReviewCollector();

		collectors.add(primaryCollector);

		String summaryPath = settings.getSummaryFilePath();
		if (summaryPath != null) {
			try {
				collectors.add(new CSVCollector(getWriter(summaryPath)));
			} catch (FileNotFoundException e1) {
				System.out.println("Can't write to " + summaryPath);
				System.exit(INTERNAL_ERROR);
			}
		}

		if (settings.isReview()) {
			collectors
					.add(new CreateReviewRequestCollector(injector.getInstance(GitLabSupport.class), (id, url) -> {}));
		}

		Arrays.stream(settings.getFileNames()).forEach(name -> {
			IDependencyListReader reader = null;
			try {
				reader = getReader(name);
			} catch (FileNotFoundException e) {
				System.out.println(String.format("The file \"%s\" does not exist.", name));
				CommandLineSettings.printUsage(System.out);
				System.exit(INTERNAL_ERROR);
			}
			if (reader != null) {
				var filter = new ExcludedSourcesFilter(settings.getExcludedSources());

				Collection<IContentId> dependencies = reader
						.getContentIds()
						.stream()
						.filter(each -> filter.keep(each))
						.collect(Collectors.toList());

				try {
					checker.getLicenseData(dependencies).forEach((id, licenseData) -> {
						collectors.forEach(collector -> collector.accept(licenseData));
					});
				} catch (RuntimeException e) {
					System.exit(INTERNAL_ERROR);
				}
			}
		});

		collectors.forEach(IResultsCollector::close);
		final int rawStatus = primaryCollector.getStatus();
		System.exit(rawStatus >= INTERNAL_ERROR ? INTERNAL_ERROR - 1 : rawStatus );
	}

	private static OutputStream getWriter(String path) throws FileNotFoundException {
		if ("-".equals(path))
			return System.out;
		return new FileOutputStream(new File(path));
	}

	@SuppressWarnings("resource")
	private static IDependencyListReader getReader(String name) throws FileNotFoundException {
		if ("-".equals(name)) {
			return new FlatFileReader(new InputStreamReader(System.in));
		} else {
			File input = new File(name);
			if (input.exists()) {
				if ("package-lock.json".equals(input.getName())) {
					return new PackageLockFileReader(new FileInputStream(input));
				}
				if ("yarn.lock".equals(input.getName())) {
					return new YarnLockFileReader(new FileReader(input));
				}
				return new FlatFileReader(new FileReader(input));
			} else {
				throw new FileNotFoundException(name);
			}
		}
	}
}
