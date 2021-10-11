/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.annotation.io.comparer.StatisticsToExport;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.annotation.counter.ImageCounter;
import org.anchoranalysis.plugin.annotation.counter.ImageCounterList;
import org.anchoranalysis.plugin.annotation.counter.ImageCounterWithStatistics;

public class ComparisonSharedState<T extends Assignment<ObjectMask>> {

    @Getter private CSVAssignment assignmentCSV;
    private ImageCounterWithStatistics<T> groupAll;
    private HashMap<String, ImageCounterWithStatistics<T>>[] groupsMap;
    private int numberLevelsGrouping;
    private Function<String, ImageCounterWithStatistics<T>> funcCreateGroup;

    @SuppressWarnings("unchecked")
    public ComparisonSharedState(
            CSVAssignment assignmentCSV,
            int numberLevelsGrouping,
            Function<String, ImageCounterWithStatistics<T>> funcCreateGroup) {
        this.assignmentCSV = assignmentCSV;
        this.numberLevelsGrouping = numberLevelsGrouping;
        this.funcCreateGroup = funcCreateGroup;

        groupsMap = new HashMap[numberLevelsGrouping];
        for (int i = 0; i < numberLevelsGrouping; i++) {
            groupsMap[i] = new HashMap<>();
        }

        groupAll = funcCreateGroup.apply("all");
    }

    public List<StatisticsToExport> statisticsForAllGroups() {
        List<StatisticsToExport> list = new ArrayList<>();
        list.add(groupAll.comparison());

        for (int i = 0; i < numberLevelsGrouping; i++) {
            for (ImageCounterWithStatistics<T> counter : groupsMap[i].values()) {
                list.add(counter.comparison());
            }
        }
        return list;
    }

    private ImageCounterWithStatistics<T> getOrCreate(int level, String key) {

        ImageCounterWithStatistics<T> item = groupsMap[level].get(key);
        if (item == null) {
            item = funcCreateGroup.apply(key);
            groupsMap[level].put(key, item);
        }
        return item;
    }

    // descriptiveSplit can be null
    public ImageCounter<T> groupsForImage(SplitString descriptiveSplit) {
        ImageCounterList<T> list = new ImageCounterList<>();
        list.add(groupAll);

        if (descriptiveSplit != null) {

            for (int i = 0; i < numberLevelsGrouping; i++) {
                // We extract a String describing each level, and retrieve it from the list, adding
                // it
                //   if it doesn't already exist
                if (i < descriptiveSplit.getNumParts()) {
                    String groupName = descriptiveSplit.combineFirstElements(i + 1, "/");
                    list.add(getOrCreate(i, groupName));
                }
            }
        }

        return list;
    }
}
