package net.marmier.mediafilename.index;

import net.marmier.mediafilename.MediaProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Added by raphael on 25.06.18.
 */
public class IndexedResultsHolderTest {

    private static List<MediaProcessor.Result> MOCK_RESULTS = createMockResults();
    private IndexedResultsHolder holder;

    @Before
    public void setUp() throws Exception {
        holder = new IndexedResultsHolder(MOCK_RESULTS);
    }

    @Test
    public void getResultMatchingPath() {
        MediaProcessor.Result resultMatchingPath = holder.getResultMatchingPath(MOCK_RESULTS.get(0).getOriginalPath());
        Assert.assertSame(MOCK_RESULTS.get(0), resultMatchingPath);
    }

    @Test
    public void getResultMatchingRoot() {
        MediaProcessor.Result resultMatchingPath = holder.getResultMatchingRoot(MOCK_RESULTS.get(0).getOriginalPath());
        Assert.assertSame(MOCK_RESULTS.get(0), resultMatchingPath);
    }

    @Test
    public void addResult() {
        MockResult newMock = new MockResult(new File("/rootdir/addhocfile.jpg").toPath(), "/rootdir/newaddhocfile.jpg",
            "/rootdir/newaddhocfile");
        holder.addResult(newMock);
        Assert.assertNotNull(holder.getResultMatchingPath(newMock.originalPath));
        Assert.assertEquals(newMock.newFilenameRoot, holder.getResultMatchingPath(newMock.originalPath).getNewFilenameRoot());
    }

    static class MockResult implements MediaProcessor.Result {
        private final Path originalPath;
        private final String newFilename;
        private final String newFilenameRoot;

        MockResult(Path originalPath, String newFilename, String newFilenameRoot) {
            this.originalPath = originalPath;
            this.newFilename = newFilename;
            this.newFilenameRoot = newFilenameRoot;
        }

        public Path getOriginalPath() {
            return originalPath;
        }

        public String getNewFilename() {
            return newFilename;
        }

        public String getNewFilenameRoot() {
            return newFilenameRoot;
        }
    }

    private static List<MediaProcessor.Result> createMockResults() {
        return Arrays.asList(
            new MockResult(new File("/blablah/blohbloh/myOLDfilename.jpg").toPath(), "/blablah/blohbloh/myNEWfilename.jpg", "/blablah/blohbloh/myNEWfilename"),
            new MockResult(new File("/blablah/blohbloh/myOLDfilename2.jpg").toPath(), "/blablah/blohbloh/myNEWfilename2.jpg", "/blablah/blohbloh/myNEWfilename2"),
            new MockResult(new File("/blablah/blohbloh/myOLDfilename3.jpg").toPath(), "/blablah/blohbloh/myNEWfilename3.jpg", "/blablah/blohbloh/myNEWfilename3"),
            new MockResult(new File("/blablah/blohbloh/myOLDfilename4.jpg").toPath(), "/blablah/blohbloh/myNEWfilename4.jpg", "/blablah/blohbloh/myNEWfilename4")
        );
    }
}