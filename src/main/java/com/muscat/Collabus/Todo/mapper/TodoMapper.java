package com.muscat.Collabus.Todo.mapper;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoUpdateRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.enums.status.TodoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoMapper {

    public Todo mapToEntity(TodoRequestDto dto, Task task, User assignee) {
        return Todo.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .task(task)
                .assignee(assignee)
                .status(TodoStatus.IN_PROGRESS)
                .build();
    }

    public TodoResponseDto mapToDto(Todo todo) {
        User assignee = todo.getAssignee();

        return TodoResponseDto.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .status(todo.getStatus().name())
                .taskId(todo.getTask().getId())
                .isDone(todo.isDone())
                .doneAt(todo.getDoneAt())
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeNickname(assignee != null ? assignee.getNickname() : null)
                .assigneeDisplayName(assignee != null ? assignee.getDisplayName() : null)
                .build();
    }

    public void updateFromDto(TodoUpdateRequestDto dto, Todo todo) {
        todo.updateContent(dto.getTitle(), dto.getDescription(), dto.getDueDate());
    }
}
