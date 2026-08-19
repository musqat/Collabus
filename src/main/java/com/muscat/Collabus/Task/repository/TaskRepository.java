package com.muscat.Collabus.Task.repository;

import java.util.List;
import com.muscat.Collabus.Task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface TaskRepository extends JpaRepository<Task, Long>,
    JpaSpecificationExecutor<Task> {

    // 탈퇴 처리에서 매니저를 넘길 Task 를 찾는다
    List<Task> findAllByTaskManager_Id(Long userId);

    // 조건은 TaskSpecifications 가 만든다. 담당 매니저를 함께 가져온다
    @Override
    @EntityGraph(attributePaths = {"taskManager"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}
