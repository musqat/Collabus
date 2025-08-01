package com.muscat.Collabus.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class BaseEntity {

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @LastModifiedBy
  private String updatedBy;

  @PrePersist
  public void prePersist() {
    if (this.updatedAt == null && this.createdAt != null) {
      this.updatedAt = this.createdAt;
    }
    if (this.updatedBy == null && this.createdBy != null) {
      this.updatedBy = this.createdBy;
    }
  }

}
