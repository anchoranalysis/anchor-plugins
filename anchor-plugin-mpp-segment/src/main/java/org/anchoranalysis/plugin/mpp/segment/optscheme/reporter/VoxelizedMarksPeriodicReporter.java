/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment.optscheme.reporter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optscheme.step.Reporting;

public class VoxelizedMarksPeriodicReporter
        extends ObjectSerializerPeriodicReporter<MarksWithEnergyBreakdown> {

    // BEAN PARAMETERS
    @BeanField @Getter @Setter private boolean proposal = false;

    /** If proposal==true, this toggles between the primary and secondary proposal */
    @BeanField @Getter @Setter private boolean secondary = false;
    // END BEAN PARAMETERS

    public VoxelizedMarksPeriodicReporter() {
        super("marks");
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    @Override
    protected Optional<MarksWithEnergyBreakdown> generateIterableElement(
            Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException {

        if (proposal) {
            return marksForProposal(reporting);
        } else {
            return Optional.of(reporting.getMarksAfter().getMarks());
        }
    }

    private Optional<MarksWithEnergyBreakdown> marksForProposal(
            Reporting<VoxelizedMarksWithEnergy> reporting) {

        Optional<VoxelizedMarksWithEnergy> selected =
                secondary ? reporting.getProposalSecondary() : reporting.getProposal();
        return selected.map(VoxelizedMarksWithEnergy::getMarks);
    }
}