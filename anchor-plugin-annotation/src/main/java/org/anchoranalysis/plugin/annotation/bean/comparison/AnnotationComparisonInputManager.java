/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationComparisonInputManager<T extends InputFromManager>
        extends InputManager<AnnotationComparisonInput<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<T> input;

    @BeanField @Getter @Setter private String nameLeft;

    @BeanField @Getter @Setter private String nameRight;

    @BeanField @Getter @Setter private Comparer comparerLeft;

    @BeanField @Getter @Setter private Comparer comparerRight;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;
    // END BEAN PROPERTIES

    @Override
    public List<AnnotationComparisonInput<T>> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), 2)) {

            Iterator<T> itr = input.inputObjects(params).iterator();

            prm.incrWorker();

            List<T> tempList = new ArrayList<>();
            while (itr.hasNext()) {
                tempList.add(itr.next());
            }

            List<AnnotationComparisonInput<T>> outList;
            try {
                outList =
                        createListInputWithAnnotationPath(
                                tempList, new ProgressReporterOneOfMany(prm));
            } catch (CreateException e) {
                throw new AnchorIOException("Cannot create inputs (with annotation path)", e);
            }
            prm.incrWorker();

            return outList;
        }
    }

    private List<AnnotationComparisonInput<T>> createListInputWithAnnotationPath(
            List<T> listInputObjects, ProgressReporter progressReporter) throws CreateException {
        return FunctionalProgress.mapList(
                listInputObjects,
                progressReporter,
                inputObject ->
                        new AnnotationComparisonInput<>(
                                inputObject,
                                Pair.of(comparerLeft, comparerRight),
                                Pair.of(nameLeft, nameRight),
                                rasterReader));
    }
}
