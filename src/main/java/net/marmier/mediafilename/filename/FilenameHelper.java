package net.marmier.mediafilename.filename;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Added by raphael on 06.10.16.
 */
public class FilenameHelper {
    private static Pattern OLD_STRIP_EXTENSION_PATTERN =
        Pattern.compile("^([/]?([^/\\n]+/)*([^./\\n]+))(\\.([a-zA-Z]+))?$");
    // ^((\B\.)?(([^./\n]+\.)+|[^./\n]+))([^./\n]+)?$
    private static Pattern STRIP_EXTENSION_PATTERN =
        Pattern.compile("^((\\B\\.)?(([^./\\n]+\\.)+|[^./\\n]+))([^./\\n]+)?$");

    public static String stripExtension(String path) {
        Objects.requireNonNull(path);
        Path p = new File(path).toPath();
        String filename = p.getFileName().toString();
        Path parent = p.getParent();

        Matcher matcher = STRIP_EXTENSION_PATTERN.matcher(filename);
        if (matcher.matches()) {
            String stripped = matcher.group(1);
            if (stripped.endsWith(".") && stripped.length() > 1 && matcher.group(4) != null) {
                stripped = stripped.substring(0, stripped.length() - 1);
            }
            if (parent != null) {
                return parent.resolve(stripped).toString();
            } else {
                return stripped;
            }
        } else {
            return null;
        }
    }

    public static String getExtension(String path) {
        Objects.requireNonNull(path);
        String filename = new File(path).toPath().getFileName().toString();

        Matcher matcher = STRIP_EXTENSION_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return matcher.group(matcher.groupCount());
        } else {
            return null;
        }
    }
}
