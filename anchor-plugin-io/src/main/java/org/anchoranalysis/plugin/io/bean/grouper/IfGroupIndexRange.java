package org.anchoranalysis.plugin.io.bean.grouper;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.range.IndexRangeNegative;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.grouper.Grouper;
import org.anchoranalysis.io.input.bean.grouper.WithoutGrouping;
import org.anchoranalysis.io.input.grouper.InputGrouper;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.input.path.ExtractPathElementRange;

/**
 * When a {@link IndexRangNegative} is passed as an argument, an {@link InputGrouper} is constructed
 * that extracts elements from a {@link Path}.
 *
 * <p>See {@link ExtractPathElementRange#extract} for how this occurs.
 *
 * <p>Otherwise, {@code group} is called to create a {@code Optional<InputGrouper>}.
 *
 * @author Owen Feehan
 */
public class IfGroupIndexRange extends Grouper {

    // START BEAN PROPERTIES
    /** Fallback to use when no group-index-range is specified. */
    @BeanField @Getter @Setter private Grouper group = new WithoutGrouping();
    // END BEAN PROPERTIES

    @Override
    public Optional<InputGrouper> createInputGrouper(Optional<IndexRangeNegative> groupIndexRange) {
        if (groupIndexRange.isPresent()) {
            return Optional.of(identifier -> extractSubrange(identifier, groupIndexRange.get()));
        } else {
            return group.createInputGrouper(groupIndexRange);
        }
    }

    /** Extracts a range of elements from {@code identifier}. */
    private static String extractSubrange(Path identifier, IndexRangeNegative groupIndexRange)
            throws DerivePathException {
        Path extractedPath = ExtractPathElementRange.extract(identifier, groupIndexRange);
        return FilePathToUnixStyleConverter.toStringUnixStyle(extractedPath);
    }
}
