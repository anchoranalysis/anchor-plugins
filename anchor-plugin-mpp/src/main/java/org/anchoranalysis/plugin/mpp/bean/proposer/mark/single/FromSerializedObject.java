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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark.single;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.deserializer.ObjectInputStreamDeserializer;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.conic.Ellipse;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.proposer.visualization.CreateProposalVisualization;

public class FromSerializedObject extends MarkProposer {

    // START BEAN
    @BeanField @Getter @Setter private String filePath;
    // END BEAN

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof Ellipse;
    }

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) {

        ObjectInputStreamDeserializer<Mark> deserializer = new ObjectInputStreamDeserializer<>();
        try {
            Path path = Paths.get(filePath);
            inputMark.assignFrom(deserializer.deserialize(path));
            return true;

        } catch (DeserializationFailedException e) {
            context.getErrorNode().addFormatted("Exception when deserializing: %s", e.toString());
            return false;
        }
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }
}
