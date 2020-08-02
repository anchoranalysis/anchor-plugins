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

package org.anchoranalysis.plugin.mpp.bean.define;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderReference;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderReference;
import ch.ethz.biol.cell.imageprocessing.stack.provider.ColoredObjectsOnStack;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderChnlProvider;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderOutlineFromCfg;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderOutlineRGB;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderWithBackground;
import ch.ethz.biol.cell.mpp.cfg.provider.CfgProviderReference;
import lombok.Getter;
import lombok.Setter;
import java.util.function.Function;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.define.adder.DefineAdderBean;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProviderReference;
import org.anchoranalysis.plugin.image.bean.object.provider.Reference;

/**
 * Adds a visualization for all binary-masks and object-collections that are added using a
 * particular background
 *
 * @author Owen Feehan
 */
public class VisualizeOnBackground extends DefineAdderBean {

    private static final String SUFFIX = ".visualize";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineAdderBean add;

    @BeanField @Getter @Setter private String backgroundID;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    // If TRUE, backgroundID refers to a Stack, otherwise it's a Chnl
    @BeanField @Getter @Setter private boolean stackBackground = false;
    // END BEAN PROPERTIES

    @Override
    public void addTo(Define define) throws BeanXmlException {

        // We first add, so we can see what's been added
        Define def = fromDelegate();

        try {
            // We add all the existing definitions
            define.addAll(def);

            // Now we add visualizations for the BinaryChnlProvider and object-collection providers
            addVisualizationFor(
                    def, define, MaskProvider.class, this::visualizationBinaryMask);
            addVisualizationFor(
                    def, define, ObjectCollectionProvider.class, this::visualizationObjects);
            addVisualizationFor(def, define, CfgProvider.class, this::visualizationCfg);

        } catch (OperationFailedException e) {
            throw new BeanXmlException(e);
        }
    }

    private Define fromDelegate() throws BeanXmlException {
        Define def = new Define();
        add.addTo(def);
        return def;
    }

    /**
     * Adds visualization for all items of type listType
     *
     * @param in definitions we search for items of type listType
     * @param out where the newly-created visualizations are added to
     * @param listType a particular class-type that filters which NamedBeans provide visualizations
     * @param createVisualization creates the visualization given the ID of the provider it is based
     *     on
     * @throws OperationFailedException
     */
    private void addVisualizationFor(
            Define in,
            Define out,
            Class<?> listType,
            Function<String, StackProvider> createVisualization)
            throws OperationFailedException {

        for (NamedBean<AnchorBean<?>> nb : in.getList(listType)) {

            NamedBean<StackProvider> nbViz = visualizationBean(createVisualization, nb.getName());
            out.add(nbViz);
        }
    }

    private NamedBean<StackProvider> visualizationBean(
            Function<String, StackProvider> createVisualization, String inputName) {
        return new NamedBean<>(inputName + SUFFIX, createVisualization.apply(inputName));
    }

    private StackProvider visualizationBinaryMask(String binaryChnlProviderID) {
        StackProviderOutlineRGB provider = new StackProviderOutlineRGB();
        addBackgroundProvider(provider);
        provider.setMask(new BinaryChnlProviderReference(binaryChnlProviderID));
        return provider;
    }

    private StackProvider visualizationObjects(String objectsProviderID) {
        ColoredObjectsOnStack provider = new ColoredObjectsOnStack();
        provider.setOutline(true);
        addBackgroundProvider(provider);
        provider.setObjects(new Reference(objectsProviderID));
        provider.setOutlineWidth(outlineWidth);
        return provider;
    }

    private StackProvider visualizationCfg(String cfgProviderID) {
        StackProviderOutlineFromCfg provider = new StackProviderOutlineFromCfg();
        provider.setBackgroundProvider(backgroundStack());
        provider.setCfgProvider(new CfgProviderReference(cfgProviderID));
        provider.setOutlineWidth(outlineWidth);
        return provider;
    }

    private void addBackgroundProvider(StackProviderWithBackground provider) {
        if (stackBackground) {
            provider.setStackBackground(backgroundStack());
        } else {
            provider.setChnlBackground(backgroundChnl());
        }
    }

    private StackProvider backgroundStack() {
        if (stackBackground) {
            return new StackProviderReference(backgroundID);
        } else {
            return new StackProviderChnlProvider(backgroundChnl());
        }
    }

    private ChannelProvider backgroundChnl() {
        return new ChnlProviderReference(backgroundID);
    }
}
