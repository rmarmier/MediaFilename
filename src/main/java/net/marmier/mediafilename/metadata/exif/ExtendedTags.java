package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.Tag;

/**
 * Added by raphael on 13.05.16.
 *
 * Copied from StandardTag, as there is no easy way to extend it.
 */
public enum ExtendedTags implements Tag {

    MEDIA_CREATE_DATE("MediaCreateDate", Type.STRING);

        /**
         * Used to get the name of the tag (e.g. "Orientation", "ISO", etc.).
         */
    private final String name;

    /**
     * Used to get a hint for the native type of this tag's value as
     * specified by Phil Harvey's <a href="http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/index.html">ExifTool Tag Guide</a>.
     */
    private final Type type;

    ExtendedTags(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> T parse(String value) {
        return type.parse(value);
    }

    @SuppressWarnings("unchecked")
    private enum Type {
        INTEGER {
            @Override
            public <T> T parse(String value) {
                return (T) Integer.valueOf(Integer.parseInt(value));
            }
        },
        DOUBLE {
            @Override
            public <T> T parse(String value) {
                return (T) Double.valueOf(Double.parseDouble(value));
            }
        },
        STRING {
            @Override
            public <T> T parse(String value) {
                return (T) value;
            }
        };

        public abstract <T> T parse(String value);
    }
}
