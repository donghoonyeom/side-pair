package sidepair.domain.project;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseCreatedTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckFeed extends BaseCreatedTimeEntity {

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_feed_node_id", nullable = false)
    private ProjectFeedNode projectFeedNode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "project_member_id", nullable = false)
    private ProjectMember projectMember;

    public CheckFeed(final String description, final ProjectFeedNode projectFeedNode,
                     final ProjectMember projectMember) {
        this(description, projectFeedNode, projectMember, null);
    }

    public CheckFeed(final String description, final ProjectFeedNode projectFeedNode,
                     final ProjectMember projectMember, final LocalDateTime createdAt) {
        this.description = description;
        this.projectFeedNode = projectFeedNode;
        this.projectMember = projectMember;
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public ProjectMember getProjectMember() {
        return projectMember;
    }
}
