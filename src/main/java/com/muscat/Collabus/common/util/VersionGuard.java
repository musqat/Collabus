package com.muscat.Collabus.common.util;

import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.CommonResponse;
import java.util.Objects;

/**
 * 수정 화면이 본 버전과 지금 버전이 같은지 확인한다.
 */
public final class VersionGuard {

  private VersionGuard() {
  }

  // 다르면 409
  public static void requireSame(Long current, Long requested) {
    if (!Objects.equals(current, requested)) {
      throw new BusinessException(CommonResponse.EDIT_CONFLICT);
    }
  }
}
