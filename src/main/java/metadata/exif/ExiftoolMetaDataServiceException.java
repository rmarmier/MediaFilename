package metadata.exif;

/**
 * Created by raphael on 11.12.15.
 */
public class ExiftoolMetaDataServiceException extends RuntimeException {

    public ExiftoolMetaDataServiceException(String message, Throwable e) {
        super(message, e);
    }
}
