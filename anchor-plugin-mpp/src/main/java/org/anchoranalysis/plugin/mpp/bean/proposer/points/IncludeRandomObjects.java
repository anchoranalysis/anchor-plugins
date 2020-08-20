/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

/**
 * 1. Iterates over each object 2. For each object, with a random probability of 0.5, includes all
 * pixels inside the object
 *
 * @author feehano
 */
public class IncludeRandomObjects extends PointsProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<List<Point3i>> propose(
            Point3d point,
            Mark mark,
            Dimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode) {

        List<Point3i> out = new ArrayList<>();

        try {
            ObjectCollection objectCollection = objects.create();

            for (ObjectMask object : objectCollection) {
                maybeAddToList(object, out, randomNumberGenerator);
            }

        } catch (CreateException e) {
            errorNode.add(e);
            return Optional.empty();
        }

        return Optional.of(out);
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }

    private static void maybeAddToList(
            ObjectMask object, List<Point3i> out, RandomNumberGenerator randomNumberGenerator) {
        if (randomNumberGenerator.sampleDoubleZeroAndOne() > 0.5) {
            out.addAll(PointsFromObject.listFrom3i(object));
        }
    }
}
