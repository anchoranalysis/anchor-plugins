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

import java.util.HashMap;
import java.util.function.Function;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.plugin.annotation.comparison.AddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupList;

class SharedState<T extends Assignment> {

    private CSVAssignment assignmentCSV;
    private AnnotationGroup<T> groupAll;
    private HashMap<String, AnnotationGroup<T>>[] groupsMap;
    private int numLevelsGrouping;
    private Function<String, AnnotationGroup<T>> funcCreateGroup;

    @SuppressWarnings("unchecked")
    public SharedState(
            CSVAssignment assignmentCSV,
            int numLevelsGrouping,
            Function<String, AnnotationGroup<T>> funcCreateGroup) {
        this.assignmentCSV = assignmentCSV;
        this.numLevelsGrouping = numLevelsGrouping;
        this.funcCreateGroup = funcCreateGroup;

        groupsMap = new HashMap[numLevelsGrouping];
        for (int i = 0; i < numLevelsGrouping; i++) {
            groupsMap[i] = new HashMap<>();
        }

        groupAll = funcCreateGroup.apply("all");
    }

    public AnnotationGroupList<T> allGroups() {
        AnnotationGroupList<T> list = new AnnotationGroupList<>();
        list.add(groupAll);

        for (int i = 0; i < numLevelsGrouping; i++) {
            list.addAll(groupsMap[i].values());
        }

        return list;
    }

    private AnnotationGroup<T> getOrCreate(int level, String key) {

        AnnotationGroup<T> item = groupsMap[level].get(key);
        if (item == null) {
            item = funcCreateGroup.apply(key);
            groupsMap[level].put(key, item);
        }
        return item;
    }

    // descriptiveSplit can be null
    public AddAnnotation<T> groupsForImage(SplitString descriptiveSplit) {
        AnnotationGroupList<T> list = new AnnotationGroupList<>();
        list.add(groupAll);

        if (descriptiveSplit != null) {

            for (int i = 0; i < numLevelsGrouping; i++) {
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

    public CSVAssignment getAssignmentCSV() {
        return assignmentCSV;
    }
}
