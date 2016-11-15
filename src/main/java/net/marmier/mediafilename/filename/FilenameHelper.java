package net.marmier.mediafilename.filename;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Added by raphael on 06.10.16.
 */
public class FilenameHelper {
        private static Pattern STRIP_EXTENSION_PATTERN = Pattern.compile("^([/]?([^/\\n]+/)*([^./\\n]+))(\\.([a-zA-Z]+))?$");

    public static String stripExtension(String filename) {
        Objects.requireNonNull(filename);

        Matcher matcher = STRIP_EXTENSION_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static String getExtension(String filename) {
        Objects.requireNonNull(filename);

        Matcher matcher = STRIP_EXTENSION_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return matcher.group(matcher.groupCount());
        } else {
            return null;
        }
    }
}
