/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.feature;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.io.csv.LabelHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceFactory;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalRerouteErrors;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * Shared-state for an export-features class
 *
 * @param <S> a source-of-rows that is duplicated for each new thread (to prevent any
 *     concurrency issues)
 * @author Owen Feehan
 */
public class SharedStateExportFeatures<S> {

    private static final String MANIFEST_FUNCTION_THUMBNAIL = "thumbnail";

    private static final String THUMBNAIL_FILE_PREFIX = "thumbnail";
    
    private static final String THUMBNAIL_SUBDIRECTORY = "thumbnails";
    
    private final GroupedResultsVectorCollection groupedResults;

    @Getter private final FeatureNameList featureNames;

    private final Supplier<S> rowSource;
    
    // Generates thumbnails, lazily if needed.
    private GeneratorSequenceIncrementalRerouteErrors<Stack> thumbnailGenerator;

    private BoundIOContext context;
    
    /**
     * Constructor
     * 
     * @param labelHeaders headers for the label columns
     * @param featureNames names of each feature in the featuer columns
     * @param rowSource source of rows in the feature-table (called independently for each thread)
     * @param context IO-context including directory in which grouped/thumbnails sub-directorys may be created
     * @throws AnchorIOException
     */
    public SharedStateExportFeatures(
            LabelHeaders labelHeaders,
            FeatureNameList featureNames,
            Supplier<S> rowSource,
            BoundIOContext context)
            throws AnchorIOException {
        this.featureNames = featureNames;
        this.rowSource = rowSource;
        this.context = context;
        this.groupedResults =
                new GroupedResultsVectorCollection(labelHeaders, featureNames, context);
    }

    /**
     * An adder for results
     * <p>
     * Note that in order for the sequence numbers to match the order in the feature table, a
     * thumbnail should be present for ALL results, or for NO results, but it should not be mixed.
     * 
     * @return
     */
    public ExportFeatureResultsAdder addResultsFor() {
        return (StringLabelsForCsvRow labels, ResultsVectorWithThumbnail results) -> {
            // Synchronization is important to preserve identical order in thumbnails and in the
            // exported feature CSV file, as multiple threads will concurrently add results
            synchronized(this) {
                // Add results to grouped-map, and write to CSV file
                groupedResults.addResultsFor(labels, results.getResultsVector());
                
                // Write thumbnail, or empty image
                results.getThumbnail().ifPresent( image->
                    addThumbnail(image, context)
                );
            }
        };
    }

    /**
     * Writes all the results that have been collected as a CSV file
     *
     * @param <T> feature input-type
     * @param featuresAggregate features that can be used for generating additional "aggregated"
     *     exports
     * @param includeGroups iff TRUE a group-column is included in the CSV file and the group
     *     exports occur, otherwise not
     * @param context io-context
     * @throws AnchorIOException
     */
    public void writeGroupedResults(
            Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
            boolean includeGroups,
            BoundIOContext context)
            throws AnchorIOException {
        groupedResults.writeResultsForAllGroups(featuresAggregate, includeGroups, context);
        

    }
    
    public S duplicateForNewThread() {
        return rowSource.get();
    }

    public void closeAnyOpenIO() throws IOException {
        if (thumbnailGenerator!=null) {
            thumbnailGenerator.end();
        }      
        groupedResults.close();
    }
    
    private void addThumbnail(DisplayStack thumbnail, BoundIOContext context) {
        if (thumbnailGenerator == null) {
            thumbnailGenerator = GeneratorSequenceFactory.createIncremental(
               THUMBNAIL_SUBDIRECTORY,
               THUMBNAIL_FILE_PREFIX,
               new StackGenerator(MANIFEST_FUNCTION_THUMBNAIL),
               context
            );
            thumbnailGenerator.start();
        }
        thumbnailGenerator.add(thumbnail.deriveStack(false));
    }
}
