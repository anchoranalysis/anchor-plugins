/* (C)2020 */
package org.anchoranalysis.plugin.image.task.sharedstate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.feature.io.csv.writer.FeatureCSVWriter;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.labeller.ImageLabeller;

/**
 * @author Owen Feehan
 * @param <T> type of init-params for the ImageFilter
 */
public class SharedStateFilteredImageOutput<T> {

    private ImageLabeller<T> filter;
    private BoundOutputManagerRouteErrors baseOutputManager;

    private GroupedMultiplexOutputManagers outputManagers;

    private Optional<FeatureCSVWriter> csvWriter;

    private T filterInitParams;

    private boolean groupIdentifierForCalled = false;

    /**
     * @param baseOutputManager
     * @param filter the filter must not yet have been inited()
     * @throws CreateException
     */
    public SharedStateFilteredImageOutput(
            BoundOutputManagerRouteErrors baseOutputManager, ImageLabeller<T> filter)
            throws CreateException {

        this.baseOutputManager = baseOutputManager;
        this.filter = filter;

        // The CSV file with all names and corresponding groups
        try {
            this.csvWriter =
                    FeatureCSVWriter.create(
                            "group",
                            baseOutputManager,
                            new String[] {"name"},
                            new FeatureNameList("group"));
        } catch (AnchorIOException e) {
            throw new CreateException(e);
        }
    }

    public synchronized void writeRow(String name, String groupIdentifier) {
        List<TypedValue> row = new ArrayList<>();
        row.add(new TypedValue(name));
        row.add(new TypedValue(groupIdentifier));

        csvWriter.ifPresent(writer -> writer.addRow(row));
    }

    public synchronized void close() {
        csvWriter.ifPresent(FeatureCSVWriter::close);
    }

    /** Determines a particular group-identifier for an input */
    public String labelFor(ProvidesStackInput input, BoundIOContext context)
            throws OperationFailedException {

        if (!groupIdentifierForCalled) {
            // We perform the necessary initialization of the filter, the first time
            //  this function is called. We do this so we have a sensible input-path
            //  to give to the filter.
            try {
                initFilterOutputManagers(input.pathForBindingRequired());
            } catch (InitException | AnchorIOException e) {
                throw new OperationFailedException(e);
            }
            groupIdentifierForCalled = true;
        }

        return filter.labelFor(filterInitParams, input, context);
    }

    /** groupIdentifierFor should always called at least once before getOutputManagerFor */
    public BoundOutputManagerRouteErrors getOutputManagerFor(String groupIdentifier) {
        assert (groupIdentifierForCalled);
        return outputManagers.getOutputManagerFor(groupIdentifier);
    }

    public T getFilterInitParams(Path pathForBinding) throws InitException {
        if (filterInitParams == null) {
            filterInitParams = filter.init(pathForBinding);
        }
        return filterInitParams;
    }

    private void initFilterOutputManagers(Path pathForBinding) throws InitException {

        this.outputManagers =
                new GroupedMultiplexOutputManagers(
                        baseOutputManager, filter.allLabels(getFilterInitParams(pathForBinding)));
    }
}
