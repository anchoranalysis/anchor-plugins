/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.mpp.io.cfg.CfgDeserializer;

public class CfgComparer extends Comparer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;
    // END BEAN PROPERTIES

    private static final RegionMembershipWithFlags REGION_MEMBERSHIP =
            RegionMapSingleton.instance()
                    .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE);

    @Override
    public Findable<ObjectCollection> createObjects(
            Path filePathSource, ImageDimensions dimensions, boolean debugMode)
            throws CreateException {

        Path filePath;
        try {
            filePath = filePathGenerator.outFilePath(filePathSource, false);
        } catch (AnchorIOException e1) {
            throw new CreateException(e1);
        }

        if (!filePath.toFile().exists()) {
            return new NotFound<>(
                    filePath, "No cfg exists at path"); // There's nothing to annotate against
        }

        CfgDeserializer deserialized = new CfgDeserializer();
        Cfg cfg;
        try {
            cfg = deserialized.deserialize(filePath);
        } catch (DeserializationFailedException e) {
            throw new CreateException(e);
        }

        ObjectCollection mask =
                cfg.calcMask(dimensions, REGION_MEMBERSHIP, BinaryValuesByte.getDefault())
                        .withoutProperties();
        return new Found<>(mask);
    }
}
