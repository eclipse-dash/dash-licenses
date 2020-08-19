/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseChecker;

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

	public static void main(String[] args) {
		CommandLineSettings settings = CommandLineSettings.getSettings(args);
		if (!settings.isValid()) {
			CommandLineSettings.printUsage(System.out);
			System.exit(0);
		}

		if (settings.isShowHelp()) {
			CommandLineSettings.printHelp(System.out);
			System.exit(0);
		}

		// TODO Set up collectors based on command line parameters
		IResultsCollector primaryCollector = new NeedsReviewCollector(System.out);
		List<IResultsCollector> collectors = new ArrayList<>();
		collectors.add(primaryCollector);
		try {
			collectors.add(new CSVCollector(new FileOutputStream(new File("DEPENDENCIES"))));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Arrays.stream(settings.getFileNames()).forEach(name -> {
			IDependencyListReader reader = null;
			try {
				reader = getReader(name);
			} catch (FileNotFoundException e) {
				System.out.println(String.format("The file \"%s\" does not exist.", name));
				CommandLineSettings.printUsage(System.out);
				System.exit(0);
			}
			if (reader != null) {
				Collection<IContentId> dependencies = reader.getContentIds();

				LicenseChecker checker = new LicenseChecker(settings);
				checker.getLicenseData(dependencies, data -> {
					collectors.forEach(collector -> collector.accept(data));
				});
			}
		});

		collectors.forEach(IResultsCollector::close);

		System.exit(primaryCollector.getStatus());
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
				return new FlatFileReader(new FileReader(input));
			} else {
				throw new FileNotFoundException(name);
			}
		}
	}
}
