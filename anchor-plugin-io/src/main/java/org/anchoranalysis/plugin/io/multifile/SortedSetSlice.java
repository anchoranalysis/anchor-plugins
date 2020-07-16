/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public class SortedSetSlice implements Comparable<SortedSetSlice> {

    @Getter @Setter private String filePath;

    @Getter @Setter private int zSliceNum;

    @Override
    public int compareTo(SortedSetSlice arg0) {

        if (zSliceNum == arg0.zSliceNum) {
            return 0;
        } else if (zSliceNum < arg0.zSliceNum) {
            return -1;
        } else {
            return 1;
        }
    }
}
