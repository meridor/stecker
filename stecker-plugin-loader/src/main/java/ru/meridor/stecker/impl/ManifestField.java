package ru.meridor.stecker.impl;

public enum ManifestField {

    NAME("Plugin-Name"),
    VERSION("Plugin-Version"),
    DATE("Plugin-Date"),
    DESCRIPTION("Plugin-Description"),
    MAINTAINER("Plugin-Maintainer"),
    DEPENDS("Plugin-Depends"),
    CONFLICTS("Plugin-Conflicts"),
    PROVIDES("Plugin-Provides");

    private final String fieldName;

    private ManifestField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
