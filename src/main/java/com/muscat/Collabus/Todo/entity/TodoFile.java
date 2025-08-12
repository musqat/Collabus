package com.muscat.Collabus.Todo.entity;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todo_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fileUrl; // 저장 위치 (로컬 경로)

  @Column(nullable = false)
  private String originalName; // 원래 파일명

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_id", nullable = false)
  private TodoWork work;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploader_id", nullable = false)
  private User uploader;
}
