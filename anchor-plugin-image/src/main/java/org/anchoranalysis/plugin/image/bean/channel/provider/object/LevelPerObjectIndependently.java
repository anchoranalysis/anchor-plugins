/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.channel.provider.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.intensity.level.LevelResult;
import org.anchoranalysis.plugin.image.intensity.level.LevelResultCollection;
import org.anchoranalysis.plugin.image.intensity.level.LevelResultCollectionFactory;

/**
 * Creates a channel with different threshold-levels for each object, calculating the level only
 * from the histogram of the particular object.
 *
 * @author Owen Feehan
 */
public class LevelPerObjectIndependently extends LevelPerObjectBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int numberDilations = 0;
    // END BEAN PROPERTIES

    @Override
    protected void writeLevelsForObjects(
            Channel channelIntensity, ObjectCollection objects, Channel output)
            throws ProvisionFailedException {

        try {
            LevelResultCollection results =
                    LevelResultCollectionFactory.createCollection(
                            channelIntensity,
                            objects,
                            getCalculateLevel(),
                            numberDilations,
                            getLogger().messageLogger());

            Voxels<?> voxelsOutput = output.voxels().any();
            for (LevelResult result : results) {
                voxelsOutput.assignValue(result.getLevel()).toObject(result.getObject());
            }

        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
