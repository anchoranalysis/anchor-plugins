package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base class for tests that read or write image-stacks.
 *
 * @author Owen Feehan
 */
abstract class StackIOTestBase {

    // START: Ensure needed instances exist in the default BeanInstanceMap
    protected static final StackReader STACK_READER = BeanInstanceMapFixture.ensureStackReader();

    static {
        BeanInstanceMapFixture.ensureStackWriter(false);
        BeanInstanceMapFixture.ensureImageMetadataReader();
        BeanInstanceMapFixture.ensureInterpolator();
    }
    // END: Ensure needed instances exist in the default BeanInstanceMap

    /** Where the output is written to. */
    @TempDir Path directory;
}
