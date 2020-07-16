/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderIfPixelZero;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CalcOutlineRGB {

    /**
     * creates a stack given an outline, a background, and a blue-channel
     *
     * <p>Rules. a) Outline-boundary replaces any existing pixels, and is shown in Green b) The blue
     * channel is shown, only if it has higher intensity than the equivalent blue in the background
     *
     * @param outline
     * @param background
     * @param blueToAssign
     * @return
     * @throws CreateException
     * @throws InitException
     */
    public static Stack apply(
            Mask outline, DisplayStack background, Channel blueToAssign, boolean createShort)
            throws CreateException {

        if (background.getNumChnl() == 3) {
            return apply(
                    outline,
                    background.createChnlDuplicate(0),
                    background.createChnlDuplicate(1),
                    background.createChnlDuplicate(2),
                    blueToAssign,
                    createShort);
        } else {
            return apply(
                    outline,
                    background.createChnlDuplicate(0),
                    background.createChnlDuplicate(0),
                    background.createChnlDuplicate(0),
                    blueToAssign,
                    createShort);
        }
    }

    public static Stack apply(
            Mask outline,
            Channel backgroundRed,
            Channel backgroundGreen,
            Channel backgroundBlue,
            Channel blueToAssign,
            boolean createShort)
            throws CreateException {

        // Duplicate background and blue
        blueToAssign = blueToAssign.duplicate();

        // We zero the pixels on the background and blue that are on our outline
        zeroPixels(outline, new Channel[] {backgroundBlue, backgroundGreen, blueToAssign});

        VoxelDataType outputType =
                createShort
                        ? VoxelDataTypeUnsignedShort.INSTANCE
                        : VoxelDataTypeUnsignedByte.INSTANCE;

        Channel chnlGreen = imposeOutlineOnChnl(outline, backgroundGreen, outputType);
        Channel chnlBlue = MaxChnls.apply(backgroundBlue, blueToAssign, outputType);

        return StackProviderRGBChnlProvider.createRGBStack(
                backgroundRed, chnlGreen, chnlBlue, outputType);
    }

    private static Channel imposeOutlineOnChnl(
            Mask outline, Channel chnl, VoxelDataType outputType) {

        double multFact =
                (double) outputType.maxValue() / outline.getChannel().getVoxelDataType().maxValue();

        return ChnlProviderIfPixelZero.mergeViaZeroCheck(
                outline.getChannel(), chnl, outputType, multFact);
    }

    private static void zeroPixels(Mask outline, Channel[] chnlArr) {
        ObjectMask objectOutline = CreateFromEntireChnlFactory.createObject(outline);
        for (Channel chnl : chnlArr) {
            chnl.getVoxelBox().any().setPixelsCheckMask(objectOutline, 0);
        }
    }
}
