/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark.collection;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.anchor.mpp.mark.conic.Ellipse;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.deserializer.ObjectInputStreamDeserializer;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;

public class FromSerializedMarks extends MarkCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String filePath;
    // END BEAN PROPERTIES

    @Override
    public Optional<MarkCollection> propose(MarkWithIdentifierFactory markFactory, ProposerContext context) {

        ObjectInputStreamDeserializer<MarkCollection> deserializer = new ObjectInputStreamDeserializer<>();
        try {
            Path path = Paths.get(filePath);
            return Optional.of(deserializer.deserialize(path));
        } catch (DeserializationFailedException e) {
            return Optional.empty();
        }
    }

    @Override
    public String descriptionBean() {
        return toString();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof Ellipse;
    }
}