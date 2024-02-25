/*************************************************************************
 * Copyright (c) 2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

public class MavenCoordinates {

	public final String groupId;
	public final String artifactId;
	public final String type;
	public final String classifier;
	public final String version;
	public final String scope;

	public MavenCoordinates(String groupId, String artifactId, String type, String classifier, String version, String scope) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.type = type;
		this.classifier = classifier;
		this.version = version;
		this.scope = scope;
	}
	
	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder
			.append(groupId)
			.append(":").append(artifactId)
			.append(":").append(type);
		if (!classifier.isBlank()) {
			builder.append(":").append(classifier);
		}
		builder.append(":").append(scope);
		
		return builder.toString();
	}
}
