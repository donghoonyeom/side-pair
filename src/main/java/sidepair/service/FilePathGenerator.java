package sidepair.service;

public interface FilePathGenerator {

    String makeFilePath(final ImageDirType dirType, String originalFileName);
}