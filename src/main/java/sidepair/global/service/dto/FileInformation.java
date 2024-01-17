package sidepair.global.service.dto;

import java.io.InputStream;

public record FileInformation(
        String originalFileName,
        long size,
        String contentType,
        InputStream inputStream
) {
}
