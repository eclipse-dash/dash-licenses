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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cyclonedx.Version;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.License;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Property;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.PackageUrlIdParser;

/**
 * Writes an enriched CycloneDX SBOM as JSON, XML, or YAML (chosen from the output
 * file extension). This is a collector: it takes the already parsed {@link Bom} (from
 * {@link CycloneDXSbomReader}, so the input is only parsed once), receives every
 * Dash license result via {@link #accept}, and when the run finishes
 * {@link #close} stamps the licenses onto the components and writes the result out.
 */
public class CycloneDXSbomWriter implements IResultsCollector {

    private static final PackageUrlIdParser PACKAGE_URL_ID_PARSER = new PackageUrlIdParser();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    // ContentId defines value-based equals()/hashCode(), so it can be used as a key.
    private final Map<IContentId, LicenseData> licenceMap = new HashMap<>();
    private final File output;
    private final Bom sbom;

    public CycloneDXSbomWriter(Bom sbom, File output) {
        this.sbom = sbom;
        this.output = output;
    }

    @Override
    public void accept(LicenseData data) {
        licenceMap.put(data.getId(), data);
    }

    @Override
    public void close() {
        enrichComponents();

        // Serialize in the format named by the output extension.
        String name = output.getName().toLowerCase();
        try {
            String content;
            if (name.endsWith(".xml")) {
                content = BomGeneratorFactory.createXml(Version.VERSION_14, sbom).toXmlString();
            } else if (name.endsWith(".json")) {
                content = BomGeneratorFactory.createJson(Version.VERSION_14, sbom).toJsonString();
            } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
                content = toYaml();
            } else {
                System.out.println("[CycloneDXSbomWriter] Unsupported CycloneDX output format: " + name);
                return;
            }
            try (FileWriter writer = new FileWriter(output)) {
                writer.write(content);
            }
            System.out.println("[CycloneDXSbomWriter] Wrote enriched SBOM to: " + output.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The CycloneDX library has no YAML generator, but CycloneDX YAML has the same
    // structure as JSON, so generate JSON and re-serialize the tree as YAML.
    private String toYaml() throws Exception {
        String json = BomGeneratorFactory.createJson(Version.VERSION_14, sbom).toJsonString();
        JsonNode tree = JSON_MAPPER.readTree(json);
        return YAML_MAPPER.writeValueAsString(tree);
    }

    // Stamp each component's resolved license and Dash status/url onto the Bom.
    private void enrichComponents() {
        if (sbom.getComponents() == null) {
            return;
        }
        for (Component component : sbom.getComponents()) {
            String purl = component.getPurl();
            if (purl == null) continue;

            var id = PACKAGE_URL_ID_PARSER.parseId(purl);
            if (id == null) continue;

            var data = licenceMap.get(id);
            if (data == null) continue;

            // attach the resolved license
            if (data.getLicense() != null) {
                License license = new License();
                license.setId(data.getLicense());
                LicenseChoice licenseChoice = new LicenseChoice();
                licenseChoice.addLicense(license);
                component.setLicenses(licenseChoice);
            }

            // attach Dash status + review url as custom properties
            List<Property> properties = component.getProperties() != null
                    ? new ArrayList<>(component.getProperties())
                    : new ArrayList<>();

            Property statusProp = new Property();
            statusProp.setName("eclipse.dash.status");
            statusProp.setValue(data.getStatus() == Status.Approved ? "approved" : "restricted");
            properties.add(statusProp);

            if (data.getUrl() != null) {
                Property urlProp = new Property();
                urlProp.setName("eclipse.dash.url");
                urlProp.setValue(data.getUrl());
                properties.add(urlProp);
            }

            component.setProperties(properties);
        }
    }
}
