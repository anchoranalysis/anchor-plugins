package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.WriteIntoFolder;
import org.junit.Rule;
import org.junit.Test;

public class OutlinePreserveRelativeSizeTest {

    private static final SizeXY SIZE = new SizeXY(300, 200);

    private static final int NUMBER_INTERSECTING = 4;
    private static final int NUMBER_NOT_INTERSECTING = 2;

    private static final ObjectCollection OBJECTS =
            IntersectingCircleObjectsFixture.generateIntersectingObjects(
                    NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, false);

    private static final Stack BACKGROUND = EnergyStackFixture.create(true, false).asStack();

    @Rule public WriteIntoFolder writer = new WriteIntoFolder(false);

    @Test
    public void testThumbnails() throws OperationFailedException, CreateException {

        List<DisplayStack> thumbnails = createAndWriteThumbnails();

        assertEquals(
                "number of thumbnails",
                thumbnails.size(),
                NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING);
        for (DisplayStack thumbnail : thumbnails) {
            assertEquals("size of a thumbnail", SIZE.asExtent(), thumbnail.extent());
        }

        DualComparer comparer =
                DualComparerFactory.compareTemporaryFolderToTest(
                        writer.getFolder(), "thumbnails", "thumbnails01");
        assertTrue(
                "thumbnails are identical to saved copy", comparer.compareTwoSubdirectories("."));
    }

    /**
     * Creates thumbnails and writes them to the temporary folder
     *
     * @throws OperationFailedException
     */
    private List<DisplayStack> createAndWriteThumbnails() throws OperationFailedException {
        OutlinePreserveRelativeSize outline = createOutline();
        outline.start(OBJECTS, boundingBoxes(OBJECTS), Optional.of(BACKGROUND));

        try {
            List<DisplayStack> thumbnails = thumbnailsFor(outline, OBJECTS);
            writer.writeList("thumbnails", thumbnails);
            outline.end();
            return thumbnails;
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<DisplayStack> thumbnailsFor(
            OutlinePreserveRelativeSize outline, ObjectCollection objects) throws CreateException {
        return objects.stream()
                .mapToList(object -> outline.thumbnailFor(ObjectCollectionFactory.of(object)));
    }

    private static StreamableCollection<BoundingBox> boundingBoxes(ObjectCollection objects) {
        return new StreamableCollection<>(
                () -> objects.streamStandardJava().map(ObjectMask::boundingBox));
    }

    private static OutlinePreserveRelativeSize createOutline() {
        OutlinePreserveRelativeSize outline = new OutlinePreserveRelativeSize();
        outline.setColorUnselectedObjects(new RGBColorBean(Color.BLUE));
        outline.setOutlineWidth(1);
        outline.setSize(SIZE);
        outline.setInterpolator(new InterpolatorBeanLanczos());
        return outline;
    }
}
