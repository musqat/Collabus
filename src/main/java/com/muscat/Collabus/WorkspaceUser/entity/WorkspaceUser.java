package com.muscat.Collabus.WorkspaceUser.entity;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.common.entity.BaseEntity;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkspaceUser extends BaseEntity {

    @EmbeddedId
    private WorkspaceUserPk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    public void changeRole(WorkspaceRole role) {
        this.role = role;
    }
}
