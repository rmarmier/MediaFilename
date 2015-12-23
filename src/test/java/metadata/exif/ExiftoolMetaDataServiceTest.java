package metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import metadata.ExifMetaData;
import metadata.MetaData;
import metadata.MetaDataService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

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

        MetaData mdata = service.metadataFromFile(file, new ExifProfile() {
            public MetaData convert(Map<ExifTool.Tag, String> valueMap) {
                return new ExifMetaData(valueMap);
            }
        });
        System.out.println(mdata.getCaptureDateTime());
    }
}