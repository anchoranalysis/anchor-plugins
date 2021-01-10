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

package org.anchoranalysis.plugin.mpp.segment.bean.marks;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.getter.IdentifyByIteration;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.io.generator.serialized.ObjectOutputStreamGenerator;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.generator.text.StringGenerator;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.ElementSupplier;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithTotalEnergy;
import org.anchoranalysis.mpp.io.marks.ColoredMarksWithDisplayStack;
import org.anchoranalysis.mpp.io.marks.generator.MarksAsUniqueValueGenerator;
import org.anchoranalysis.mpp.io.marks.generator.MarksFlattenedGenerator;
import org.anchoranalysis.mpp.io.marks.generator.MarksGenerator;
import org.anchoranalysis.mpp.mark.ColoredMarks;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.optimization.DualStack;
import org.anchoranalysis.overlay.bean.DrawObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MarksOutputter {

    /** Marks on a background with a thin outline */
    public static final String OUTPUT_OUTLINE_THIN = "outline";

    /** Marks on a background with a thick outline */
    private static final String OUTPUT_OUTLINE_THICK = "outlineThick";

    /** XML serialized version of marks */
    public static final String OUTPUT_MARKS_XML_SERIALIZED = "optimalMarks";

    /** XML serialized version of marks plus an energy breakdown */
    private static final String OUTPUT_MARKS_WITH_ENERGY_XML_SERIALIZED = "optimalMarksWithEnergy";

    private static final Optional<String> MANIFEST_FUNCTION_MARKS = Optional.of("marks");

    public static void outputResults(
            MarksWithEnergyBreakdown marks,
            DualStack dualStack,
            RegionMembershipWithFlags regionMembership,
            Logger logger,
            Outputter outputter)
            throws OperationFailedException {

        ColoredMarks coloredMarks =
                new ColoredMarks(
                        marks.getMarks(),
                        outputter.getSettings().defaultColorIndexFor(20),
                        new IdentifyByIteration<>());

        ColoredMarksWithDisplayStack coloredMarksDisplayStack =
                new ColoredMarksWithDisplayStack(coloredMarks, dualStack.getBackground());

        WriterRouterErrors writer = outputter.writerSelective();

        writeMarks(marks, writer);

        writeOutline(OUTPUT_OUTLINE_THIN, coloredMarksDisplayStack, regionMembership, writer, 1);
        writeOutline(OUTPUT_OUTLINE_THICK, coloredMarksDisplayStack, regionMembership, writer, 3);

        writeMarksAsRaster(coloredMarksDisplayStack, writer, regionMembership);

        writeFinalMarks(marks::getMarks, writer);
        outputMarksSize(marks.getMarksWithTotalEnergy(), writer, logger);
    }

    private static void writeMarks(MarksWithEnergyBreakdown marks, WriterRouterErrors writer) {
        writer.write(
                OUTPUT_MARKS_WITH_ENERGY_XML_SERIALIZED,
                () -> new XStreamGenerator<>(MANIFEST_FUNCTION_MARKS),
                () -> marks);
    }

    private static void writeMarksAsRaster(
            ColoredMarksWithDisplayStack coloredMarksDisplayStack,
            WriterRouterErrors writer,
            RegionMembershipWithFlags regionMembership) {
        writer.write(
                "finalMarksRaster",
                () ->
                        new MarksAsUniqueValueGenerator(
                                coloredMarksDisplayStack.getStack().dimensions(), regionMembership),
                coloredMarksDisplayStack.getMarksColored()::getMarks);
    }

    private static void writeOutline(
            String outputNamePrefix,
            ColoredMarksWithDisplayStack coloredMarksDisplayStack,
            RegionMembershipWithFlags regionMembership,
            WriterRouterErrors writer,
            int outlineWidth) {

        DrawObject outlineWriter = new Outline(outlineWidth);
        writer.write(
                outputNamePrefix,
                () ->
                        new MarksGenerator(
                                outlineWriter, new IdentifyByIteration<>(), regionMembership),
                () -> coloredMarksDisplayStack);
        writer.write(
                outputNamePrefix + "MIP",
                () ->
                        new MarksFlattenedGenerator(
                                outlineWriter, new IdentifyByIteration<>(), regionMembership),
                () -> coloredMarksDisplayStack);
    }

    private static void writeFinalMarks(
            ElementSupplier<MarkCollection> marks, WriterRouterErrors writer) {
        writer.write(
                OUTPUT_MARKS_XML_SERIALIZED,
                () -> new XStreamGenerator<>(MANIFEST_FUNCTION_MARKS),
                marks);
        writer.write(
                "finalMarksBinary",
                () -> new ObjectOutputStreamGenerator<>(MANIFEST_FUNCTION_MARKS),
                marks);
    }

    private static void outputMarksSize(
            MarksWithTotalEnergy marks, WriterRouterErrors writer, Logger logger) {
        writer.write("marksSize", StringGenerator::new, () -> String.format("%d", marks.size()));
        logger.messageLogger().log("Marks size = " + marks.size());
    }
}
