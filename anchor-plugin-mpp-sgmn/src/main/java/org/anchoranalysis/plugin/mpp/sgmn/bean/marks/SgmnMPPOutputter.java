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

package org.anchoranalysis.plugin.mpp.sgmn.bean.marks;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.MarksWithTotalEnergy;
import org.anchoranalysis.anchor.mpp.mark.ColoredMarks;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.generator.serialized.ObjectOutputStreamGenerator;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.generator.text.StringGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.marks.generator.MarksGenerator;
import org.anchoranalysis.mpp.io.marks.generator.MarksFlattenedGenerator;
import org.anchoranalysis.mpp.io.marks.ColoredMarksWithDisplayStack;
import org.anchoranalysis.mpp.io.marks.generator.MarksAsUniqueValueGenerator;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SgmnMPPOutputter {

    public static void outputResults(
            MarksWithEnergyBreakdown marks,
            DualStack dualStack,
            RegionMembershipWithFlags regionMembership,
            Logger logger,
            BoundOutputManagerRouteErrors outputManager)
            throws OperationFailedException {

        ColoredMarks coloredCfg =
                new ColoredMarks(
                        marks.getMarks(),
                        outputManager.getOutputWriteSettings().defaultColorIndexFor(20),
                        new IDGetterIter<Mark>());

        ColoredMarksWithDisplayStack coloredCfgDisplayStack =
                new ColoredMarksWithDisplayStack(coloredCfg, dualStack.getBgStack());

        WriterRouterErrors writer = outputManager.getWriterCheckIfAllowed();

        writeMarks(marks, writer);

        writeOutline("finalOutline", coloredCfgDisplayStack, regionMembership, writer, 1);
        writeOutline("finalOutlineVisual", coloredCfgDisplayStack, regionMembership, writer, 3);

        writeCfgMaskCollection(coloredCfgDisplayStack, writer, regionMembership);

        writeFinalCfg(marks.getMarks(), writer);
        outputCfgSize(marks.getMarksWithTotalEnergy(), writer, logger);
    }

    private static void writeMarks(MarksWithEnergyBreakdown marks, WriterRouterErrors writer) {
        writer.write("finalMarks", () -> new XStreamGenerator<>(marks, Optional.of("marks")));
    }

    private static void writeCfgMaskCollection(
            ColoredMarksWithDisplayStack coloredCfgDisplayStack,
            WriterRouterErrors writer,
            RegionMembershipWithFlags rm) {
        writer.write(
                "finalMarksRaster",
                () ->
                        new MarksAsUniqueValueGenerator(
                                coloredCfgDisplayStack.getStack().dimensions(),
                                rm,
                                coloredCfgDisplayStack.getMarksColored().getMarks()));
    }

    private static void writeOutline(
            String outputNamePrefix,
            ColoredMarksWithDisplayStack coloredCfgDisplayStack,
            RegionMembershipWithFlags rm,
            WriterRouterErrors writer,
            int outlineWidth) {

        DrawObject outlineWriter = new Outline(outlineWidth);
        writer.write(
                outputNamePrefix,
                () ->
                        new MarksGenerator(
                                outlineWriter,
                                coloredCfgDisplayStack,
                                new IDGetterIter<Overlay>(),
                                rm));
        writer.write(
                outputNamePrefix + "MIP",
                () ->
                        new MarksFlattenedGenerator(
                                outlineWriter,
                                coloredCfgDisplayStack,
                                new IDGetterIter<Overlay>(),
                                rm));
    }

    private static void writeFinalCfg(MarkCollection marks, WriterRouterErrors writer) {
        writer.write("finalCfg", () -> new XStreamGenerator<>(marks, Optional.of("cfg")));
        writer.write(
                "finalCfgBinary", () -> new ObjectOutputStreamGenerator<>(marks, Optional.of("cfg")));
    }

    private static void outputCfgSize(
            MarksWithTotalEnergy marks, WriterRouterErrors writer, Logger logger) {
        writer.write(
                "cfgSize", () -> new StringGenerator(String.format("%d", marks.size())));
        logger.messageLogger().log("Cfg size = " + marks.size());
    }
}
