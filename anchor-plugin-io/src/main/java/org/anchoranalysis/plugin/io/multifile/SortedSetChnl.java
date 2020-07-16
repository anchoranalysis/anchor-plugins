/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import java.util.Iterator;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SortedSetChnl implements Comparable<SortedSetChnl>, Iterable<SortedSetSlice> {

    @Getter private int chnlNum;

    private TreeMap<Integer, SortedSetSlice> slices = new TreeMap<>();

    public int numSlices() {
        return this.slices.size();
    }

    public void add(int sliceNum, String filePath) {

        SortedSetSlice slice = slices.get(sliceNum);

        if (slice == null) {
            slice = new SortedSetSlice(filePath, sliceNum);
            slices.put(sliceNum, slice);
        } else {
            slice.setFilePath(filePath);

            // We shouldn't ever have two channels with the same ID
            assert false;
        }
    }

    @Override
    public int compareTo(SortedSetChnl arg0) {

        if (chnlNum == arg0.chnlNum) {
            return 0;
        } else if (chnlNum < arg0.chnlNum) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public Iterator<SortedSetSlice> iterator() {
        return slices.values().iterator();
    }
}
