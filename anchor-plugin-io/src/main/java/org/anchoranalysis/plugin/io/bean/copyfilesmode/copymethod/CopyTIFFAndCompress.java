/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod;

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
import org.anchoranalysis.core.error.OperationFailedException;

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

    private static TiffReader createReader(String source)
            throws DependencyException, ServiceException, FormatException, IOException {

        TiffReader reader = new TiffReader();
        reader.setMetadataFiltered(true);
        reader.setOriginalMetadataPopulated(true);

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        reader.setMetadataStore(service.createOMEXMLMetadata());
        reader.setId(source);
        reader.setSeries(0);

        return reader;
    }

    private static TiffWriter createWriter(TiffReader reader, String destination)
            throws FormatException, IOException {

        TiffWriter writer = new TiffWriter();

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
