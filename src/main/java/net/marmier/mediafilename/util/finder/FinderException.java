package net.marmier.mediafilename.util.finder;

/**
 * Added by raphael on 09.08.17.
 */
public class FinderException extends RuntimeException {

    public FinderException(String message, Exception e) {
        super(message, e);
    }

    public FinderException(String message) {
        super(message);
    }
}
