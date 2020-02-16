package org.anchoranalysis.plugin.io.bean.descriptivename;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveFile;
import org.junit.Test;

public class PatternSpanTest {

	@Test
	public void test() {
		PatternSpan ps = new PatternSpan();
		List<DescriptiveFile> ret = ps.descriptiveNamesFor( createFiles(), "<UNKNOWN>");
		
		assertIndexEquals( ret, 0, "b");
		assertIndexEquals( ret, 1, "d");
		assertIndexEquals( ret, 2, "e");
	}
	
	private List<File> createFiles() {
		File file1 = new File("/a/b/c");
		File file2 = new File("/a/d/c");
		File file3 = new File("/a/e/c");
		return Arrays.asList(file1, file2, file3);
	}
	
	private void assertIndexEquals( List<DescriptiveFile> ret, int index, String expected ) {
		String actual = ret.get(index).getDescriptiveName();
		assertEquals( actual, expected);
	}
}
