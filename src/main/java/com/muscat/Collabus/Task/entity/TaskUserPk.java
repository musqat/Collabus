package com.muscat.Collabus.Task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class TaskUserPk implements Serializable {

  @Column(name = "task_id")
  private Long taskId;

  @Column(name = "user_id")
  private Long userId;
}
