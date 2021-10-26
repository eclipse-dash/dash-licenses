/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

public class ContentId implements IContentId {

	private static final ClearlyDefinedIdParser IdParser = new ClearlyDefinedIdParser();

	private String type;
	private String source;
	private String namespace;
	private String name;
	private String version;

	private ContentId(String type, String source, String namespace, String name, String version) {
		this.type = type;
		this.source = source;
		this.namespace = namespace;
		this.name = name;
		this.version = version;
	}

	public static IContentId getContentId(String type, String source, String namespace, String name, String version) {
		return new ContentId(type, source, namespace, name, version);
	}

	public static IContentId getContentId(String string) {
		return IdParser.parseId(string);
	}

	@Override
	public String toString() {
		return type + "/" + source + "/" + namespace + "/" + name + "/" + version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentId) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
