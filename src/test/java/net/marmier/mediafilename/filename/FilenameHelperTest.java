package net.marmier.mediafilename.filename;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
    public void testStripExtensionDotFile() throws Exception {
        assertEquals(".myDotFile", FilenameHelper.stripExtension(".myDotFile.png"));
        assertEquals("mypath/.myDotFile", FilenameHelper.stripExtension("mypath/.myDotFile.png"));
        assertEquals("/mypath/mypath/.myDotFile", FilenameHelper.stripExtension("/mypath/mypath/.myDotFile.png"));
    }

    @Test
    public void testStripExtensionDotInTheMiddle() {
        assertEquals("myFile.blahblah.myDotFile", FilenameHelper.stripExtension("myFile.blahblah.myDotFile.png"));
        assertEquals(".myFile.blahblah.myDotFile", FilenameHelper.stripExtension(".myFile.blahblah.myDotFile.png"));
        assertEquals("mypath/.myFile.myDotFile", FilenameHelper.stripExtension("mypath/.myFile.myDotFile.png"));
    }

    @Test
    public void unsupportedCornerCasesToFixIfPossible() {
        // Some unsupported corner cases that shoul not happen in real life
        assertNull(FilenameHelper.stripExtension("mypath/..myDotFile.png"));
        assertNull(FilenameHelper.stripExtension("mypath/..myDo..tFile.png"));
        assertNull(FilenameHelper.stripExtension("mypath/myDotFile.png.."));
        assertNull(FilenameHelper.stripExtension("mypath/..png"));
    }

    @Test
    public void testStripExtensionDotFileWithoutExtention() throws Exception {
        assertEquals(".myDotFileWithoutExtention", FilenameHelper.stripExtension(".myDotFileWithoutExtention"));
        assertEquals("mypath/mypath/.myDotFileWithoutExtention", FilenameHelper.stripExtension("mypath/mypath/.myDotFileWithoutExtention"));
        assertEquals("/mypath/mypath/.myDotFileWithoutExtention", FilenameHelper.stripExtension("/mypath/mypath/.myDotFileWithoutExtention"));
    }

    @Test
    public void testStripExtensionFileWithoutExtention() throws Exception {
        assertEquals("myFileWithoutExtention", FilenameHelper.stripExtension("myFileWithoutExtention"));
        assertEquals("mypath/mypath/myFileWithoutExtention", FilenameHelper.stripExtension("mypath/mypath/myFileWithoutExtention"));
        assertEquals("/mypath/mypath/myFileWithoutExtention", FilenameHelper.stripExtension("/mypath/mypath/myFileWithoutExtention"));
    }

    @Test
    public void testGetExtension() throws Exception {
        assertEquals("png", FilenameHelper.getExtension("myFile.png"));
        assertEquals("png", FilenameHelper.getExtension(".myFile.png"));
        assertNull(FilenameHelper.getExtension("myFile"));
        assertNull(FilenameHelper.getExtension(".myFile"));
    }

    @Test
    public void testGetExtensionDotInTheMiddle() {
        assertEquals("png", FilenameHelper.getExtension("blahblah.myFile.png"));
        assertEquals("png", FilenameHelper.getExtension(".blahblah.myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/mypath/blahblah.myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/mypath/.blahblah.myFile.png"));
    }

    @Test
    public void testGetExtensionWithPath() throws Exception {
        assertEquals("png", FilenameHelper.getExtension("mypath/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/mypath/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/mypath/mypath/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("mypa.th/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("mypath/mypa.th/myFile.png"));
        assertEquals("png", FilenameHelper.getExtension("/mypath/mypa.th/myFile.png"));
    }
}