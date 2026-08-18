package com.muscat.Collabus.Todo.event;

import java.util.List;

/**
 * 첨부 파일 레코드가 지워진 뒤 발행되는 이벤트
 * 디스크 삭제는 되돌릴 수 없으므로 트랜잭션이 커밋된 뒤에만 수행한다.
 */
public record FilesDeletedEvent(List<String> fileUrls) {

}
