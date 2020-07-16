/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.SingleFileTypeGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class ObjectAsStringGenerator<T> extends SingleFileTypeGenerator implements IterableGenerator<T> {

    private T object = null;

    public ObjectAsStringGenerator() {
        super();
    }

    public ObjectAsStringGenerator(T object) {
        super();
        this.object = object;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {

        if (getIterableElement() == null) {
            throw new OutputWriteFailedException("no mutable element set");
        }

        try (FileWriter outFile = new FileWriter(filePath.toFile())) {

            PrintWriter out = new PrintWriter(outFile);
            out.println(object.toString());

        } catch (IOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    @Override
    public String getFileExtension(OutputWriteSettings outputWriteSettings) {
        return outputWriteSettings.getExtensionText();
    }

    @Override
    public T getIterableElement() {
        return this.object;
    }

    @Override
    public void setIterableElement(T element) {
        this.object = element;
    }

    @Override
    public Generator getGenerator() {
        return this;
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return Optional.of(new ManifestDescription("text", "object"));
    }
}
