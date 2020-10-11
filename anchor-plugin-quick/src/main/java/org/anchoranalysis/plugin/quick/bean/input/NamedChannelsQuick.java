/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.input;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.io.bean.channel.map.ChannelEntry;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.input.NamedChannelsInputPart;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.path.derive.DerivePath;
import org.anchoranalysis.io.bean.path.derive.Replace;
import org.anchoranalysis.io.exception.AnchorIOException;
import org.anchoranalysis.plugin.io.bean.input.channel.NamedChannelsAppend;
import org.anchoranalysis.plugin.quick.bean.input.filepathappend.AppendStack;

/**
 * A particular type of NamedChannels which allows easier input of data, making several assumptions.
 *
 * <p>This is a convenient helper class to avoid a more complicated structure.
 *
 * <p>Note that {@code regexAdjacent} applies for adjacent-channels and {@code regex} applies for
 * appended channels.
 */
public class NamedChannelsQuick extends QuickBase<NamedChannelsInputPart> {

    // START BEAN PROPERTIES

    /**
     * This needs to be set if there is at least one adjacentChannel.
     *
     * <p>This should be a regex (with a single group that is replaced) that is searched for among
     * the input-paths. This only needs to be set if at least one adjacentChannel is.
     * specified
     */
    @BeanField @AllowEmpty @Getter @Setter private String regexAdjacent = "";

    /** The name of the channel provided by the rasters in file Provider */
    @BeanField @Getter @Setter private String mainChannelName;

    /** Index of the mainChannel */
    @BeanField @Getter @Setter private int mainChannelIndex = 0;

    /** Additional channels other than the main one, which are located in the main raster file */
    @BeanField @Getter @Setter private List<ChannelEntry> additionalChannels = new ArrayList<>();

    /** Channels that are located in a separate raster file adjacent to the main raster file */
    @BeanField @Getter @Setter private List<AdjacentFile> adjacentChannels = new ArrayList<>();

    /**
     * Channels that are located in a separate raster file somewhere else in the project's structure
     */
    @BeanField @Getter @Setter private List<AppendStack> appendChannels = new ArrayList<>();

    /** The raster-reader to use for opening any adjacent-channels */
    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReaderAdjacent;
    // END BEAN PROPERTIES

    private InputManager<NamedChannelsInputPart> append;

    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;

        checkChannels(adjacentChannels, regexAdjacent, "adjacentChannel");
        checkChannels(appendChannels, getRegex(), "appendChannel");
    }

    /**
     * Checks that if a list has an item, then a string must be non-empty
     *
     * @param list
     * @param mustBeNonEmpty
     * @param errorText
     * @throws BeanMisconfiguredException
     */
    private static void checkChannels(List<?> list, String mustBeNonEmpty, String errorText)
            throws BeanMisconfiguredException {
        if (!list.isEmpty() && mustBeNonEmpty.isEmpty()) {
            throw new BeanMisconfiguredException(
                    String.format(
                            "If an %s is specified, regex must also be specified", errorText));
        }
    }

    @Override
    public List<NamedChannelsInputPart> inputs(InputManagerParams params) throws AnchorIOException {
        createAppendedChannelsIfNecessary();
        return append.inputs(params);
    }

    private void createAppendedChannelsIfNecessary() throws AnchorIOException {
        if (this.append == null) {
            try {
                this.append = createAppendedChannels();
                append.checkMisconfigured(defaultInstances);
            } catch (BeanMisconfiguredException e) {
                throw new AnchorIOException("defaultInstances bean is misconfigured", e);
            }
        }
    }

    private InputManager<NamedChannelsInputPart> createAppendedChannels()
            throws BeanMisconfiguredException {

        InputManager<NamedChannelsInputPart> channels =
                NamedChannelsCreator.create(
                        fileInputManager(),
                        mainChannelName,
                        mainChannelIndex,
                        additionalChannels,
                        getStackReader());

        channels =
                appendChannels(channels, pathsAdjacent(), stackReaderAdjacent);

        channels =
                appendChannels(channels, pathsAppend(), getStackReaderAppend());

        return channels;
    }

    private static NamedChannelsAppend appendChannels(
            InputManager<NamedChannelsInputPart> input,
            List<NamedBean<DerivePath>> derivePaths,
            StackReader stackReader) {
        NamedChannelsAppend append = new NamedChannelsAppend();
        append.setIgnoreFileNotFoundAppend(false);
        append.setForceEagerEvaluation(false);
        append.setInput(input);
        append.setListAppend(derivePaths);
        append.setStackReader(stackReader);
        return append;
    }

    private List<NamedBean<DerivePath>> pathsAdjacent() {
        return FunctionalList.mapToList(adjacentChannels, this::convertAdjacentFile);
    }

    private List<NamedBean<DerivePath>> pathsAppend()
            throws BeanMisconfiguredException {

        List<NamedBean<DerivePath>> out = new ArrayList<>();

        for (AppendStack stack : appendChannels) {
            try {
                out.add(convertAppendStack(stack));
            } catch (BeanMisconfiguredException e) {
                throw new BeanMisconfiguredException(
                        String.format(
                                "Cannot create file-path-generator for %s and regex %s",
                                stack.getName(), getRegex()),
                        e);
            }
        }

        return out;
    }

    private NamedBean<DerivePath> convertAdjacentFile(AdjacentFile file) {
        Replace generator = new Replace();
        generator.setRegex(regexAdjacent);
        generator.setReplacement(file.getReplacement());
        return new NamedBean<>(file.getName(), generator);
    }

    private NamedBean<DerivePath> convertAppendStack(AppendStack stack)
            throws BeanMisconfiguredException {
        return stack.createPathDeriver(getRootName(), getRegex());
    }
}
