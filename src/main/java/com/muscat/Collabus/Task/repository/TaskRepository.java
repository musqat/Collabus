package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface TaskRepository extends JpaRepository<Task, Long>,
    JpaSpecificationExecutor<Task> {

    // 조건은 TaskSpecifications 가 만든다. 담당 매니저를 함께 가져온다
    @Override
    @EntityGraph(attributePaths = {"taskManager"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}
