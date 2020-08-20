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

package org.anchoranalysis.plugin.mpp.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.conic.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.GuessDimensions;

/**
 * Creates object-masks from a {@link Cfg} where (a particular region of) each mark creates an
 * object.
 *
 * @author Owen Feehan
 */
public class FromCfg extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CfgProvider cfgProvider;

    @BeanField @Getter @Setter private int regionID = 0;

    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private DimensionsProvider dimensions = new GuessDimensions();
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        Cfg cfg = cfgProvider.create();

        Dimensions dims = dimensions.create();

        return cfg.deriveObjects(
                        dims,
                        regionMap.membershipWithFlagsForIndex(regionID)
                        )
                .withoutProperties();
    }
}
