/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/** Helpful routines and constants related to outputting for {@link ExportFeaturesTaskTest} */
class ExportOutputter {

    private ExportOutputter() {}

    // Saved output locations for particular tests
    public static final String OUTPUT_DIR_SIMPLE_1 = "simple01/";
    public static final String OUTPUT_DIR_MERGED_1 = "mergedPairs01/";
    public static final String OUTPUT_DIR_MERGED_2 = "mergedPairs02/";
    public static final String OUTPUT_DIR_MERGED_3 = "mergedPairs03/";
    public static final String OUTPUT_DIR_IMAGE_CACHE = "imageCache/";
    public static final String OUTPUT_DIR_SIMPLE_WITH_REFERENCE = "simpleWithReference/";

    // Used for tests where we expect an exception to be thrown, and thus never to actually be
    // compared
    // It doesn't physically exist
    public static final String OUTPUT_DIR_IRRELEVANT = "irrelevant/";

    public static final String[] OUTPUTS_TO_COMPARE = {
        "featuresAggregated.csv",
        "features.csv",
        "grouped/arbitraryGroup/featuresGroup.csv",
        "stackCollection/input.tif",
        "nrgStack/nrgStack_00.tif",
        "nrgStack/nrgStack_01.tif",
        "nrgStack/nrgStack_02.tif",
        "nrgStackParams.xml",
        "grouped/arbitraryGroup/featuresAggregatedGroup.xml",
        String.format("objects/%s.h5", MultiInputFixture.OBJECTS_NAME),
        "manifest.ser.xml"
    };
}
