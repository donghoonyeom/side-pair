package sidepair.domain.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.member.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember extends BaseProjectMember {

    @Column
    private Double participationRate = 0.0;

    public ProjectMember(final ProjectRole role, final LocalDateTime joinedAt,
                         final Project project, final Member member) {
        super(role, joinedAt, project, member);
    }

    public void updateParticipationRate(final Double rate) {
        this.participationRate = rate;
    }

    public Double getParticipationRate() {
        return participationRate;
    }
}
