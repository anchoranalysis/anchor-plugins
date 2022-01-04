/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.io.csv.metadata.RowLabels;
import org.anchoranalysis.feature.io.name.CombinedName;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.SimpleName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class IdentifierHelper {

    public static RowLabels identifierFor(
            String imageIdentifier,
            String objectIdentifier,
            Optional<String> groupGeneratorName,
            String providerName,
            boolean multipleProviders) {
        return new RowLabels(
                Optional.of(new String[] {imageIdentifier, objectIdentifier}),
                createGroupName(groupGeneratorName, providerName, multipleProviders));
    }

    private static Optional<MultiName> createGroupName(
            Optional<String> groupGeneratorName, String providerName, boolean multipleProviders) {
        if (multipleProviders) {
            if (groupGeneratorName.isPresent()) {
                return Optional.of(new CombinedName(groupGeneratorName.get(), providerName));
            } else {
                return Optional.of(new SimpleName(providerName));
            }
        } else {
            return groupGeneratorName.map(SimpleName::new);
        }
    }
}
