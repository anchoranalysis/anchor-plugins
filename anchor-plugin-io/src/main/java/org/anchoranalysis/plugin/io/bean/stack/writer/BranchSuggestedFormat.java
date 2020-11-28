package org.anchoranalysis.plugin.io.bean.stack.writer;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.functional.CheckedStream;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.io.stack.output.StackWriteOptions;
import lombok.Getter;
import lombok.Setter;

/**
 * Selects particular writers based on if particular suggested image formats are present.
 * 
 * <p>This is useful for mapping a suggested file format by user to the underlying
 * {@link StackWriter} that should write it.
 * 
 * @author Owen Feehan
 *
 */
public class BranchSuggestedFormat extends StackWriterDelegateBase {

    // START BEAN PROPERTIES
    /** A list of writers to select if a given format is used
     *
     * <p>The name of the format should be the default extension (without a leading period)
     *  of the corresponding format. Case is irrelevant.
     */
    @BeanField @Getter @Setter private List<StackWriter> writersIfSuggested;

    /** The writer to use if there is no suggested file-format. */
    @BeanField @Getter @Setter private StackWriter writerIfNoSuggestion;
    // END BEAN PROPERTIES
    
    @Override
    protected StackWriter selectDelegate(StackWriteOptions writeOptions) throws ImageIOException {

        Optional<ImageFileFormat> format = writeOptions.getSuggestedFormatToWrite();
        if (format.isPresent()) {
            String extension = format.get().getDefaultExtension();
            return writerForExtension(extension, writeOptions).orElseThrow( () -> 
                new ImageIOException("No stack-writer is present in 'writers' that matches " + extension)
            );
        } else {
            return writerIfNoSuggestion;
        }
    }
    
    /** 
     * Searches for an extension in {@code writers} whose default-extension matches {@code extension}.
     *  
     * @throws ImageIOException */
    private Optional<StackWriter> writerForExtension(String extension, StackWriteOptions writeOptions) throws ImageIOException {
        return CheckedStream.filter( writersIfSuggested.stream(), ImageIOException.class, writer -> writer.fileFormat(writeOptions).matchesIdentifier(extension) ).findFirst();
    }
}
