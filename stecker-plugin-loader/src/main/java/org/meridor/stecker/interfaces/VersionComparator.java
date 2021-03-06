package org.meridor.stecker.interfaces;

import org.meridor.stecker.VersionRelation;

import java.util.Optional;

/**
 * Compares required and actual dependency versions and decides how they are related.
 */
public interface VersionComparator {

    /**
     * Returns how actual version is related to required, e.g. whether it's less or greater than required or in required
     * version range. See {@link org.meridor.stecker.VersionRelation} values for details.
     *
     * @param required required version or version range or nothing
     * @param actual   actual version or nothing
     * @return how actual version relates to required
     */
    VersionRelation compare(Optional<String> required, Optional<String> actual);

}
