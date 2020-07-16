/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.slice;

import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes three RGB channels and projects them into a canvas of width/height in the form of a new RGB
 * stack
 *
 * @author Owen Feehan
 */
class ExtractProjectedStack {

    private int width;
    private int height;

    public ExtractProjectedStack(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    public Stack extractAndProjectStack(Channel red, Channel green, Channel blue, int z)
            throws IncorrectImageSizeException {
        Stack stack = new Stack();
        extractAndProjectChnl(red, z, stack);
        extractAndProjectChnl(green, z, stack);
        extractAndProjectChnl(blue, z, stack);
        return stack;
    }

    private void extractAndProjectChnl(Channel chnl, int z, Stack stack)
            throws IncorrectImageSizeException {
        Channel chnlProjected = createProjectedChnl(chnl.extractSlice(z));
        stack.addChnl(chnlProjected);
    }

    private Channel createProjectedChnl(Channel chnlIn) {

        // Then the mode is off
        if (width == -1
                || height == -1
                || (chnlIn.getDimensions().getX() == width
                        && chnlIn.getDimensions().getY() == height)) {
            return chnlIn;
        } else {
            Extent eOut = new Extent(width, height, 1);
            Point3i crnrPos = createTarget(chnlIn.getDimensions(), eOut);

            BoundingBox bboxToProject =
                    boxToProject(crnrPos, chnlIn.getDimensions().getExtent(), eOut);

            BoundingBox bboxSrc = bboxSrc(bboxToProject, chnlIn.getDimensions());

            return copyPixels(bboxSrc, bboxToProject, chnlIn, eOut);
        }
    }

    private static Point3i createTarget(ImageDimensions sd, Extent e) {
        Point3i crnrPos = new Point3i();
        crnrPos.setX((e.getX() - sd.getX()) / 2);
        crnrPos.setY((e.getY() - sd.getY()) / 2);
        crnrPos.setZ(0);
        return crnrPos;
    }

    private static BoundingBox boxToProject(Point3i crnrPos, Extent eChnl, Extent eTrgt) {
        return new BoundingBox(crnrPos, eChnl)
                .intersection()
                .with(new BoundingBox(eTrgt))
                .orElseThrow(AnchorImpossibleSituationException::new);
    }

    private static BoundingBox bboxSrc(BoundingBox bboxToProject, ImageDimensions sd) {
        Point3i srcCrnrPos = createSrcCrnrPos(bboxToProject, sd);
        return new BoundingBox(srcCrnrPos, bboxToProject.extent());
    }

    private static Point3i createSrcCrnrPos(BoundingBox bboxToProject, ImageDimensions sd) {
        Point3i srcCrnrPos = new Point3i(0, 0, 0);

        if (bboxToProject.extent().getX() < sd.getX()) {
            srcCrnrPos.setX((sd.getX() - bboxToProject.extent().getX()) / 2);
        }

        if (bboxToProject.extent().getY() < sd.getY()) {
            srcCrnrPos.setY((sd.getY() - bboxToProject.extent().getY()) / 2);
        }
        return srcCrnrPos;
    }

    private Channel copyPixels(
            BoundingBox bboxSrc, BoundingBox bboxToProject, Channel chnl, Extent eOut) {

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                new ImageDimensions(eOut, chnl.getDimensions().getRes()),
                                VoxelDataTypeUnsignedByte.INSTANCE);
        chnl.getVoxelBox()
                .asByte()
                .copyPixelsTo(bboxSrc, chnlOut.getVoxelBox().asByte(), bboxToProject);
        return chnlOut;
    }
}
