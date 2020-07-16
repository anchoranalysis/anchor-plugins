/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.objects.csv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

/**
 * Maps FileID (String) to MapChild
 *
 * @author Owen Feehan
 */
class MapFileID {

    // Maps the fileID to a further map
    private Map<String, MapGroupToRow> delegate = new HashMap<>();

    // Maintains a set of all groups used
    @Getter private Set<String> setGroups = new HashSet<>();

    public void put(String fileID, String groupID, CSVRow row) {
        setGroups.add(groupID);

        // Get a GroupToRow, creating if necessary
        MapGroupToRow map = delegate.computeIfAbsent(fileID, key -> new MapGroupToRow());
        map.put(groupID, row);
    }

    public MapGroupToRow get(String key) {
        return delegate.get(key);
    }
}
