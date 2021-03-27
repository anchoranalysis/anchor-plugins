/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.file.path.prefixer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixerAvoidResolve;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.OutputPrefixerSettings;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerException;

/**
 * Multiplexes between two {@link PathPrefixer}s depending on whether an <i>incrementing number
 * sequence was output</i> was requested.
 *
 * @author Owen Feehan
 */
public class IfIncrementingNumberRequested extends PathPrefixerAvoidResolve {

    // START BEAN PROPERTIES
    /** Called if the requested condition <b>is</b> true. */
    @Getter @Setter @BeanField PathPrefixerAvoidResolve prefixerIf;

    /** Called if the requested condition <b>is not</b> true. */
    @Getter @Setter @BeanField PathPrefixerAvoidResolve prefixerElse;
    // END BEAN PROPERTIES

    @Override
    public DirectoryWithPrefix outFilePrefixFromPath(
            NamedPath path, Path root, PathPrefixerContext context) throws PathPrefixerException {
        return multiplex(context.getPrefixer()).outFilePrefixFromPath(path, root, context);
    }

    private PathPrefixerAvoidResolve multiplex(OutputPrefixerSettings settings) {
        if (settings.isOutputIncrementingNumberSequence()) {
            return prefixerIf;
        } else {
            return prefixerElse;
        }
    }
}
