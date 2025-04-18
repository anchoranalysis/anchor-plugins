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

package org.anchoranalysis.plugin.io.bean.file.copy.method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.in.TiffReader;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CopyTIFFAndCompress {

    public static void apply(String source, Path destination) throws OperationFailedException {

        // Delete any file that is already at the destination
        try {
            Files.deleteIfExists(destination);
        } catch (IOException e2) {
            throw new OperationFailedException(e2);
        }

        compressFile(source, destination);
    }

    private static void compressFile(String source, Path destination)
            throws OperationFailedException {

        String d = destination.toString();

        try (TiffReader reader = createReader(source)) {

            TiffWriter writer = createWriter(reader, d);

            int numImages = reader.getImageCount();
            for (int i = 0; i < numImages; i++) {
                byte[] buf = reader.openBytes(i);

                writer.saveBytes(i, buf);
            }
            writer.close();

        } catch (FormatException | IOException | DependencyException | ServiceException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Created an opens a reader for TIFF files.
     *
     * <p>It is the responsibility of the calling function to close the {@link TiffReader}.
     */
    private static TiffReader createReader(String source)
            throws DependencyException, ServiceException, FormatException, IOException {

        TiffReader reader = new TiffReader(); // NOSONAR
        reader.setMetadataFiltered(true);
        reader.setOriginalMetadataPopulated(true);

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        reader.setMetadataStore(service.createOMEXMLMetadata());
        reader.setId(source);
        reader.setSeries(0);

        return reader;
    }

    /**
     * Created an opens a writer for TIFF files.
     *
     * <p>It is the responsibility of the calling function to close the {@link TiffWriter}.
     */
    private static TiffWriter createWriter(TiffReader reader, String destination)
            throws FormatException, IOException {

        TiffWriter writer = new TiffWriter(); // NOSONAR

        writer.setCompression("LZW");
        writer.setWriteSequentially(true);
        writer.setBigTiff(true);
        writer.setMetadataRetrieve((MetadataRetrieve) reader.getMetadataStore());
        writer.setId(destination);
        writer.setSeries(0);
        writer.setInterleaved(reader.isInterleaved());

        return writer;
    }
}
