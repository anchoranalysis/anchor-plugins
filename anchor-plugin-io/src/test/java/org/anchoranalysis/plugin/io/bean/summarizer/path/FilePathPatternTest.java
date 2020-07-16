/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.path;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.core.error.OperationFailedException;
import org.junit.Test;

public class FilePathPatternTest {

    @Test
    public void test() throws OperationFailedException {

        Path path1 = Paths.get("a/b/c");
        Path path2 = Paths.get("a/b/d");

        FilePathPattern fpp = new FilePathPattern();
        fpp.add(path1);
        fpp.add(path2);

        assertEquals("${0} = \"d\" (1) | \"c\" (1)", secondLineFrom(fpp.describe()));
    }

    private static String secondLineFrom(String multilineStr) {
        String[] split = multilineStr.split(System.getProperty("line.separator"));
        return split[1];
    }
}
