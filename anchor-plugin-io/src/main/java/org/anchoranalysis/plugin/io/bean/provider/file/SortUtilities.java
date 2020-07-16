/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SortUtilities {

    public static Collection<File> sortFiles(Collection<File> files) {
        List<File> out = new ArrayList<>();
        out.addAll(files);
        Collections.sort(out);
        return out;
    }
}
