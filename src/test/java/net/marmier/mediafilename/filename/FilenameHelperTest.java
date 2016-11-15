package net.marmier.mediafilename.filename;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Added by raphael on 06.10.16.
 */
public class FilenameHelperTest {

    @Test
    public void testStripExtension() throws Exception {
        assertEquals("myFile", FilenameHelper.stripExtension("myFile.png"));
    }
    @Test
    public void testStripExtensionWithPath() throws Exception {
        assertEquals("mypath/myFile", FilenameHelper.stripExtension("mypath/myFile.png"));
        assertEquals("mypa.th/myFile", FilenameHelper.stripExtension("mypa.th/myFile.png"));
    }

    @Test
    public void testGetExtension() throws Exception {
        assertEquals("png", FilenameHelper.getExtension("myFile.png"));
    }

    @Test
    public void testGetExtensionWithPath() throws Exception {
        assertEquals("png", FilenameHelper.getExtension("mypath/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("mypa.th/myFile.png"));
    }
}