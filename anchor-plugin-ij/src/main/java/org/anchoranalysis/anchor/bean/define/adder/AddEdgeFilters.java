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

package org.anchoranalysis.anchor.bean.define.adder;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderBlur;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderEdgeFilter;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderGradientSingleDimension;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderMedianFilterIJ2D;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderReference;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.define.adder.DefineAdderWithPrefixBean;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.plugin.image.bean.blur.BlurGaussian3D;
import org.anchoranalysis.plugin.image.bean.blur.BlurStrategy;

public class AddEdgeFilters extends DefineAdderWithPrefixBean {

    private static final String NAME_MEDIAN = "Median";
    private static final String NAME_GAUSSIAN = "Blurred";

    // START BEAN PROPERTIES
    /** The ID of the chnl that provides the input to the filter */
    @BeanField @Getter @Setter private String chnlID;

    @BeanField @Positive @Getter @Setter private double medianRadiusMeters = 0;

    @BeanField @Positive @Getter @Setter private double gaussianSigmaMeters = 0;

    /** If TRUE, the median filter is included */
    @BeanField @Getter @Setter private boolean median = true;

    /** If TRUE, the Gaussian filter is included */
    @BeanField @Getter @Setter private boolean gaussian = true;
    // END BEAN PROPERTIES

    @Override
    public void addTo(Define define) throws BeanXmlException {

        if (median) {
            addFilterType(define, NAME_MEDIAN, createMedian());
        }

        if (gaussian) {
            addFilterType(define, NAME_GAUSSIAN, createGaussian());
        }
    }

    private void addFilterType(Define define, String filterName, AnchorBean<?> filterProvider)
            throws BeanXmlException {
        addWithName(define, filterName, filterProvider);
        new GradientsForFilter(filterName).addTo(define);
    }

    private class GradientsForFilter {

        private String filterName;

        public GradientsForFilter(String filterName) {
            super();
            this.filterName = filterName;
        }

        public void addTo(Define define) throws BeanXmlException {

            addForFilter(define, "_Gradient_Magnitude", edgeFilter(filterName));

            addForFilter(define, "_Gradient_X", gradientSingleDimension(0));

            addForFilter(define, "_Gradient_Y", gradientSingleDimension(1));

            addForFilter(
                    define,
                    "_Gradient_Second_Magnitude",
                    edgeFilter(filterName + "_Gradient_Magnitude"));
        }

        private void addForFilter(Define define, String suffix, AnchorBean<?> item)
                throws BeanXmlException {
            addWithName(define, filterName + suffix, item);
        }

        private ChannelProvider edgeFilter(String unresolvedSrcName) {
            ChnlProviderEdgeFilter provider = new ChnlProviderEdgeFilter();
            provider.setOutputShort(true);
            provider.setChnl(createDup(resolveName(unresolvedSrcName)));
            return provider;
        }

        private ChannelProvider gradientSingleDimension(int axis) {
            ChnlProviderGradientSingleDimension provider =
                    new ChnlProviderGradientSingleDimension();
            provider.setOutputShort(true);
            provider.setAddSum(32768);
            provider.setAxis(axis);
            provider.setChnl(createDup(resolveName(filterName)));
            return provider;
        }
    }

    private ChannelProvider createMedian() {
        ChnlProviderMedianFilterIJ2D provider = new ChnlProviderMedianFilterIJ2D();
        provider.setRadius((int) Math.round(medianRadiusMeters));
        provider.setRadiusInMeters(true);
        provider.setChnl(createDup(chnlID));
        return provider;
    }

    private ChannelProvider createGaussian() {
        ChnlProviderBlur provider = new ChnlProviderBlur();
        provider.setStrategy(createBlurStrategy());
        provider.setChnl(createDup(chnlID));
        return provider;
    }

    private BlurStrategy createBlurStrategy() {
        BlurGaussian3D blurStrategy = new BlurGaussian3D();
        blurStrategy.setSigma(gaussianSigmaMeters);
        blurStrategy.setSigmaInMeters(true);
        return blurStrategy;
    }

    private ChannelProvider createDup(String srcID) {
        ChnlProviderReference provider = new ChnlProviderReference();
        provider.setId(srcID);
        provider.setDuplicate(true);
        return provider;
    }
}
