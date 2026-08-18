package com.muscat.Collabus.WorkspaceUser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkspaceUserPk implements Serializable {

  @Column(name = "workspace_id")
  private Long workspaceId;

  @Column(name = "user_id")
  private Long userId;
}
