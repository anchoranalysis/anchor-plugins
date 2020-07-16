/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

/**
 * A mapping used in {@link GroupFiles} between the key associated with each image, and other files
 *
 * @author Owen Feehan
 */
class GroupFilesMap {

    private Map<String, ParsedFilePathBag> map = new HashMap<>();

    public void add(String key, FileDetails fd) {
        ParsedFilePathBag bag = map.computeIfAbsent(key, k -> new ParsedFilePathBag());
        bag.add(fd);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public ParsedFilePathBag get(String key) {
        return map.get(key);
    }
}
