/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import java.util.Optional;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateShellObjectMask;

/**
 * Constructs a shell around an object-mask using a standard dilation and erosion process
 *
 * @author Owen Feehan
 */
public abstract class IntensityMeanShellBase extends FeatureNrgChnl {

    // START BEAN PROPERTIES
    @BeanField @NonNegative private int iterationsErosion = 0;

    @BeanField private int iterationsDilation = 0;

    /**
     * Iff TRUE, calculates instead on the inverse of the mask (what's left when the shell is
     * removed)
     */
    @BeanField private boolean inverse = false;

    /**
     * A channel of the nrgStack that is used as an additional mask using default byte values for ON
     * and OFF
     */
    @BeanField private int nrgIndexMask = -1;

    @BeanField private boolean inverseMask = false; // Uses the inverse of the passed mask

    @BeanField private double emptyValue = 255;

    @BeanField private boolean do3D = true;

    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (iterationsDilation == 0 && iterationsErosion == 0) {
            throw new BeanMisconfiguredException(
                    "At least one of iterationsDilation and iterationsErosion must be positive");
        }
    }

    @Override
    protected double calcForChnl(SessionInput<FeatureInputSingleObject> input, Channel chnl)
            throws FeatureCalcException {

        ObjectMask objectShell = createShell(input);

        if (nrgIndexMask != -1) {
            // If an NRG mask is defined...
            Optional<ObjectMask> omIntersected =
                    intersectWithNRGMask(
                            objectShell, input.get().getNrgStackRequired().getNrgStack());

            if (omIntersected.isPresent()) {
                objectShell = omIntersected.get();
            } else {
                return emptyValue;
            }
        }

        return calcForShell(objectShell, chnl);
    }

    private ObjectMask createShell(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalcException {
        return input.calc(
                CalculateShellObjectMask.of(
                        input.resolver(),
                        iterationsDilation,
                        getIterationsErosion(),
                        0,
                        isDo3D(),
                        inverse));
    }

    private Optional<ObjectMask> intersectWithNRGMask(ObjectMask object, NRGStack nrgStack) {
        return object.intersect(createNrgMask(nrgStack), nrgStack.getDimensions());
    }

    protected abstract double calcForShell(ObjectMask shell, Channel chnl)
            throws FeatureCalcException;

    private ObjectMask createNrgMask(NRGStack nrgStack) {
        return new ObjectMask(
                new BoundingBox(nrgStack.getDimensions().getExtent()),
                nrgStack.getChnl(nrgIndexMask).getVoxelBox().asByte(),
                inverseMask
                        ? BinaryValues.getDefault().createInverted()
                        : BinaryValues.getDefault());
    }

    @Override
    public String getParamDscr() {
        return String.format(
                "%s,iterationsDilation=%d,iterationsErosion=%d,inverse=%s,do3D=%s",
                super.getParamDscr(),
                iterationsDilation,
                iterationsErosion,
                inverse ? "true" : "false",
                do3D ? "true" : "false");
    }

    public int getIterationsDilation() {
        return iterationsDilation;
    }

    public void setIterationsDilation(int iterationsDilation) {
        this.iterationsDilation = iterationsDilation;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public int getNrgIndexMask() {
        return nrgIndexMask;
    }

    public void setNrgIndexMask(int nrgIndexMask) {
        this.nrgIndexMask = nrgIndexMask;
    }

    public boolean isInverseMask() {
        return inverseMask;
    }

    public void setInverseMask(boolean inverseMask) {
        this.inverseMask = inverseMask;
    }

    public double getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(double emptyValue) {
        this.emptyValue = emptyValue;
    }

    public int getIterationsErosion() {
        return iterationsErosion;
    }

    public void setIterationsErosion(int iterationsErosion) {
        this.iterationsErosion = iterationsErosion;
    }

    public boolean isDo3D() {
        return do3D;
    }

    public void setDo3D(boolean do3D) {
        this.do3D = do3D;
    }
}
