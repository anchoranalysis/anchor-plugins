/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.define.DefineAddException;
import org.anchoranalysis.bean.define.adder.DefineAdderWithPrefixBean;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.plugin.image.bean.blur.BlurGaussian3D;
import org.anchoranalysis.plugin.image.bean.blur.BlurStrategy;
import org.anchoranalysis.plugin.image.bean.channel.provider.Reference;
import org.anchoranalysis.plugin.image.bean.channel.provider.gradient.GradientForAxis;
import org.anchoranalysis.plugin.image.bean.channel.provider.gradient.Sobel;
import org.anchoranalysis.plugin.image.bean.channel.provider.intensity.Blur;
import org.anchoranalysis.plugin.imagej.bean.channel.provider.filter.rank.MedianFilter2D;

/** Adds edge filters and their gradients to a {@link Define} object. */
public class AddEdgeFilters extends DefineAdderWithPrefixBean {

    private static final String NAME_MEDIAN = "Median";
    private static final String NAME_GAUSSIAN = "Blurred";

    // START BEAN PROPERTIES
    /** The ID of the channel that provides the input to the filter. */
    @BeanField @Getter @Setter private String channelID;

    /** The radius of the median filter in meters. */
    @BeanField @Positive @Getter @Setter private double medianRadiusMeters = 0;

    /** The sigma value for the Gaussian filter in meters. */
    @BeanField @Positive @Getter @Setter private double gaussianSigmaMeters = 0;

    /** If true, the median filter is included. */
    @BeanField @Getter @Setter private boolean median = true;

    /** If true, the Gaussian filter is included. */
    @BeanField @Getter @Setter private boolean gaussian = true;

    // END BEAN PROPERTIES

    @Override
    public void addTo(Define define) throws DefineAddException {

        if (median) {
            addFilterType(define, NAME_MEDIAN, createMedian());
        }

        if (gaussian) {
            addFilterType(define, NAME_GAUSSIAN, createGaussian());
        }
    }

    /**
     * Adds a filter type and its gradients to the {@link Define} object.
     *
     * @param define the {@link Define} object to add to
     * @param filterName the name of the filter
     * @param filterProvider the {@link AnchorBean} that provides the filter
     * @throws DefineAddException if there's an error adding the filter
     */
    private void addFilterType(Define define, String filterName, AnchorBean<?> filterProvider)
            throws DefineAddException {
        addWithName(define, filterName, filterProvider);
        new GradientsForFilter(filterName).addTo(define);
    }

    /** Inner class for adding gradients for a specific filter. */
    @AllArgsConstructor
    private class GradientsForFilter {

        private String filterName;

        /**
         * Adds gradient filters to the {@link Define} object.
         *
         * @param define the {@link Define} object to add to
         * @throws DefineAddException if there's an error adding the gradients
         */
        public void addTo(Define define) throws DefineAddException {

            addForFilter(define, "_Gradient_Magnitude", edgeFilter(filterName));

            addForFilter(define, "_Gradient_X", gradientSingleDimension("x"));

            addForFilter(define, "_Gradient_Y", gradientSingleDimension("y"));

            addForFilter(
                    define,
                    "_Gradient_Second_Magnitude",
                    edgeFilter(filterName + "_Gradient_Magnitude"));
        }

        private void addForFilter(Define define, String suffix, AnchorBean<?> item)
                throws DefineAddException {
            addWithName(define, filterName + suffix, item);
        }

        private ChannelProvider edgeFilter(String unresolvedSrcName) {
            Sobel provider = new Sobel();
            provider.setOutputShort(true);
            provider.setChannel(createDup(resolveName(unresolvedSrcName)));
            return provider;
        }

        private ChannelProvider gradientSingleDimension(String axis) {
            GradientForAxis provider = new GradientForAxis();
            provider.setOutputShort(true);
            provider.setAddSum(32768);
            provider.setAxis(axis);
            provider.setChannel(createDup(resolveName(filterName)));
            return provider;
        }
    }

    /**
     * Creates a median filter {@link ChannelProvider}.
     *
     * @return a {@link ChannelProvider} for the median filter
     */
    private ChannelProvider createMedian() {
        MedianFilter2D provider = new MedianFilter2D();
        provider.setRadius((int) Math.round(medianRadiusMeters));
        provider.setRadiusInMeters(true);
        provider.setChannel(createDup(channelID));
        return provider;
    }

    /**
     * Creates a Gaussian filter {@link ChannelProvider}.
     *
     * @return a {@link ChannelProvider} for the Gaussian filter
     */
    private ChannelProvider createGaussian() {
        Blur provider = new Blur();
        provider.setStrategy(createBlurStrategy());
        provider.setChannel(createDup(channelID));
        return provider;
    }

    /**
     * Creates a {@link BlurStrategy} for the Gaussian filter.
     *
     * @return a {@link BlurStrategy} for the Gaussian filter
     */
    private BlurStrategy createBlurStrategy() {
        BlurGaussian3D blurStrategy = new BlurGaussian3D();
        blurStrategy.setSigma(gaussianSigmaMeters);
        blurStrategy.setSigmaInMeters(true);
        return blurStrategy;
    }

    /**
     * Creates a {@link ChannelProvider} that duplicates a channel.
     *
     * @param srcID the ID of the source channel
     * @return a {@link ChannelProvider} that duplicates the specified channel
     */
    private ChannelProvider createDup(String srcID) {
        Reference provider = new Reference();
        provider.setId(srcID);
        provider.setDuplicate(true);
        return provider;
    }
}
