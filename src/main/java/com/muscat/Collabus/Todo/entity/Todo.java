package com.muscat.Collabus.Todo.entity;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.entity.BaseEntity;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.TodoResponse;
import com.muscat.Collabus.enums.status.TodoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    private LocalDateTime doneAt;

    public boolean isDone() {
        return status != TodoStatus.IN_PROGRESS;
    }

    // 담당자가 작업을 끝내고 검수를 요청한다.
    public void requestReview() {
        this.doneAt = LocalDateTime.now();
        this.status = TodoStatus.WAITING_REVIEW;
    }

    // Task Manager 가 검수를 승인한다. 검수 대기 상태에서만 가능하다.
    public void confirm() {
        if (this.status != TodoStatus.WAITING_REVIEW) {
            throw new BusinessException(TodoResponse.NEED_WAITING_REVIEW_STATUS);
        }
        this.status = TodoStatus.CONFIRMED;
    }

    // 검수 대기 중인 Todo 에 작업 내용이 추가되면 다시 진행 중으로 되돌린다.
    public void reopenIfWaitingReview() {
        if (this.status == TodoStatus.WAITING_REVIEW) {
            this.status = TodoStatus.IN_PROGRESS;
        }
    }

    public void assignTo(User assignee) {
        this.assignee = assignee;
    }

    public void updateContent(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
