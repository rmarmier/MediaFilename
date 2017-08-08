package net.marmier.mediafilename.util.finder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Added by raphael on 10.08.17.
 */
public class FinderFileVisitorTest {
    private static final String TEST_RESOURCES_DIR = "src/test/resources/util/finder";

    private File testResourceDir;

    @Before
    public void setUp() throws IOException {
        testResourceDir = new File(TEST_RESOURCES_DIR);

    }

    @Test
    public void walkTree() throws Exception {
        FinderFileVisitor<String> visitor = new FinderFileVisitor<>(Path::toString);
        Files.walkFileTree(testResourceDir.toPath(), visitor);
        Assert.assertEquals(10, visitor.getResults().size());
        Assert.assertEquals("src/test/resources/util/finder/directory01/directory03/testfile06.txt", visitor.getResults().get(0));
        Assert.assertEquals("src/test/resources/util/finder/directory01/directory03/testfile07.txt", visitor.getResults().get(1));
        Assert.assertEquals("src/test/resources/util/finder/directory01/testfile01.txt", visitor.getResults().get(2));
        Assert.assertEquals("src/test/resources/util/finder/directory01/testfile02.txt", visitor.getResults().get(3));
        Assert.assertEquals("src/test/resources/util/finder/directory01/testfile03.txt", visitor.getResults().get(4));
        Assert.assertEquals("src/test/resources/util/finder/directory01/testfile04.txt", visitor.getResults().get(5));
        Assert.assertEquals("src/test/resources/util/finder/directory01/testfile05.txt", visitor.getResults().get(6));
        Assert.assertEquals("src/test/resources/util/finder/directory02/testfile08.txt", visitor.getResults().get(7));
        Assert.assertEquals("src/test/resources/util/finder/directory02/testfile09.txt", visitor.getResults().get(8));
        Assert.assertEquals("src/test/resources/util/finder/testfile10.txt", visitor.getResults().get(9));

    }

    @Test
    public void preVisitDirectory() throws Exception {
        FinderFileVisitor<Boolean> visitor = new FinderFileVisitor<>(f -> true);
        BasicFileAttributes attributes = Files.readAttributes(testResourceDir.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        Assert.assertEquals(FileVisitResult.CONTINUE, visitor.preVisitDirectory(testResourceDir.toPath(), attributes));
    }

    @Test
    public void visitFile() throws Exception {

        // Standard case
        {
            FinderFileVisitor<Long> visitor = new FinderFileVisitor<>(f -> f.toFile().length());
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            BasicFileAttributes attributes = Files.readAttributes(testfile10, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Assert.assertEquals(FileVisitResult.CONTINUE, visitor.visitFile(testfile10, attributes));
            Assert.assertEquals(1, visitor.getResults().size());
            Assert.assertEquals(11L, visitor.getResults().get(0).longValue());
        }

        // Processor returns null, default behaviour
        {
            FinderFileVisitor<Long> visitor = new FinderFileVisitor<>(f -> null);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            BasicFileAttributes attributes = Files.readAttributes(testfile10, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            try {
                visitor.visitFile(testfile10, attributes);
                Assert.fail("A " + FinderException.class.getName() + " is expected on null processing result.");
            }
            catch (FinderException e) {
                Assert.assertEquals("Error processing file: unexpected null result for path: src/test/resources/util/finder/testfile10.txt", e.getMessage());
            }
        }
        // Processor returns null, null tolerated
        {
            FinderFileVisitor<Long> visitor = new FinderFileVisitor<>(f -> null, false, true);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            BasicFileAttributes attributes = Files.readAttributes(testfile10, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Assert.assertEquals(FileVisitResult.CONTINUE, visitor.visitFile(testfile10, attributes));
            Assert.assertEquals(0, visitor.getResults().size());
        }
        // Processor throws an exception, default behaviour (not tolerated)
        {
            FinderFileVisitor<Long> visitor = new FinderFileVisitor<>(f -> { throw new RuntimeException("Test exception"); }, false, false);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            BasicFileAttributes attributes = Files.readAttributes(testfile10, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            try {
                visitor.visitFile(testfile10, attributes);
                Assert.fail("A " + FinderException.class.getName() + " is expected on null processing result.");
            }
            catch (FinderException e) {
                Assert.assertEquals("Error processing file: exception [Test exception] while processing file at path: src/test/resources/util/finder/testfile10.txt", e.getMessage());
            }
        }
        // Processor throws an exception, tolerated
        {
            FinderFileVisitor<Long> visitor = new FinderFileVisitor<>(f -> { throw new RuntimeException("Test exception"); }, true, false);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            BasicFileAttributes attributes = Files.readAttributes(testfile10, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Assert.assertEquals(FileVisitResult.CONTINUE, visitor.visitFile(testfile10, attributes));
            Assert.assertEquals(0, visitor.getResults().size());
        }
    }

    @Test
    public void visitFileFailed() throws Exception {
        // Processor throws an exception, default behaviour (not tolerated)
        {
            FinderFileVisitor<Boolean> visitor = new FinderFileVisitor<>(f -> true, false, false);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            try {
                visitor.visitFileFailed(testfile10, new IOException("Test I/O exception"));
                Assert.fail("A " + FinderException.class.getName() + " is expected on visit fail upon I/O exception.");
            }
            catch (FinderException e) {
                Assert.assertEquals("Error [Test I/O exception] accessing file: src/test/resources/util/finder/testfile10.txt", e.getMessage());
            }
        }
        // Processor throws an exception, tolerated
        {
            FinderFileVisitor<Boolean> visitor = new FinderFileVisitor<>(f -> true, true, false);
            Path testfile10 = new File(testResourceDir.getPath() + "/testfile10.txt").toPath();
            Assert.assertEquals(FileVisitResult.CONTINUE, visitor.visitFileFailed(testfile10, new IOException("Test I/O exception")));
        }

    }

    @Test
    public void postVisitDirectory() throws Exception {
        FinderFileVisitor<Boolean> visitor = new FinderFileVisitor<>(f -> true);
        Assert.assertEquals(FileVisitResult.CONTINUE, visitor.postVisitDirectory(testResourceDir.toPath(), new IOException("Test I/O exception")));
    }

}