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
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.eclipse.dash.licenses.util.ProximityComparator;
import org.eclipse.dash.licenses.util.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gitlab4j.api.GitLabApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * Instances of the IPLabCloneService create a local shallow clone of the IPLab
 * repository and use that local clone to make various queries of the data
 * contained within. This implementation favours simplicity over efficiency,
 * choosing to keep the data exclusively in the file system where it may be
 * repeatedly traversed.
 */
public class IPLabLocalCloneCurationDataService {
	@Inject
	ISettings settings;
	@Inject
	GitLabApi gitlabApi;
	
	final Logger logger = LoggerFactory.getLogger(IPLabLocalCloneCurationDataService.class);
	private Path clonePath;
	
	@Inject
	public void init() {
		try {
			var repoUrl = getRepositoryUrl();
			var localPath = Files.createTempDirectory("iplab-");
			
			logger.debug("Cloning {} to {}.", repoUrl, localPath);
			Git.cloneRepository()
					.setURI(repoUrl)
					.setDirectory(localPath.toFile())
					.setDepth(1)
					.call();
			this.clonePath = localPath;
			addShutdownHook();
		} catch (IOException | GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	private String getRepositoryUrl() {
		return settings.getIpLabHostUrl() + "/" + settings.getIpLabRepositoryPath() + ".git";
	}
	
	public boolean update(boolean rebuild) {
		try (var repository = new FileRepositoryBuilder().setGitDir(clonePath.toFile()).build()) {
			try (Git git = new Git(repository)) {
				var pullResult = git.pull().call();
				if (!pullResult.isSuccessful() && rebuild) {
					init();
				}
				return pullResult.isSuccessful();
			} 
		} catch (IOException | GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	private void addShutdownHook() {
		if (clonePath == null) return;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try (var walk = Files.walk(clonePath)) {
					walk.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							logger.debug("Error encountered while cleaning up.", e);
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	public Stream<IContentData> findContentData(IContentId id) {
		var exact = findCurationExactMatch(id);
		if (exact != null) return Stream.of(exact);
		
		var data = new HashSet<IContentData>();
		findCompatibleCurations(id, data);
		findHistoricalCurations(id, data);
		
		return data.stream().sorted(new ServiceVersionComparator(id));
	}
	
	/**
	 * Compare the service version of two content ids. This comparator assumes that
	 * the values that it is provided with are both valid semantic versions, and
	 * that the major and minor versions are equal.
	 */
	class ServiceVersionComparator implements Comparator<IContentData> {
		ProximityComparator comparator;
		
		public ServiceVersionComparator(IContentId base) {
			this.comparator = new ProximityComparator(new Version(base.getVersion()).service());
		}
		
		@Override
		public int compare(IContentData o1, IContentData o2) {
			if (o1.getId().getVersion().equals(o2.getId().getVersion())) return 0;
			
			return comparator.compare(new Version(o1.getId().getVersion()).service(), new Version(o2.getId().getVersion()).service());
		}
	}
	
	public Stream<String> getFileContents(Path path) throws IOException {
		var file = clonePath.resolve(path);
		return Files.lines(file);
	}

	public IContentData findCurationExactMatch(IContentId id) {
		var path = "curations/" + id + "/info.json";
		var file = clonePath.resolve(path);
		return readCurationData(id, file);
	}

	private IContentData readCurationData(IContentId id, Path path) {
		logger.debug("Querying IPLab for {}.", id);
		if (Files.exists(path)) {
			try {
				var json = Files.readString(path);
				// TODO There is a chance that the JSON may be misformed. Deal with that.
				var info = JsonUtils.readJson(new StringReader(json));
				IPLabContentData contentData = new IPLabContentData(id, info);
				// TODO Deal with workswith and exceptions
				if (contentData.getStatus() == Status.Approved) {
					logger.debug("Found curation file at {}.", path);
					return contentData;
				}
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		} else {
			logger.debug("No match found at {}.", path);
		}
		return null;
	}
	
	private void findCompatibleCurations(IContentId id, Collection<IContentData> matches) {
		var path = "curations/" + id.getType() + "/" + id.getSource() + "/" + id.getNamespace() + "/" + id.getName();
		logger.debug("Querying IPLab curation data for close match for {}.", id);
		var component = clonePath.resolve(path);
		if (Files.exists(component)) {
			var version = new Version(id.getVersion());
			try (var versions = Files.list(component)) {
				versions
					.filter(file -> Files.isDirectory(file))
					.map(each -> new Version(each.getFileName().toString()))
					.filter(each -> each.isSemantic())
					.filter(each -> each.major() == version.major() && each.minor() == version.minor())
					.map(each -> component.resolve(each.toString() + "/info.json"))
					.map(file -> readCurationData(id, file))
					.filter(Objects::nonNull)
					.forEach(match -> {
						logger.debug("Found close curation match {}.", match.getId());
						matches.add(match);
					});
			} catch (IOException e) {
				logger.debug("Not found {}.", id);
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Find matching records in the historical information. Historical information
	 * is stored in the repository in the <code>mapping</code> directory in
	 * arbitrarily large CSV files. This includes, for example, data extracted from
	 * the old IPZilla instance. The implementation is pretty simple: iterate over
	 * the files line-by-line and pass those records that match to the
	 * <code>consumer</code>. Given that the dataset is relatively small, we favour
	 * simplicity over efficiency, and make no attempts to optimise the behaviour.
	 * <p>
	 * Entries that matches the major and minor versions of the <code>id</code> are
	 * considered to be matches: the <code>consumer</code> may be called multiple
	 * times. It's up to the caller to decide what to do with the multiple entries.
	 */
	private boolean findHistoricalCurations(IContentId id, Collection<IContentData> matches) {
		logger.debug("Querying IPLab historical data for {}.", id);
	
		try {
			var mapping = clonePath.resolve("mapping");
			try (var files = Files.list(mapping)) {
				files
					.map(path -> findHistoricalMappingInFile(id, path))
					.filter(Objects::nonNull)
					.forEach(match -> {
						logger.debug("Found close historical match {}.", match.getRealId());
						matches.add(match);
					});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	
	private boolean compatibleVersions(IContentId base, IContentId candidate) {
		if (!base.getType().equals(candidate.getType())) return false;
		if (!base.getSource().equals(candidate.getSource())) return false;
		if (!base.getNamespace().equals(candidate.getNamespace())) return false;
		if (!base.getName().equals(candidate.getName())) return false;
		
		if (base.getVersion().equals(candidate.getVersion())) return true;
		
		var baseVersion = new Version(base.getVersion());
		var candidateVersion = new Version(candidate.getVersion());
		
		if (!baseVersion.isSemantic() || !candidateVersion.isSemantic()) return false;
		
		return baseVersion.major() == candidateVersion.major() && baseVersion.minor() == candidateVersion.minor();
	}
	
	private IPLabHistoricalData findHistoricalMappingInFile(IContentId id, Path path) {
		if (!path.getFileName().toString().endsWith(".csv")) return null;
		try (var lines = Files.lines(path)) {
			return lines
					.skip(1)
					.map(each -> new IPLabHistoricalData(id, each))
					.filter(each -> each.isValid())
					.filter(each -> each.getStatus() == Status.Approved)
					.filter(each -> compatibleVersions(id, each.getRealId()))
					.findFirst()
					.orElse(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	class IPLabHistoricalData implements IContentData {
		private IContentId id;
		private String[] parts;

		public IPLabHistoricalData(IContentId id, String line) {
			this.id = id;
			parts = line.split(",");
		}
		
		public boolean isValid() {
			return parts.length == 4;
		}

		@Override
		public IContentId getId() {
			return id;
		}
		
		public IContentId getRealId() {
			return ContentId.getContentId(parts[0]);
		}

		@Override
		public String getLicense() {
			return parts[1].trim();
		}

		@Override
		public int getScore() {
			return 100;
		}

		@Override
		public String getAuthority() {
			return parts[3].trim();
		}

		@Override
		public String getUrl() {
			return null;
		}
		
		@Override
		public Status getStatus() {
			return "approved".equals(parts[2].trim()) ? Status.Approved : Status.Restricted;
		}
	}
}

