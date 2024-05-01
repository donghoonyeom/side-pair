package sidepair.integration.helper;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.http.HttpMethod;
import sidepair.service.FileService;
import sidepair.service.dto.FileInformation;

public class TestFileService implements FileService {

    @Override
    public void save(final String path, final FileInformation fileInformation) {
    }

    @Override
    public URL generateUrl(final String path, final HttpMethod httpMethod) {
        try {
            return new URL("http://example.com" + path);
        } catch (final MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
