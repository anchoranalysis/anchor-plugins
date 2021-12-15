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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.awt.Color;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Create a thumbnail by drawing an outline of an object at a particular-scale, and placing it
 * centered in a window of a certain size.
 *
 * <p>Preserves the relative-size between objects (i.e. they are all reduced by the same
 * scale-factor) in the same batch.
 *
 * <p>If it's a z-stack, a maximum intensity projection is first applied.
 *
 * <p>All thumbnails are created with identical size. An error will occur if the background is ever
 * smaller than the thumbnail size.
 *
 * <p>If no specific background-channel is set with {@link #setBackgroundChannelIndex} then the
 * following scheme applies:
 *
 * <ul>
 *   <li>If {@code backgroundSource} has exactly zero channels, an empty zero-valued monochrome
 *       background is used (unsigned 8 bit).
 *   <li>If {@code backgroundSource} has exactly one channel, it's used as a monochrome background
 *   <li>If {@code backgroundSource} has exactly three channels, it's used as a RGB background
 *   <li>If {@code backgroundSource} has any other number of channels, the first channel is used as
 *       a background.
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class OutlinePreserveRelativeSize extends ThumbnailFromObjects {

    // START BEAN PROPERTIES
    /** Size of all created thumbnails */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);

    /**
     * Uses only this channel (identified by an index in the stack) as the background, -1 disables.
     */
    @BeanField @Getter @Setter private int backgroundChannelIndex = -1;

    /** Interpolator used when scaling the background */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;

    /**
     * The width of the outline. By default, it's 3 as it's nice to have a strongly easily-visible
     * emphasis on where the object is in a thumbnail.
     */
    @BeanField @Getter @Setter private int outlineWidth = 3;

    /**
     * Optionally outline the other (unselected for the thumbnail) objects in this particular color.
     * If not set, these objects aren't outlined at all.
     */
    @BeanField @OptionalBean @Getter @Setter private RGBColorBean colorUnselectedObjects;

    /**
     * Whether objects may overlap or not when unscaled.
     *
     * <p>If they overlap, we scale them individually, not taking account of their neighbours.
     *
     * <p>If they may not overlap, we scale them collectively, as visually this gives tighter
     * borders between neighbouring objects.
     */
    @BeanField @OptionalBean @Getter @Setter private boolean overlappingObjects = true;
    // END BEAN PROPERTIES

    /**
     * Alternative constructor that switches on the coloring of unselected objects by default.
     *
     * <p>They are colored in blue.
     *
     * @param interpolator how to resize an image.
     * @return a newly created instance of {@link OutlinePreserveRelativeSize} that colors
     *     unselected objects, but otherwise uses defaults.
     */
    public static OutlinePreserveRelativeSize createToColorUnselectedObjects(
            Interpolator interpolator) {
        OutlinePreserveRelativeSize out = new OutlinePreserveRelativeSize();
        out.colorUnselectedObjects = new RGBColorBean(Color.BLUE);
        out.interpolator = interpolator;
        return out;
    }

    @Override
    public ThumbnailBatch<ObjectCollection> start(
            ObjectCollection objects,
            StreamableCollection<BoundingBox> boundingBoxes,
            Optional<Stack> backgroundSource)
            throws OperationFailedException {

        if (!objects.isEmpty()) {
            VoxelsResizer interpolatorBackground = interpolator.voxelsResizer();

            // Determine what to scale the objects and any background by
            FlattenAndScaler scaler =
                    new FlattenAndScaler(
                            boundingBoxes,
                            objects,
                            overlappingObjects,
                            interpolatorBackground,
                            size.asExtent(),
                            backgroundSource,
                            backgroundChannelIndex);

            return new ThumbnailBatchOutline(
                    scaler, objects, size.asExtent(), outlineWidth, colorForUnselectedObjects());
        } else {
            return objectForBatch -> {
                throw new CreateException("No objects are expected in this batch");
            };
        }
    }

    private Optional<Color> colorForUnselectedObjects() {
        return Optional.ofNullable(colorUnselectedObjects).map(RGBColorBean::toAWTColor);
    }
}
