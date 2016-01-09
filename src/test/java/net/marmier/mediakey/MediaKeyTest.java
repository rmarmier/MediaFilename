package net.marmier.mediakey;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Added by raphael on 07.02.16.
 */
public class MediaKeyTest {

    @Test
    public void testGetParent() {

        // Nominal case
        {
            File targetFile = new File("/parentdirectory/directory");
            assertEquals("/parentdirectory", targetFile.getParent());
        }

        // Case of target directly under root, absolute path
        {
            File targetFile = new File("/directory");
            assertEquals("/", targetFile.getParent());
        }

        // Case of relative path
        {
            File targetFile = new File("parentdirectory/directory");
            assertEquals("parentdirectory", targetFile.getParent());
        }

        // Case of relative path
        {
            File targetFile = new File("directory");
            assertNull(targetFile.getParent());
        }
    }

    @Test
    public void getCurrentDirectoryFromSystem() {
        String userDir = System.getProperty("user.dir");
        assertNotNull(userDir);
    }

}