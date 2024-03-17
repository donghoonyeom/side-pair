package sidepair.service;

import java.net.URL;
import org.springframework.http.HttpMethod;
import sidepair.service.dto.FileInformation;

public interface FileService {
    void save(final String path, final FileInformation fileInformation);

    URL generateUrl(final String path, final HttpMethod httpMethod);
}
