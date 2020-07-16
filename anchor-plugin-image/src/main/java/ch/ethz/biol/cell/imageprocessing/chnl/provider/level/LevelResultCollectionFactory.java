/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider.level;

import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

public class LevelResultCollectionFactory {

    private LevelResultCollectionFactory() {}

    public static LevelResultCollection createCollection(
            Channel chnl,
            ObjectCollection objects,
            CalculateLevel calculateLevel,
            int numDilations,
            MessageLogger logger)
            throws CreateException {

        LevelResultCollection all = new LevelResultCollection();

        for (ObjectMask objectMask : objects) {

            ObjectMask objectForCalculateLevel;

            logger.logFormatted(
                    "Creating level result %s", objectMask.centerOfGravity().toString());

            // Optional dilation
            if (numDilations != 0) {
                objectForCalculateLevel =
                        MorphologicalDilation.createDilatedObject(
                                objectMask,
                                Optional.of(chnl.getDimensions().getExtent()),
                                chnl.getDimensions().getZ() > 1,
                                numDilations,
                                false);
            } else {
                objectForCalculateLevel = objectMask;
            }

            Histogram h = HistogramFactory.create(chnl, objectForCalculateLevel);
            int level;
            try {
                level = calculateLevel.calculateLevel(h);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            LevelResult res = new LevelResult(level, objectMask, h);

            logger.logFormatted("Level result is %d", res.getLevel());

            all.add(res);
        }
        return all;
    }
}
