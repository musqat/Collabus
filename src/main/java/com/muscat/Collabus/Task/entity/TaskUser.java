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
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void changeRole(TaskRole role) {
        this.role = role;
    }
}
