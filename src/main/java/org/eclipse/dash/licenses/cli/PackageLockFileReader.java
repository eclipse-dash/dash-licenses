package org.eclipse.dash.licenses.cli;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.json.JsonObject;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.util.JsonUtils;

public class PackageLockFileReader implements IDependencyListReader {

	private InputStream input;

	public PackageLockFileReader(InputStream input) {
		this.input = input;
	}

	@Override
	public Collection<IContentId> getContentIds() {
		Set<IContentId> content = new HashSet<>();
		try {
			JsonObject json = JsonUtils.readJson(input);
			dependenciesDo(json.getJsonObject("dependencies"), id -> content.add(id));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return content;
	}

	/**
	 * Walk through the structure to identify the dependencies, and (recursively)
	 * the dependencies of the dependencies.
	 * 
	 * @param json     A "dependency" list from a package-lock.json file
	 * @param consumer A single argument consumer of each listed item.
	 */
	private void dependenciesDo(JsonObject json, Consumer<ContentId> consumer) {
		if (json == null)
			return;
		json.forEach((key, value) -> {
			String[] parts = key.split("/");
			String namespace;
			String name;
			if (parts.length == 2) {
				namespace = parts[0];
				name = parts[1];
			} else {
				namespace = "-";
				name = key;
			}
			String version = value.asJsonObject().getString("version");
			consumer.accept(new ContentId("npm", "npmjs", namespace, name, version));

			dependenciesDo(json.getJsonObject("dependencies"), consumer);
		});
	}

}
