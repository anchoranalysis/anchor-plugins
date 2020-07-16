/* (C)2020 */
package org.anchoranalysis.plugin.opencv;

public class CVInit {

    private CVInit() {}

    /**
     * This routine must always be executed at least once before calling any routines in the OpenCV
     * library
     */
    public static void alwaysExecuteBeforeCallingLibrary() {
        nu.pattern.OpenCV.loadShared();
    }
}
