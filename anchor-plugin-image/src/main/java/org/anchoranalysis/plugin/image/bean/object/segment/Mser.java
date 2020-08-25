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

package org.anchoranalysis.plugin.image.bean.object.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.image.object.factory.SingleObjectFromPointsFactory;
import org.anchoranalysis.image.seed.SeedCollection;

/**
 * Applies the MSER algorithm from imglib2
 *
 * @author Owen Feehan
 */
public class Mser extends SegmentChannelIntoObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private long minSize = 100;

    @BeanField @Getter @Setter private long maxSize = 10000000;

    @BeanField @Getter @Setter private double maxVar = 0.5;

    @BeanField @Getter @Setter private double minDiversity = 0;

    @BeanField @Getter @Setter private double delta = 1;
    // END BEAN PROPERTIES

    @SuppressWarnings("unchecked")
    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> objectMask, Optional<SeedCollection> seeds)
            throws SegmentationFailedException {

        checkUnsupportedObjectMask(objectMask);
        checkUnsupportedSeeds(seeds);

        @SuppressWarnings("rawtypes")
        Img img = ImgLib2Wrap.wrap(channel.voxels());

        final MserTree<?> treeDarkToBright =
                MserTree.buildMserTree(img, delta, minSize, maxSize, maxVar, minDiversity, true);

        try {
            return convertOutputToObjects(treeDarkToBright);
        } catch (CreateException exc) {
            throw new SegmentationFailedException(exc);
        }
    }

    private <T extends Type<T>> ObjectCollection convertOutputToObjects(MserTree<T> tree)
            throws CreateException {
        return ObjectCollectionFactory.mapFrom(
                tree, mser -> SingleObjectFromPointsFactory.create(convertPixelPoints(mser)));
    }

    private static List<Point3i> convertPixelPoints(
            net.imglib2.algorithm.componenttree.mser.Mser<?> mser) {
        List<Point3i> out = new ArrayList<>();
        for (Localizable localizable : mser) {
            out.add(new Point3i(localizable.getIntPosition(0), localizable.getIntPosition(1), 0));
        }
        return out;
    }
}
