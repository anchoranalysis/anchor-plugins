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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark.collection;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.mpp.feature.error.CheckException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.EllipsoidFactory;

public class FromObjects extends MarkCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @Getter @Setter private boolean suppressZCovariance = false;

    @BeanField @Getter @Setter private double shellRad = 0.2;

    @BeanField @OptionalBean @Getter @Setter private CheckMark checkMark;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof Ellipsoid;
    }

    @Override
    public Optional<MarkCollection> propose(
            MarkWithIdentifierFactory markFactory, ProposerContext context)
            throws ProposalAbnormalFailureException {

        ObjectCollection objectsCreated;
        try {
            objectsCreated = objects.create();
        } catch (CreateException e) {
            throw new ProposalAbnormalFailureException("Failed to create objects", e);
        }

        MarkCollection marks = new MarkCollection();

        try {
            if (checkMark != null) {
                checkMark.start(context.getEnergyStack());
            }
        } catch (OperationFailedException e) {
            throw new ProposalAbnormalFailureException("Failed to start checkMark", e);
        }

        try {
            for (ObjectMask object : objectsCreated) {
                Mark mark = createFromObject(object, context.getEnergyStack());
                mark.setId(markFactory.idAndIncrement());

                if (checkMark == null
                        || checkMark.check(
                                mark, context.getRegionMap(), context.getEnergyStack())) {
                    marks.add(mark);
                }
            }

            if (checkMark != null) {
                checkMark.end();
            }

            return Optional.of(marks);

        } catch (CreateException e) {
            throw new ProposalAbnormalFailureException(
                    "Failed to create a mark from the obj-mask", e);
        } catch (CheckException e) {
            throw new ProposalAbnormalFailureException(
                    "A failure occurred while checking the mark", e);
        }
    }

    private Ellipsoid createFromObject(ObjectMask object, EnergyStack energyStack)
            throws CreateException {
        return EllipsoidFactory.createMarkEllipsoidLeastSquares(
                object, energyStack.dimensions(), suppressZCovariance, shellRad);
    }
}
