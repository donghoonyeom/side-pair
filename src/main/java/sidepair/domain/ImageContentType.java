package sidepair.domain;

import java.util.Arrays;
import sidepair.domain.exception.ImageExtensionException;

public enum ImageContentType {
    JPG("image/jpg"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp"),
    ;

    private final String extension;

    ImageContentType(final String extension) {
        this.extension = extension;
    }

    public static ImageContentType findImageContentType(final String imageContentType) {
        return Arrays.stream(values())
                .filter(it -> it.extension.equals(imageContentType))
                .findAny()
                .orElseThrow(() -> new ImageExtensionException("허용되지 않는 확장자입니다."));
    }
}
