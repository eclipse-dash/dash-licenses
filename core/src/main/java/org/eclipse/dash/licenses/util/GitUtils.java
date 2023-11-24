/*************************************************************************
 * Copyright (c) 2023 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

import java.io.IOException;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.URIish;

public class GitUtils {
	
	/**
	 * Answers the browsable (https) URL for our best guess at the URL of the remote
	 * Eclipse Project Git repository associated with the Git repository in the
	 * working directory.
	 * 
	 * @return a browsable (<code>https</code>) URL or <code>null</code>.
	 */
	public static String getEclipseRemote() {
		return findEclipseRemotes()
				.map(each -> String.format("https://%s/%s", each.getHost(), clean(each.getPath())))
				.findFirst().orElse(null);
	}

	static Stream<URIish> findEclipseRemotes() {
		try {
			var repository = new RepositoryBuilder().findGitDir().build();
			if (repository == null) return null;
	
			var git = Git.wrap(repository);
			return git.remoteList().call().stream()
					.flatMap(each -> each.getURIs().stream())
					.filter(each -> each.isRemote())
					.filter(each -> isEclipseRemote(each));
		} catch (IOException | GitAPIException e) {
			// FIXME Log this (or maybe just don't...)
		}
		return Stream.empty();
	}
	
	static boolean isEclipseRemote(URIish uri) {
		if ("git.eclipse.org".equals(uri.getHost())) return true;
		if ("gitlab.eclipse.org".equals(uri.getHost()) && isEclipseProjectGitLabGroup(uri)) return true;
		if ("github.com".equals(uri.getHost()) && isEclipseProjectGitHubOrg(uri)) return true;
		
		return false;
	}

	static boolean isEclipseProjectGitLabGroup(URIish uri) {
		var path = clean(uri.getPath());
		if (path.startsWith("eclipse/")) return true;
		if (path.startsWith("eclipsefdn/")) return true;
		
		return false;
	}

	static boolean isEclipseProjectGitHubOrg(URIish uri) {
		var path = clean(uri.getPath());
		if (path.startsWith("eclipse/")) return true;
		if (path.startsWith("eclipse-")) return true;
		
		// TODO We need to be more dynamic
		if (path.startsWith("jetty/")) return true;
		if (path.startsWith("deeplearning4j/")) return true;
		
		return false;
	}
	
	/**
	 * The path sometimes has a leading slash and sometimes does not. I believe that
	 * the difference is the result of the schema (there's no slash when the
	 * <code>git</code> schema is used, but there is a slash when it's
	 * <code>https</code>. Just strip a leading slash if one exists.
	 */
	static private String clean(String path) {
		if (path.startsWith("/")) return path.substring(1);
		return path;
	}
}
