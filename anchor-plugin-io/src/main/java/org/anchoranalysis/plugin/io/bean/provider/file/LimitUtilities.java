/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LimitUtilities {

    public static Collection<File> apply(Collection<File> filesIn, int maxNumItems) {

        ArrayList<File> filesOut = new ArrayList<>();

        int i = 0;
        for (File f : filesIn) {
            filesOut.add(f);

            if (++i == maxNumItems) {
                break;
            }
        }

        return filesOut;
    }
}
