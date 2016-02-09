package net.marmier.mediafilename.metadata.exif;

import net.marmier.mediafilename.MediaFilename;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.MetaDataService;
import net.marmier.mediafilename.timezone.Offset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Added by raphael on 12.12.15.
 */
public class ExiftoolMetaDataServiceTest {

    MetaDataService service = new ExiftoolMetaDataService();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testMetadataFromFile() throws Exception {

        {
            File file = new File("src/test/resources/nikon/DSC_4180.JPG");
            MetaData mdata = service.metadataFromFile(file);
            Assert.assertEquals("DSC_4180.JPG", mdata.getFileName());
            Assert.assertEquals("2015-10-18T16:50:29", mdata.getCaptureDateTime().toString());
        }
        {
            File file = new File("src/test/resources/nikon/DSC_5303.NEF");
            MetaData mdata = service.metadataFromFile(file);
            Assert.assertEquals("DSC_5303.NEF", mdata.getFileName());
            Assert.assertEquals("2015-10-27T13:08:16", mdata.getCaptureDateTime().toString());
        }
    }

    @Test
    public void testFilenameGenerationNEF() {
        String expected = "2015-10-27_120816utc_tz+0100_DSC_5303.NEF";

        MediaFilename mediaFilename = new MediaFilename(Offset.forCode("UTC+1"), "src/test/resources/nikon");
        String filename = mediaFilename.generateFilename(new File("src/test/resources/nikon/DSC_5303.NEF"));

        Assert.assertEquals(expected, filename);
    }

    @Test
    public void testFilenameGenerationJPG() {
        String expected = "2015-10-18_145029utc_tz+0200_DSC_4180.JPG";

        MediaFilename mediaFilename = new MediaFilename(Offset.forCode("UTC+2"), "src/test/resources/nikon");
        String filename = mediaFilename.generateFilename(new File("src/test/resources/nikon/DSC_4180.JPG"));

        Assert.assertEquals(expected, filename);
    }
}