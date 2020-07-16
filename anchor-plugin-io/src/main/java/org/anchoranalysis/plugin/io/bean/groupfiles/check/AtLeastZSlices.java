/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles.check;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.plugin.io.multifile.IntegerRange;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

public class AtLeastZSlices extends CheckParsedFilePathBag {

    // START BEAN PROPERTIES
    @BeanField private int minNumZSlices = 1;
    // END BEAN PROPERTIES

    @Override
    public boolean accept(ParsedFilePathBag parsedBag) {
        Optional<IntegerRange> sliceRange = parsedBag.rangeSliceNum();
        return sliceRange.isPresent() && sliceRange.get().getSize() >= minNumZSlices;
    }

    public int getMinNumZSlices() {
        return minNumZSlices;
    }

    public void setMinNumZSlices(int minNumZSlices) {
        this.minNumZSlices = minNumZSlices;
    }
}
