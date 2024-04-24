package sidepair.persistence.project;

import java.util.List;
import sidepair.domain.project.ProjectMember;

public interface ProjectMemberJdbcRepository {
    void saveAllInBatch(final List<ProjectMember> projectMembers);
}

