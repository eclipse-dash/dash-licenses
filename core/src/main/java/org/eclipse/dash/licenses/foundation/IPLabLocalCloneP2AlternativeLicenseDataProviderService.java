/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * Instances of the IPLabCloneService create a local shallow clone of the IPLab repository
 * and use that local clone to make various queries. 
 */
public class IPLabLocalCloneP2AlternativeLicenseDataProviderService implements ILicenseDataProvider {
	final Pattern mavenPattern = Pattern.compile("^maven\\.(?<group>[\\w.\\-_]*)\\.artifact\\.(?<artifact>[\\w.\\-_]*)$");
	
	@Inject
	IPLabLocalCloneCurationDataService iplab;
	
	final Logger logger = LoggerFactory.getLogger(IPLabLocalCloneP2AlternativeLicenseDataProviderService.class);
	
	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.isEmpty())
			return;

		logger.info("Querying curationed data for license data for {} items.", ids.size());

		for(IContentId id : ids) {
			getMavenAlternative(id).ifPresent(gav -> {
				logger.debug("Mapping {} to {}.", id, gav);
				findContentData(gav, consumer);
			});
		}
	}
	
	private Optional<IContentId> getMavenAlternative(IContentId id) {
		if ("p2".equals(id.getType())) {
			var matcher = mavenPattern.matcher(id.getName());
			if (matcher.matches()) {
				return Optional.of(ContentId.getContentId("maven","mavencentral",matcher.group("group"), matcher.group("artifact"), id.getVersion()));
			} 
			
			try {
				return iplab.getFileContents(Path.of("projects", "p2.bundleIdMap"))
					.filter(each -> !each.isBlank())
					.map(each -> new BundleIdMap(each))
					.filter(each -> each.isValid())
					.filter(each -> each.getBundleId().equals(id.getName()))
					.map(each -> ContentId.getContentId("maven","mavencentral",each.getGroupId(), each.getArtifactId(), id.getVersion()))
					.findFirst();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return Optional.empty();
	}
	
	class BundleIdMap {

		private String[] parts;

		public BundleIdMap(String line) {
			var parts = line.split(",");
			if (parts.length == 3) {
				this.parts = parts;
			}
		}
		
		public boolean isValid() {
			return parts != null;
		}
		
		public String getBundleId() {
			return parts[0];
		}
		
		public String getGroupId() {
			return parts[1];
		}
		
		public String getArtifactId() {
			return parts[2];
		}
	}

	private void findContentData(IContentId id, Consumer<IContentData> consumer) {
		iplab.findContentData(id).findFirst().ifPresent(consumer);
	}
	
	@Override
	public int getWeight() {
		return 10;
	}
}

