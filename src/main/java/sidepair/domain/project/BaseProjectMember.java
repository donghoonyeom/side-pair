package sidepair.domain.project;

import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import sidepair.domain.BaseEntity;
import sidepair.domain.member.Member;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseProjectMember extends BaseEntity {

    protected static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS";

    @Enumerated(value = EnumType.STRING)
    protected ProjectRole role;

    @CreatedDate
    protected LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @QueryInit(value = {"feedContent.project"})
    protected Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @QueryInit(value = {"email"})
    protected Member member;

    public BaseProjectMember(final ProjectRole role, final LocalDateTime joinedAt,
                             final Project project, final Member member) {
        this(null, role, joinedAt, project, member);
    }

    public BaseProjectMember(final Long id, final ProjectRole role, final LocalDateTime joinedAt,
                             final Project project, final Member member) {
        this.id = id;
        this.role = role;
        this.joinedAt = joinedAt;
        this.project = project;
        this.member = member;
    }

    @PrePersist
    private void prePersist() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        final String formattedTime = LocalDateTime.now().format(formatter);
        joinedAt = LocalDateTime.parse(formattedTime, formatter);
    }

    public boolean isLeader() {
        return role == ProjectRole.LEADER;
    }

    public boolean isSameMember(final Member member) {
        return this.member.equals(member);
    }

    public void becomeLeader() {
        this.role = ProjectRole.LEADER;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final BaseProjectMember that = (BaseProjectMember) o;
        return Objects.equals(project, that.project) && Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), project, member);
    }

    public ProjectRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public Project getProject() {
        return project;
    }

    public Member getMember() {
        return member;
    }
}
