/* (C)2020 */
package org.anchoranalysis.plugin.image.task.sharedstate;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.anchoranalysis.io.manifest.ManifestFolderDescription;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/** A set of output-managers, one for each group */
class GroupedMultiplexOutputManagers {

    private static final ManifestFolderDescription MANIFEST_DESCRIPTION =
            new ManifestFolderDescription("groupOutput", "outputManager", new SetSequenceType());

    private Map<String, BoundOutputManagerRouteErrors> map;

    public GroupedMultiplexOutputManagers(
            BoundOutputManagerRouteErrors baseOutputManager, Set<String> groups) {

        map = new TreeMap<>();

        for (String key : groups) {
            map.put(key, baseOutputManager.deriveSubdirectory(key, MANIFEST_DESCRIPTION));
        }
    }

    // Returns null if no object exists
    public BoundOutputManagerRouteErrors getOutputManagerFor(String key) {
        return map.get(key);
    }
}
