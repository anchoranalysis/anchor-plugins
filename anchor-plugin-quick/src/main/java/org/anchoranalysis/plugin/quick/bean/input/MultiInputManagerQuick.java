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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.io.bean.channel.ChannelEntry;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.files.FileInput;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.input.stack.Stacks;
import org.anchoranalysis.plugin.quick.bean.input.filepathappend.FilePathBaseAppendToManager;

/**
 * A multi-input manager that makes various assumptions, so it can be more quickly specified.
 *
 * @author Owen Feehan
 */
public class MultiInputManagerQuick extends QuickBase<MultiInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String inputName;

    /*** Additional entities that are appended to the multi-input */
    @BeanField @Getter @Setter
    private List<FilePathBaseAppendToManager> listAppend = new ArrayList<>();

    /**
     * Additional channels other than the main one, which are located in the main raster file
     *
     * <p>If this list has at least one, then we treat the main raster file not as a stack, but
     * break it into separate channels that are each presented as a separate stack to the MultiInput
     */
    @BeanField @Getter @Setter private List<ChannelEntry> additionalChannels = new ArrayList<>();

    /**
     * If true, a raster-stack is treated as a single-channel, even if multiple exist (and no
     * additionalChannel is set)
     */
    @BeanField @Getter @Setter private boolean stackAsChannel = false;

    /**
     * If either stackAsChannel==true or we have specified additionalChannels this indicated which
     * channel to use from the stack
     */
    @BeanField @Getter @Setter private int channelIndex = 0;
    // END BEAN PROPERTIES

    private MultiInputManager inputManager;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        this.inputManager = createMulti();
        inputManager.checkMisconfigured(defaultInstances);

        if (!additionalChannels.isEmpty() && getRegex() == null) {
            throw new BeanMisconfiguredException(
                    "If there is at least one additionalChannel then regex must be set");
        }
    }

    @Override
    public List<MultiInput> inputs(InputManagerParams params) throws InputReadFailedException {
        return inputManager.inputs(params);
    }

    private MultiInputManager createMulti() throws BeanMisconfiguredException {
        MultiInputManager input = new MultiInputManager();
        input.setInputName(inputName);
        input.setInput(createStacks());
        input.setStackReader(getStackReaderAppend());

        // Add all the various types of items that can be appended
        for (FilePathBaseAppendToManager append : listAppend) {
            append.addToManager(input, getRootName(), getRegex());
        }

        return input;
    }

    private InputManager<? extends ProvidesStackInput> createStacks()
            throws BeanMisconfiguredException {
        InputManager<FileInput> files = fileInputManager();

        if (stackAsChannel || !additionalChannels.isEmpty()) {
            // Then we treat the main raster as comprising of multiple independent channels
            //  and each are presented separately to the MultiInput as stacks
            //
            // Channel 0 always takes the inputName
            // The other channels are defined by the contents of the ImgChannelMapEntry
            return NamedChannelsCreator.create(
                    files, inputName, channelIndex, additionalChannels, getStackReader());

        } else {
            // Normal mode, where we simply wrap the FileProvider in a Stacks
            Stacks stacks = new Stacks(files);
            stacks.setStackReader(getStackReader());
            return stacks;
        }
    }
}
