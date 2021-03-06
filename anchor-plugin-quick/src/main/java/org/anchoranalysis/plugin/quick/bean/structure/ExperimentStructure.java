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

package org.anchoranalysis.plugin.quick.bean.structure;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixerAvoidResolve;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerException;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.PathRegEx;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.Rooted;

/**
 * A {@link PathPrefixer} that combines a prefix with an {@code experimentType}.
 *
 * <p>A convenience method for commonly used prefixer settings when the output occurs in an {@code
 * experiment/$1/} filesystem structure where {@code $1} is the {@code experimentType}.
 */
public class ExperimentStructure extends PathPrefixer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RootedExperiment experiment;

    @BeanField @Getter @Setter private RegEx regEx;

    @BeanField @Getter @Setter private String prefix;
    // END BEAN PROPERTIES

    private PathPrefixer delegate;

    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    public DirectoryWithPrefix outFilePrefix(
            NamedPath path, String experimentIdentifier, PathPrefixerContext context)
            throws PathPrefixerException {

        createDelegateIfNeeded();

        return delegate.outFilePrefix(path, experimentIdentifier, context);
    }

    @Override
    public DirectoryWithPrefix rootDirectoryPrefix(
            String experimentIdentifier, PathPrefixerContext context) throws PathPrefixerException {

        createDelegateIfNeeded();

        return delegate.rootDirectoryPrefix(experimentIdentifier, context);
    }

    private void createDelegateIfNeeded() throws PathPrefixerException {

        if (delegate == null) {
            delegate = new Rooted(createResolver(), experiment.getRootName());

            try {
                delegate.checkMisconfigured(defaultInstances);
            } catch (BeanMisconfiguredException e) {
                throw new PathPrefixerException(e);
            }
        }
    }

    private PathPrefixerAvoidResolve createResolver() {
        PathRegEx resolver = new PathRegEx();
        resolver.setPrefix(prefix + experiment.getExperimentType());
        resolver.setRegEx(regEx);
        return resolver;
    }
}
