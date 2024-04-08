package sidepair.domain.project;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.member.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPendingMember extends BaseProjectMember {

    public ProjectPendingMember(final ProjectRole role, final Member member) {
        super(role, null, null, member);
    }

    public ProjectPendingMember(final ProjectRole role, final Project project, final Member member) {
        super(role, null, project, member);
    }

    public ProjectPendingMember(final ProjectRole role, final LocalDateTime joinedAt,
                                final Project project, final Member member) {
        super(role, joinedAt, project, member);
    }

    public ProjectPendingMember(final Long id, final ProjectRole role, final LocalDateTime joinedAt,
                                final Project project, final Member member) {
        super(id, role, joinedAt, project, member);
    }

    public void initProject(final Project project) {
        if (this.project == null) {
            this.project = project;
        }
    }
}
