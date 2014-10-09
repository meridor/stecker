package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.ManifestReader;
import ru.meridor.tools.plugin.PluginException;
import ru.meridor.tools.plugin.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class DefaultManifestReader implements ManifestReader {

    public static final String DEPENDENCY_DELIMITER = ";";
    public static final String VERSION_DELIMITER = "=";

    @Override
    public PluginMetadata read(Path pluginFile) throws PluginException {
        try {
            Manifest manifest = getManifest(pluginFile);
            PluginMetadataContainer pluginMetadata = new PluginMetadataContainer(
                    getRequiredField(manifest, ManifestField.NAME),
                    getRequiredField(manifest, ManifestField.VERSION),
                    pluginFile
            );
            pluginMetadata.setDate(getDateField(manifest, ManifestField.DATE));
            pluginMetadata.setDescription(getField(manifest, ManifestField.DESCRIPTION));
            pluginMetadata.setMaintainer(getField(manifest, ManifestField.MAINTAINER));
            pluginMetadata.addRequiredDependencies(getDependenciesList(manifest, ManifestField.DEPENDS));
            pluginMetadata.addConflictingDependencies(getDependenciesList(manifest, ManifestField.CONFLICTS));

            Optional<String> rawProvides = getField(manifest, ManifestField.PROVIDES);
            pluginMetadata.setProvidedDependency(
                    rawProvides.isPresent() ?
                            Optional.of(getDependency(rawProvides.get())) :
                            Optional.empty()
            );

            return pluginMetadata;
        } catch (Exception e) {
            throw new PluginException("Invalid manifest in plugin file " + pluginFile.toAbsolutePath(), e);
        }
    }

    protected Manifest getManifest(Path pluginFile) throws IOException {
        JarInputStream jarStream = new JarInputStream(Files.newInputStream(pluginFile));
        return jarStream.getManifest();
    }

    protected Optional<String> getField(Manifest manifest, ManifestField field) {
        return Optional.ofNullable(manifest.getMainAttributes().getValue(field.getFieldName()));
    }

    protected String getRequiredField(Manifest manifest, ManifestField field) throws ManifestException {
        Optional<String> value = getField(manifest, field);
        if (!value.isPresent()){
            throw new ManifestException(String.format("Required field %s not found in plugin manifest", field.getFieldName()));
        }
        return value.get();
    }

    protected List<Dependency> getDependenciesList(Manifest manifest, ManifestField field) throws ManifestException {
        Optional<String> value = getField(manifest, field);
        if (!value.isPresent()) {
            return Collections.emptyList();
        }
        List<String> rawDependencies = Arrays.asList(value.get().split(DEPENDENCY_DELIMITER));
        List<Dependency> dependenciesList = new ArrayList<>();
        for (String rawDependency: rawDependencies) {
            dependenciesList.add(getDependency(rawDependency.trim()));
        }
        return dependenciesList;
    }

    protected Dependency getDependency(String rawDependency) throws ManifestException {
        String[] nameAndVersion = rawDependency.split(VERSION_DELIMITER);
        switch (nameAndVersion.length) {
            case 1: return new DependencyContainer(nameAndVersion[0].trim());
            case 2: return new DependencyContainer(nameAndVersion[0].trim(), nameAndVersion[1].trim());
            default: throw new ManifestException(String.format("Invalid dependency specification: %s", rawDependency));
        }
    }

    protected Optional<ZonedDateTime> getDateField(Manifest manifest, ManifestField field) throws ManifestException {
        try {
            Optional<String> value = getField(manifest, field);
            if (!value.isPresent()){
                return Optional.empty();
            }
            return Optional.of(ZonedDateTime.parse(value.get(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (Exception e) {
            throw new ManifestException(String.format("Invalid date and time specification"));
        }
    }

    protected class ManifestException extends Exception {

        public ManifestException(String message) {
            super(message);
        }
    }

}
