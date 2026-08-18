package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 목록에서 담당 매니저를 함께 보여주므로 한 번에 가져온다
    @EntityGraph(attributePaths = {"taskManager"})
    Page<Task> findAllByWorkspace_Id(Long workspaceId, Pageable pageable);
}
