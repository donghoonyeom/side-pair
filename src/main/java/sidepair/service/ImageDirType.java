package sidepair.service;

public enum ImageDirType {
    FEED_NODE("feed"),
    USER_PROFILE("member/profile");

    private final String dirName;

    ImageDirType(final String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}

