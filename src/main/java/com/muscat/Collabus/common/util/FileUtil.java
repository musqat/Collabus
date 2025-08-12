package com.muscat.Collabus.common.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileUtil {

  @Value("${file.upload-dir}")
  private String uploadDir;

  /**
   * 파일 저장 후 상대 경로 리턴
   */
  public String saveFile(MultipartFile file) {
    try {
      String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      File destination = new File(uploadDir, uniqueName);

      // 디렉토리 없으면 생성
      if (!destination.getParentFile().exists()) {
        boolean created = destination.getParentFile().mkdirs();
        if (!created) {
          throw new IOException("디렉토리 생성 실패: " + destination.getParentFile().getPath());
        }
      }

      file.transferTo(destination);

      return uploadDir + "/" + uniqueName;
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
    }
  }

  /**
   * 파일 삭제
   */
  public void deleteFile(String path) {
    File file = new File(path);
    if (file.exists() && !file.delete()) {
      System.err.println(" 파일 삭제 실패: " + path);
    }
  }
}
