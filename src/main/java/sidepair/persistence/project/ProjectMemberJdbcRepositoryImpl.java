package sidepair.persistence.project;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import sidepair.domain.project.ProjectMember;

@Repository
public class ProjectMemberJdbcRepositoryImpl implements ProjectMemberJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectMemberJdbcRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAllInBatch(final List<ProjectMember> projectMembers) {
        final String sql = "INSERT INTO project_member "
                + "(project_id, member_id, role, joined_at, participation_rate) "
                + "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, projectMembers, projectMembers.size(), ((ps, projectMember) -> {
            ps.setLong(1, projectMember.getProject().getId());
            ps.setLong(2, projectMember.getMember().getId());
            ps.setString(3, projectMember.getRole().name());
            ps.setObject(4, projectMember.getJoinedAt());
            ps.setDouble(5, projectMember.getParticipationRate());
        }));
    }
}
