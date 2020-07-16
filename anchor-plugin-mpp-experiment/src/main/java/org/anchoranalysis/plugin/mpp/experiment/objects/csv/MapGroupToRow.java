/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.objects.csv;

import java.util.Collection;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Maps Group (String) to a CSVRow
 *
 * @author Owen Feehan
 */
public class MapGroupToRow {

    // Maps the group name to each row
    private MultiValueMap delegate = new MultiValueMap();

    public void put(String key, CSVRow value) {
        delegate.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Collection<CSVRow> get(String key) {
        return (Collection<CSVRow>) delegate.getCollection(key);
    }
}
