/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegExpFilePathParserTest {

    @Test
    public void testSetPath() {

        RegExpFilePathParser parser = new RegExpFilePathParser();

        // Always do expression first
        parser.setZSliceGroupID(1);
        parser.setChnlGroupID(2);
        parser.setExpression(".*hello_(\\d*)_(\\d*)_.*");

        parser.setPath("hello_4_2_world");

        assertTrue(parser.getChnlNum().get() == 2);
        assertTrue(parser.getZSliceNum().get() == 4);

        parser.setChnlGroupID(0);

        parser.setPath("hello_5_7_world");

        assertTrue(parser.getZSliceNum().get() == 5);
    }
}
