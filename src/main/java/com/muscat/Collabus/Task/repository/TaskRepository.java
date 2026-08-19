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

    // 목록에서 담당 매니저를 함께 보여주므로 한 번에 가져온다.
    // 조건 조합은 TaskSpecifications 가 만든다
    @Override
    @EntityGraph(attributePaths = {"taskManager"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}
