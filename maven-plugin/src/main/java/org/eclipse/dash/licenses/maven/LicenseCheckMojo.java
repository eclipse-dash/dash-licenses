/*************************************************************************
 * Copyright (c) 2020,2023 Red Hat Inc. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.cli.CSVCollector;
import org.eclipse.dash.licenses.cli.IResultsCollector;
import org.eclipse.dash.licenses.cli.NeedsReviewCollector;
import org.eclipse.dash.licenses.context.LicenseToolModule;
import org.eclipse.dash.licenses.projects.ProjectService;
import org.eclipse.dash.licenses.review.CreateReviewRequestCollector;
import org.eclipse.dash.licenses.review.GitLabSupport;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Maven goal for running the Dash License Check tool.
 */
@Mojo(name = "license-check", requiresProject = true, aggregator = true, requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class LicenseCheckMojo extends AbstractArtifactFilteringMojo {

	// @FIXME Refactor
	// @see MavenIdParser::P2_GROUPID_PREFIX
	private static final String P2_GROUPID_PREFIX = "p2.";
	/**
	 * Optionally process the request within the context of an Eclipse Foundation
	 * project. (E.g., technology.dash)
	 */
	@Parameter(property = "dash.projectId")
	private String projectId;
	

	/**
	 * Optionally specify the Eclipse Project repository that is the source of the request
	 */
	@Parameter(property = "dash.repo")
	private String repo;

	/**
	 * Output a summary to the given file. If not specified, then a dependencies
	 * summary will be generated at the default location within
	 * <code>${project.build.directory}</code>
	 */
	@Parameter(property = "dash.summary", defaultValue = "${project.build.directory}/dash/summary")
	private File summary;

	/**
	 * Output a summary of created reviews to the given file. If not specified, then
	 * a review-summary will be generated at the default location within
	 * <code>${project.build.directory}</code>
	 */
	@Parameter(property = "dash.review.summary", defaultValue = "${project.build.directory}/dash/review-summary")
	private File reviewSummary;

	/**
	 * Batch size to use (number of entries sent per API call.)
	 */
	@Parameter(property = "dash.batch", defaultValue = "" + ISettings.DEFAULT_BATCH)
	private int batch;

	/**
	 * URL for the Eclipse Foundations's license check API.
	 */
	@Parameter(property = "dash.foundationApi", defaultValue = ISettings.DEFAULT_IPZILLA_URL)
	private String foundationApi;

	/**
	 * URL for Clearly Defined's license definitions API.
	 */
	@Parameter(property = "dash.clearlyDefinedApi", defaultValue = ISettings.DEFAULT_CLEARLYDEFINED_URL)
	private String clearlyDefinedApi;

	/**
	 * URL that returns the list of approved licenses. This URL should return a JSON
	 * document containing a map of SPDX license identifiers and their descriptions,
	 * for example:
	 *
	 * <pre>
	 * {
	 *   "approved": {
	 *     "Apache-2.0": "Apache Software License 2.0",
	 *     "CPL-1.0": "Common Public License Version 1.0"
	 *   }
	 * }
	 * </pre>
	 */
	@Parameter(property = "dash.licenses", defaultValue = ISettings.DEFAULT_APPROVED_LICENSES_URL)
	private String licenses;

	/**
	 * Confidence threshold expressed as integer percentage. (0-100)
	 */
	@Parameter(property = "dash.confidence", defaultValue = "" + ISettings.DEFAULT_THRESHOLD)
	private int confidence;

	@Parameter(property = "dash.iplab.token")
	private String iplabToken;

	/**
	 * Skip execution of the Dash License Check mojo.
	 */
	@Parameter(property = "dash.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Make the build fail when any dependency is identified as requiring review by
	 * Eclipse Foundation.
	 */
	@Parameter(property = "dash.fail", defaultValue = "false")
	private boolean failWhenReviewNeeded;

	/**
	 * Optional <tt>&lt;proxy&gt;</tt> ID configuration.
	 */
	@Parameter(property = "dash.proxy")
	private String proxy;

	/**
	 * The Maven session.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession mavenSession;

	/**
	 * The Maven reactor.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	private List<MavenProject> reactorProjects;

	/**
	 * Maven Security Dispatcher
	 */
	@Component
	private SecDispatcher securityDispatcher;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// We are aggregating the deps for all projects in the reactor, so we only need
		// to execute once. This check ensures we run only during the build of the
		// top-level reactor project and avoids duplicate invocations
		if (!mavenSession.getCurrentProject().equals(mavenSession.getTopLevelProject())) {
			return;
		}

		if (skip) {
			getLog().info("Skipping dependency license check");
			return;
		}

		// Validate the user-given dash license tool settings
		ISettings settings;
		try {
			settings = new MavenSettings(batch, foundationApi, clearlyDefinedApi, licenses, confidence, projectId, iplabToken, repo);
		} catch (IllegalArgumentException e) {
			throw new MojoExecutionException("Invalid setting: " + e.getMessage());
		}

		// Get filtered list of project dependencies for all modules in the reactor
		Set<Artifact> filteredArtifacts = new HashSet<>();
		for (MavenProject project : reactorProjects) {
			filteredArtifacts.addAll(filterArtifacts(project.getArtifacts()));
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug("Filtered dependency artifact list:");
			filteredArtifacts.stream().sorted().map(a -> "  " + a).forEach(getLog()::debug);
		}

		// Adapt dependency artifacts to dash content IDs
		List<IContentId> deps = new ArrayList<>();
		filteredArtifacts.stream().sorted().forEach(a -> {
			// FIXME Refactor. This is duplicated from MavenIdParser
			String type = a.getGroupId().startsWith(P2_GROUPID_PREFIX) ? "p2" : "maven";
			// TODO deps are not necessarily from orbit or maven central
			String source = a.getGroupId().startsWith(P2_GROUPID_PREFIX) ? "orbit" : "mavencentral";
			// TODO could get duplicates here if two artifact coords differ only by
			// classifier
			deps.add(ContentId.getContentId(type, source, a.getGroupId(), a.getArtifactId(), a.getVersion()));
		});

		List<IResultsCollector> collectors = new ArrayList<>();

		// This collector generates feedback for the user that the command line tool
		// would always print to stdout, so we collect the output in memory for printing
		// to the maven log later
		ByteArrayOutputStream primaryOut = new ByteArrayOutputStream();
		NeedsReviewCollector needsReviewCollector = new NeedsReviewCollector();
		collectors.add(needsReviewCollector);

		Injector injector = Guice.createInjector(new LicenseToolModule(settings, createProxySettings()));
		
		if (settings.getProjectId() != null) {
			var validator = injector.getInstance(ProjectService.class);
			if (!validator.validate(settings.getProjectId(), message -> getLog().error(message))) {
				throw new MojoExecutionException("Invalid project id.");
			}
		}
		
		LicenseChecker checker = injector.getInstance(LicenseChecker.class);

		summary.getParentFile().mkdirs();
		reviewSummary.getParentFile().mkdirs();

		try (
				OutputStream summaryOut = new FileOutputStream(summary);
				PrintWriter reviewSummaryOut = new PrintWriter(new FileWriter(reviewSummary))) {

			collectors.add(new CSVCollector(summaryOut));

			if (iplabToken != null && projectId != null) {
				collectors.add(new CreateReviewRequestCollector(injector.getInstance(GitLabSupport.class),
						(id, url) -> reviewSummaryOut.println("[" + id + "](" + url + ")")));
			} else if (iplabToken != null) {
				getLog().info(
						"Provide both an authentication token and a project id to automatically create review tickets.");
			}

			for (LicenseData licenseData : checker.getLicenseData(deps).values()) {
				collectors.forEach(c -> c.accept(licenseData));
			}
			collectors.forEach(IResultsCollector::close);

		} catch (IOException e) {
			throw new MojoExecutionException("Can't write dependency summary file", e);
		}

		// Pass the output from the collectors to the maven log
		primaryOut.toString(StandardCharsets.UTF_8).lines().forEach(getLog()::info);

		getLog().info("Summary file was written to: " + summary);

		if (failWhenReviewNeeded && needsReviewCollector.getStatus() > 0) {
			getLog().error("Dependency license check failed. Some dependencies need to be vetted.");
			throw new MojoFailureException("Some dependencies must be vetted.");
		}
	}

	protected IProxySettings createProxySettings() {
		Proxy proxyServer = mavenSession.getSettings().getActiveProxy();
		if (proxy != null) {
			proxyServer = mavenSession.getSettings().getProxies().stream().filter(p -> proxy.equals(p.getId()))
					.findFirst().orElse(null);
			if (proxyServer == null) {
				getLog().warn(MessageFormat.format("No such proxy server is activated in settings.xml: {0}", proxy));
				return null;
			}
		}

		if (proxyServer == null) {
			// No proxy configuration
			return null;
		}

		return new MavenProxySettings(proxyServer.getProtocol(), proxyServer.getHost(), proxyServer.getPort(), proxyServer.getUsername(),
				proxyServer.getPassword(), securityDispatcher, getLog());
	}

}
