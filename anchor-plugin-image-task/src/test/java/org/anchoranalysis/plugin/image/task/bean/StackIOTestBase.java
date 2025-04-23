/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean;

import java.nio.file.Path;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base class for tests that read or write image-stacks.
 *
 * @author Owen Feehan
 */
public abstract class StackIOTestBase {

    /** The {@link StackReader} instance used for reading image stacks. */
    protected static final StackReader STACK_READER = BeanInstanceMapFixture.ensureStackReader();

    /**
     * Sets up the necessary instances in the default {@link
     * org.anchoranalysis.bean.xml.BeanInstanceMap}.
     */
    @BeforeAll
    static void setupWriter() {
        BeanInstanceMapFixture.ensureStackWriter(false);
        BeanInstanceMapFixture.ensureImageMetadataReader();
        BeanInstanceMapFixture.ensureInterpolator();
    }

    /** Removes the static writer from the {@link org.anchoranalysis.bean.xml.BeanInstanceMap}. */
    @AfterAll
    static void teardown() {
        // Remove the static writer, as we want the global state to stay neutral
        // for future tests (especially as this test uses TIFFs).
        BeanInstanceMapFixture.removeStackWriter();
    }

    /**
     * The directory where the output is written to.
     *
     * <p>This is automatically created and managed by JUnit using the {@link TempDir} annotation.
     */
    @TempDir protected Path directory; // NOSONAR
}
