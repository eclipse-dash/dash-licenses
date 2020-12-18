/*************************************************************************
 * Copyright (c) 2020, Red Hat Inc.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;

/**
 * Abstract mojo containing artifact filtering capability.
 */
public abstract class AbstractArtifactFilteringMojo extends AbstractMojo {

	/**
	 * Scope of dependencies to include, as determined by Maven. If specified, it
	 * must be one of the following values:
	 * <ul>
	 * <li><code>runtime</code> -- gives runtime and compile dependencies only.</li>
	 * <li><code>compile</code> -- (default) gives compile, provided, and system
	 * dependencies only.</li>
	 * <li><code>test</code> -- gives all dependencies.</li>
	 * <li><code>provided</code> -- gives provided dependencies only.</li>
	 * <li><code>system</code> -- gives system dependencies only.</li>
	 * </ul>
	 * If no scope is specified, then the default value of <code>compile</code> is
	 * used, including compile, provided, and system dependencies only.
	 */
	@Parameter(defaultValue = "compile")
	protected String includeScope;

	/**
	 * Scope to exclude. An empty string indicates no scopes (default).
	 */
	@Parameter(defaultValue = "")
	protected String excludeScope;

	/**
	 * Comma separated list of types to include. Empty string indicates include
	 * everything (default).
	 */
	@Parameter(defaultValue = "")
	protected String includeTypes;

	/**
	 * Comma separated list of types to exclude. Empty string indicates don't
	 * exclude anything (default).
	 */
	@Parameter(defaultValue = "")
	protected String excludeTypes;

	/**
	 * Comma separated list of classifiers to include. Empty string indicates
	 * include everything (default).
	 */
	@Parameter(defaultValue = "")
	protected String includeClassifiers;

	/**
	 * Comma separated list of classifiers to exclude. Empty string indicates don't
	 * exclude anything (default).
	 */
	@Parameter(defaultValue = "")
	protected String excludeClassifiers;

	/**
	 * Comma separated list of group IDs to include. Empty string indicates include
	 * everything (default).
	 */
	@Parameter(defaultValue = "")
	protected String includeGroupIds;

	/**
	 * Comma separated list of group IDs to exclude. Empty string indicates don't
	 * exclude anything (default).
	 */
	@Parameter(defaultValue = "")
	protected String excludeGroupIds;

	/**
	 * Comma separated list of artifact IDs to include. Empty string indicates
	 * include everything (default).
	 */
	@Parameter(defaultValue = "")
	protected String includeArtifactIds;

	/**
	 * Comma separated list of artifact IDs to exclude. Empty string indicates don't
	 * exclude anything (default).
	 */
	@Parameter(defaultValue = "")
	protected String excludeArtifactIds;

	/**
	 * Filter a set of artifacts based on the user-supplied filter parameters.
	 * 
	 * @param artifacts a set of artifacts to filter
	 * @return a new set containing only the artifacts that match the filter
	 * @throws MojoExecutionException if there was a problem filtering the artifacts
	 */
	protected Set<Artifact> filterArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
		FilterArtifacts filter = new FilterArtifacts();
		filter.addFilter(new ScopeFilter(sanitise(includeScope), sanitise(excludeScope)));
		filter.addFilter(new TypeFilter(sanitise(includeTypes), sanitise(excludeTypes)));
		filter.addFilter(new ClassifierFilter(sanitise(includeClassifiers), sanitise(excludeClassifiers)));
		filter.addFilter(new GroupIdFilter(sanitise(includeGroupIds), sanitise(excludeGroupIds)));
		filter.addFilter(new ArtifactIdFilter(sanitise(includeArtifactIds), sanitise(excludeArtifactIds)));
		try {
			return filter.filter(artifacts);
		} catch (ArtifactFilterException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * Utility to strip extraneous spaces from user-supplied filter parameters.
	 */
	private static String sanitise(String value) {
		String sanitised = "";
		if (value != null && !value.isEmpty()) {
			sanitised = value.trim().replaceAll("[\\s]*,[\\s]*", ",");
		}
		return sanitised;
	}
}
