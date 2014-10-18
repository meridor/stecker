package ru.meridor.stecker.impl;

class VersionRange {

    private String startVersion = "";

    private boolean startVersionIncluded = false;

    private String endVersion = "";

    private boolean endVersionIncluded = false;

    private boolean isValid = false;

    public VersionRange(String range) {
        parse(range);
    }

    private void parse(String range) {
        if (range == null) {
            return;
        }
        String[] startEnd = range.trim().split(",");
        if (startEnd.length != 2) {
            return;
        }
        String start = startEnd[0].trim();
        String end = startEnd[1].trim();
        boolean startIncluded = start.startsWith("[");
        boolean startExcluded = start.startsWith("(");
        boolean endIncluded = end.endsWith("]");
        boolean endExcluded = end.endsWith(")");
        if (
                (!startIncluded && !startExcluded) ||
                        (!endIncluded && !endExcluded)
                ) {
            return;
        }
        this.startVersionIncluded = startIncluded;
        this.endVersionIncluded = endIncluded;
        this.startVersion = start.substring(1);
        this.endVersion = end.substring(0, end.length() - 1);
        this.isValid = true;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getEndVersion() {
        return endVersion;
    }

    public boolean isEndVersionIncluded() {
        return endVersionIncluded;
    }

    public String getStartVersion() {
        return startVersion;
    }

    public boolean isStartVersionIncluded() {
        return startVersionIncluded;
    }

    public boolean contains(String version) {
        if (version == null || !isValid()) {
            return false;
        }
        boolean isStartVersionOk = isStartVersionIncluded() ?
                getStartVersion().compareTo(version) <= 0 :
                getStartVersion().isEmpty() || getStartVersion().compareTo(version) < 0;
        boolean isEndVersionOk = isEndVersionIncluded() ?
                getEndVersion().compareTo(version) >= 0 :
                getEndVersion().isEmpty() || getEndVersion().compareTo(version) > 0;
        return isStartVersionOk && isEndVersionOk;
    }
}