package com.muscat.Collabus.Workspace.repository;

import com.muscat.Collabus.Workspace.entity.Workspace;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

  // 내가 만든 워크스페이스 목록. 생성자 정보를 함께 가져온다
  @EntityGraph(attributePaths = {"founder"})
  List<Workspace> findAllByFounderId(Long userId);
}
