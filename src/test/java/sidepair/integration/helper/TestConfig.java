package sidepair.integration.helper;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sidepair.persistence.auth.RefreshTokenRepository;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.FileService;

@TestConfiguration
public class TestConfig {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TestConfig(final ProjectRepository projectRepository,
                      final ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Bean
    public FileService fileService() {
        return new TestFileService();
    }

    @Bean
    public TestTransactionService testTransactionService() {
        return new TestTransactionService(projectRepository, projectMemberRepository);
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return new TestRefreshTokenRepository();
    }
}
