/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.PackageUrlIdParser;

import org.spdx.spdxRdfStore.RdfStore;
import org.spdx.library.SpdxModelFactory;
import org.spdx.library.model.v2.SpdxConstantsCompatV2;
import org.spdx.library.model.v2.SpdxPackage;
import org.spdx.library.model.v2.ExternalRef;
import org.spdx.library.model.v2.enumerations.ReferenceCategory;

/**
 * Reads SPDX SBOMs (JSON, YAML, tag-value, and RDF/XML). Each package carries a
 * list of external references; this extracts the ones whose category is
 * "package manager" and type is "purl" as {@link IContentId}s.
 *
 * Created via the {@link #forFile(File)} factory, which returns null for files
 * that are not SPDX so callers can fall through to the next reader.
 */
public class SpdxSbomReader implements IDependencyListReader {

    private final File file;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final PackageUrlIdParser PURL_PARSER = new PackageUrlIdParser();

    public SpdxSbomReader(File file) {
        this.file = file;
    }

    /**
     * Create a reader if the file is an SPDX SBOM (RDF/tag-value by extension, or
     * JSON/YAML that carries the SPDX-only "spdxVersion" field). Returns null
     * otherwise, so the caller can fall through to the next reader.
     */
    public static SpdxSbomReader forFile(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".rdf") || name.endsWith(".rdf.xml") || name.endsWith(".spdx")) {
            return new SpdxSbomReader(file);
        }

        // JSON and YAML are shared with CycloneDX; SPDX files carry "spdxVersion".
        if (name.endsWith(".json") && hasSpdxVersion(file, OBJECT_MAPPER)) {
            return new SpdxSbomReader(file);
        }
        if ((name.endsWith(".yaml") || name.endsWith(".yml")) && hasSpdxVersion(file, YAML_MAPPER)) {
            return new SpdxSbomReader(file);
        }

        return null;
    }

    private static boolean hasSpdxVersion(File file, ObjectMapper mapper) {
        try {
            return mapper.readTree(file).has("spdxVersion");
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Collection<IContentId> getContentIds() {
        String name = file.getName().toLowerCase();
        // check ".rdf"/".rdf.xml" first: a ".rdf.xml" double extension also ends with ".xml"
        if (name.endsWith(".rdf") || name.endsWith(".rdf.xml")) {
            return parseSpdxRdfXml(file);
        }
        if (name.endsWith(".json")) {
            return parseSpdxJson(file);
        }
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return parseSpdxYaml(file);
        }
        // tag-value is sometimes saved as .txt rather than .spdx
        if (name.endsWith(".spdx") || name.endsWith(".txt")) {
            return parseSpdxTagValue(file);
        }
        throw new RuntimeException("Unsupported SPDX SBOM format for file: " + file.getPath());
    }

    private List<IContentId> parseSpdxJson(File file) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(file);
            return extractPurls(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<IContentId> parseSpdxYaml(File file) {
        try {
            JsonNode root = YAML_MAPPER.readTree(file);
            return extractPurls(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Walk packages -> externalRefs, keeping the package-manager purl references.
    // JSON and YAML parse to the same node tree, so both share this.
    private List<IContentId> extractPurls(JsonNode root) {
        List<IContentId> results = new ArrayList<>();
        for (JsonNode pkg : root.path("packages")) {
            for (JsonNode ref : pkg.path("externalRefs")) {
                String category = ref.path("referenceCategory").asText("");
                String type = ref.path("referenceType").asText("");

                // SPDX 2.2 and 2.3 use different package-manager spellings.
                if ((category.equalsIgnoreCase("PACKAGE-MANAGER") || category.equalsIgnoreCase("PACKAGE_MANAGER"))
                        && type.equalsIgnoreCase("purl")) {
                    String purl = ref.path("referenceLocator").asText(null);
                    if (purl != null) {
                        IContentId id = PURL_PARSER.parseId(purl);
                        if (id != null) {
                            results.add(id);
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<IContentId> parseSpdxTagValue(File file) {
        try {
            List<IContentId> results = new ArrayList<>();
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (!line.startsWith("ExternalRef:")) continue;

                // an ExternalRef line is: "ExternalRef: <category> <type> <locator>"
                String[] parts = line.substring("ExternalRef:".length()).trim().split("\\s+", 3);
                if (parts.length == 3
                        && (parts[0].equalsIgnoreCase("PACKAGE-MANAGER") || parts[0].equalsIgnoreCase("PACKAGE_MANAGER"))
                        && parts[1].equalsIgnoreCase("purl")) {
                    IContentId id = PURL_PARSER.parseId(parts[2]);
                    if (id != null) results.add(id);
                }
            }
            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<IContentId> parseSpdxRdfXml(File file) {
        // Declared outside the try so anything collected before a failure is still returned.
        List<IContentId> results = new ArrayList<>();
        try {
            // RdfStore parses the RDF/XML for us and returns the SPDX document URI.
            RdfStore rdfStore = new RdfStore();
            String documentUri = rdfStore.loadModelFromFile(file.getPath(), false);

            // Ask the library for every SPDX package as a typed object. The cast is safe
            // because we requested CLASS_SPDX_PACKAGE (hence @SuppressWarnings above).
            List<SpdxPackage> packages = (List<SpdxPackage>) SpdxModelFactory
                    .getSpdxObjects(rdfStore, null, SpdxConstantsCompatV2.CLASS_SPDX_PACKAGE, documentUri, null)
                    .collect(Collectors.toList());

            for (SpdxPackage pkg : packages) {
                for (ExternalRef ref : pkg.getExternalRefs()) {
                    // ReferenceCategory is an enum, so the library already normalizes the
                    // SPDX 2.2/2.3 spelling difference for us. In RDF the reference type is
                    // the full listed-reference URI (e.g. http://spdx.org/rdf/references/purl),
                    // not the bare string "purl".
                    String refTypeUri = ref.getReferenceType().getIndividualURI();
                    if (ref.getReferenceCategory() == ReferenceCategory.PACKAGE_MANAGER
                            && (SpdxConstantsCompatV2.SPDX_LISTED_REFERENCE_TYPES_PREFIX + "purl")
                                    .equalsIgnoreCase(refTypeUri)) {
                        IContentId id = PURL_PARSER.parseId(ref.getReferenceLocator());
                        if (id != null) {
                            results.add(id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
