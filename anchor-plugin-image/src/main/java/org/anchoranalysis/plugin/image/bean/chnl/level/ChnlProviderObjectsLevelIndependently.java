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

package org.anchoranalysis.plugin.image.bean.chnl.level;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResult;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollection;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollectionFactory;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.voxel.Voxels;

// Calculates a threshold-level for each object independently
public class ChnlProviderObjectsLevelIndependently extends ChnlProviderLevel {

    // START BEAN
    @BeanField @Getter @Setter private int numDilations = 0;
    // END BEAN

    @Override
    protected Channel createFor(Channel chnlIntensity, ObjectCollection objects, Channel chnlOutput)
            throws CreateException {

        try {
            LevelResultCollection results =
                    LevelResultCollectionFactory.createCollection(
                            chnlIntensity,
                            objects,
                            getCalculateLevel(),
                            numDilations,
                            getLogger().messageLogger());

            Voxels<?> voxelsOutput = chnlOutput.voxels().any();
            for (LevelResult result : results) {
                voxelsOutput.assignValue(result.getLevel()).toObject(result.getObject());
            }

            return chnlOutput;

        } catch (CreateException e) {
            throw new CreateException(e);
        }
    }
}
