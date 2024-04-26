package sidepair.persistence.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.domain.project.Project;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectQueryRepository {

    Optional<Project> findById (final Long projectId);

    List<Project> findAllByEndDate (final LocalDate endDate);
}
