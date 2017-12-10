package net.marmier.mediafilename.util.finder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Added by raphael on 13.08.17.
 */
public class FinderTest {

    private static final String TEST_RESOURCES_DIR = "src/test/resources/util/finder";

    private File testResourceDir;

    @Before
    public void setUp() throws IOException {
        testResourceDir = new File(TEST_RESOURCES_DIR);
    }

    @Test
    public void find() throws Exception {
        //
        {
            Finder finder = new Finder(new File("src/test/resources/util"));
            List<Path> results = finder.find(testResourceDir, false, false);
            Assert.assertNotNull(results);
            Assert.assertEquals(10, results.size());
            Assert.assertEquals("src/test/resources/util/finder/directory01/directory03/testfile06.txt", results.get(0).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/directory03/testfile07.txt", results.get(1).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/testfile01.txt", results.get(2).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/testfile02.txt", results.get(3).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/testfile03.txt", results.get(4).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/testfile04.txt", results.get(5).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory01/testfile05.txt", results.get(6).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory02/testfile08.txt", results.get(7).toString());
            Assert.assertEquals("src/test/resources/util/finder/directory02/testfile09.txt", results.get(8).toString());
            Assert.assertEquals("src/test/resources/util/finder/testfile10.txt", results.get(9).toString());

        }
    }

}