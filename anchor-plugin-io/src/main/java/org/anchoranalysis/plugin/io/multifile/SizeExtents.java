/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import java.util.Optional;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.stack.Stack;

/**
 * Remembers the different sizes among the files
 *
 * <p>It assumes numbering in ranges begins from 0 inclusive.
 *
 * @author Owen Feehan
 */
public class SizeExtents {

    private Optional<IntegerRange> rangeZ;
    private Optional<IntegerRange> rangeC;
    private Optional<IntegerRange> rangeT;

    private Integer sizeX = null;
    private Integer sizeY = null;

    // Assumes numbering starts from 0
    public SizeExtents(ParsedFilePathBag fileBag) {
        this.rangeZ = fileBag.rangeSliceNum();
        this.rangeC = fileBag.rangeChnlNum();
        this.rangeT = fileBag.rangeTimeIndex();
    }

    public boolean hasNecessaryExtents() {
        return rangeC.isPresent() && rangeZ.isPresent() && rangeT.isPresent();
    }

    public void populateMissingFromArbitrarySlice(Stack stackArbitrarySlice) {

        sizeX = stackArbitrarySlice.getDimensions().getX();
        sizeY = stackArbitrarySlice.getDimensions().getY();

        if (!rangeC.isPresent()) {
            rangeC = Optional.of(new IntegerRange(stackArbitrarySlice.getNumChnl()));
        }

        if (!rangeZ.isPresent()) {
            rangeZ = Optional.of(new IntegerRange(stackArbitrarySlice.getDimensions().getZ()));
        }

        if (!rangeT.isPresent()) {
            // If there's no indexes associated with the files, we assume there's a single index
            rangeT = Optional.of(new IntegerRange(1));
        }
    }

    public Extent toExtent() {
        return new Extent(sizeX, sizeY, rangeZ.map(IntegerRange::getSize).orElse(1));
    }

    public IntegerRange getRangeZ() {
        return rangeZ.get();
    }

    public IntegerRange getRangeC() {
        return rangeC.get();
    }

    public IntegerRange getRangeT() {
        return rangeT.get();
    }

    public boolean rangeTPresent() {
        return rangeT.isPresent();
    }

    public boolean rangeCPresent() {
        return rangeC.isPresent();
    }
}
