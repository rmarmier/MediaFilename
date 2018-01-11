## Purpose

To generate new filenames for media files, so files from different sources can be archived together with a consistent ordering.

## Arguments

1. A timezone definition, from those defined in the Offset class. For historical reasons and compatibility with an older tool.
2. A file or directory containing targeted media files.

## Behaviour

The tool first determines the working directory. The parent directory of the targeted file or directory will be used, unless the given path is relative, in which case the user.dir system property will be assumed to be the desired working directory.

If a directory has been given, it is traversed recursively in search for supported media files.

The tool generates a rename command for each supported media files. It embeds the UTC time and the timezone into the new filename so the usual alphanumerical sort will keep files in chronological order.

In addition, the tool does a second pass to try to match any unsupported companion file like *.xmp to its master's basename and rename it accordingly.

## Format of the generated filenames

The new name for the media file implement the following format:


[Year]-[Month]-[Day]_[hours][minutes][seconds]utc_tz[timezone]_[original file name]

````
Example:
2015-12-03_074904utc_tz+0100_DSC_5926.JPG
````

This format allows the natural ordering of media files from various origins according to the time of capture, independently of the timezone.

## Supported media files

| File type       | EXIF field            | Explanation                                                 |
| --------------- | --------------------- | ----------------------------------------------------------- |
| JPEG            | DateTimeOriginal      | Generic support for JPEG files, indifferent of camera make. |
| Nikon NEF       | DateTimeOriginal      | Support for Nikon NEF raw format pictures.                  |

## How to use the result

The tool sends the rename command simultaneously to the standard output and to a shell command file in the working directory. The command file can be executed or sourced in place.

Additionally, it produces two log files, one detailing the processing and one for errors only (for historical and compatibility reasons with an earlier GUI wrapper).

## System requirements

* Java 8
* ExifTool by Phil Harvey

## Version history
v 1.0 - 2016-03-09 - Initial usable version.

## Author
Raphaël Marmier
