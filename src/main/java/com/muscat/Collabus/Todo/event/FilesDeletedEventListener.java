package com.muscat.Collabus.Todo.event;

import com.muscat.Collabus.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilesDeletedEventListener {

  private final FileUtil fileUtil;

  // 커밋 이후에만 실제 파일을 지운다
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onFilesDeleted(FilesDeletedEvent event) {
    for (String fileUrl : event.fileUrls()) {
      try {
        fileUtil.deleteFile(fileUrl);
      } catch (Exception e) {
        // 파일 하나가 실패해도 나머지 정리는 계속한다
        log.warn("첨부 파일 삭제 실패: {}", fileUrl, e);
      }
    }
  }
}
