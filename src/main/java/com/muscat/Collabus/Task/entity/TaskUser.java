package com.muscat.Collabus.Task.entity;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.enums.role.TaskRole;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskUser {

  @EmbeddedId
  private TaskUserPk id;

  @ManyToOne
  @MapsId("taskId")
  private Task task;

  @ManyToOne
  @MapsId("userId")
  private User user;

  @Enumerated(EnumType.STRING)
  private TaskRole role;
}
