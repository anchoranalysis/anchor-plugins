/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterwriter;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import org.anchoranalysis.image.extent.ImageDimensions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MetadataUtilities {

    // NOTE: Tidy up exceptions later
    public static IMetadata createMetadata(
            ImageDimensions sd,
            int numChnl,
            PixelType pixelType,
            boolean makeRGB,
            boolean pretendSeries)
            throws ServiceException, DependencyException {

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata meta = service.createOMEXMLMetadata();

        meta.createRoot();

        int seriesNum = 0;

        meta.setImageID(String.format("Image:%d", seriesNum), seriesNum);
        meta.setPixelsID(String.format("Pixels:%d", seriesNum), seriesNum);
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, seriesNum, 0);
        meta.setPixelsDimensionOrder(DimensionOrder.XYZTC, seriesNum);
        meta.setPixelsType(pixelType, seriesNum);
        meta.setPixelsSizeC(new PositiveInteger(numChnl), seriesNum);

        meta.setPixelsSizeX(new PositiveInteger(sd.getX()), seriesNum);
        meta.setPixelsSizeY(new PositiveInteger(sd.getY()), seriesNum);

        // We pretend Z-stacks are Time frames as it makes it easier to
        //   view in other software if they are a series
        if (pretendSeries) {
            meta.setPixelsSizeT(new PositiveInteger(sd.getZ()), seriesNum);
            meta.setPixelsSizeZ(new PositiveInteger(1), seriesNum);
        } else {
            meta.setPixelsSizeT(new PositiveInteger(1), seriesNum);
            meta.setPixelsSizeZ(new PositiveInteger(sd.getZ()), seriesNum);
        }

        meta.setPixelsPhysicalSizeX(createLength(sd.getRes().getX() * sd.getX()), 0);
        meta.setPixelsPhysicalSizeY(createLength(sd.getRes().getY() * sd.getY()), 0);
        meta.setPixelsPhysicalSizeZ(createLength(sd.getRes().getZ() * sd.getZ()), 0);

        addNumChnls(meta, calcNumChnls(makeRGB), calcSamplesPerPixel(makeRGB), seriesNum);

        return meta;
    }

    private static int calcNumChnls(boolean makeRGB) {
        return makeRGB ? 1 : 3;
    }

    private static int calcSamplesPerPixel(boolean makeRGB) {
        // We do the opposite of calcNumChnls
        return calcNumChnls(!makeRGB);
    }

    private static void addNumChnls(
            IMetadata meta, int numChnl, int samplesPerPixel, int seriesNum) {
        for (int i = 0; i < numChnl; i++) {
            meta.setChannelID(String.format("Channel:%d:%d", seriesNum, i), seriesNum, i);
            meta.setChannelSamplesPerPixel(new PositiveInteger(samplesPerPixel), seriesNum, i);
        }
    }

    private static Length createLength(double valMeters) {
        return FormatTools.createLength(valMeters, UNITS.METER);
    }
}
