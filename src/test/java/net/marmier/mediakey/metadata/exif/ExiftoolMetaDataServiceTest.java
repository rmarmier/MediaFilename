package net.marmier.mediakey.metadata.exif;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.MetaDataService;
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
        File file = new File("src/test/resources/nikon/2015-10-18_145029utc_tz+0200_DSC_4180.JPG");

        MetaData mdata = service.metadataFromFile(file, new NikonNefProfile());
        Assert.assertEquals("2015-10-18_145029utc_tz+0200_DSC_4180.JPG", mdata.getFileName());
        Assert.assertEquals("2015-10-18T16:50:29", mdata.getCaptureDateTime().toString());
        System.out.println(mdata.getCaptureDateTime());
    }
}