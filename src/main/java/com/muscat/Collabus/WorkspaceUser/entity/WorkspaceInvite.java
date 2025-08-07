package com.muscat.Collabus.WorkspaceUser.entity;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.enums.status.InviteStatus;
import com.muscat.Collabus.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "workspace_invite")
public class WorkspaceInvite extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inviter_id", nullable = false)
  private User inviter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invitee_id", nullable = false)
  private User invitee;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkspaceRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InviteStatus status = InviteStatus.PENDING;
}
