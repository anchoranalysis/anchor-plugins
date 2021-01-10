/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.plugin.io.multifile.FileDetails;

public class RegularExpression extends FilePathParser {

    // START BEAN PARAMETERS
    @BeanField @Getter private String expression;

    @BeanField @Getter @Setter private int channelGroupID = 0; // 0 Disables

    @BeanField @Getter @Setter private int zSliceGroupID = 0; // 0 Disables

    @BeanField @Getter @Setter private int timeIndexGroupID = 0; // 0 Disables

    @BeanField @Getter @Setter private int keyGroupID = 0; // 0 Disables

    @BeanField @Getter @Setter private boolean keyRequired = false;
    // END BEAN PARAMETERS

    private String key;

    Pattern pattern = null;

    @Override
    public Optional<FileDetails> parsePath(Path path) {

        // Replace backslashes with forward slashes
        Matcher matcher =
                pattern.matcher(
                        FilePathToUnixStyleConverter.toStringUnixStyle(path.toAbsolutePath()));

        if (!matcher.matches()) {
            return Optional.empty();
        }

        int channelNum = ExtractGroup.maybeExtractInt(channelGroupID, matcher);
        int zSliceNum = ExtractGroup.maybeExtractInt(zSliceGroupID, matcher);
        int timeIndex = ExtractGroup.maybeExtractInt(timeIndexGroupID, matcher);

        if (channelNum == -1 || zSliceNum == -1 || timeIndex == -1) {
            return Optional.empty();
        }

        if (keyGroupID > 0) {
            Optional<String> extractedKey = ExtractGroup.extractString(keyGroupID, matcher);
            if (!extractedKey.isPresent() || (extractedKey.get().isEmpty() && keyRequired)) {
                return Optional.empty();
            }
            key = extractedKey.get();
        }
        return Optional.of(
                new FileDetails(
                        path,
                        asOptional(channelGroupID, channelNum),
                        asOptional(zSliceGroupID, zSliceNum),
                        asOptional(timeIndexGroupID, timeIndex)));
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        this.pattern = Pattern.compile(expression);
    }

    private static Optional<Integer> asOptional(int groupID, int val) {
        if (groupID > 0) {
            return Optional.of(val);
        } else {
            return Optional.empty();
        }
    }
}
