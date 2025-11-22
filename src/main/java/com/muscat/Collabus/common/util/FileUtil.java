package com.muscat.Collabus.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public String saveFile(MultipartFile file) {
    try {
      if (file == null || file.isEmpty()) {
        throw new IOException("빈 파일입니다");
      }

      String originalName = file.getOriginalFilename();
      String safeOriginal = originalName == null ? "unknown" : Paths.get(originalName).getFileName().toString();
      String uniqueName = UUID.randomUUID() + "_" + safeOriginal;

      Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
      Files.createDirectories(uploadRoot);

      Path destination = uploadRoot.resolve(uniqueName).normalize();
      if (!destination.startsWith(uploadRoot)) {
        throw new IOException("잘못된 파일 경로입니다");
      }

      file.transferTo(destination.toFile());
      return destination.toString().replace('\\', '/');
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
    }
  }

  public void deleteFile(String path) {
    try {
      if (path == null || path.isBlank()) return;
      Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
      Path target = Paths.get(path).toAbsolutePath().normalize();
      if (!target.startsWith(uploadRoot)) {
        return;
      }
      Files.deleteIfExists(target);
    } catch (IOException ignored) {
      System.err.println("파일 삭제 실패: " + path);
    }
  }
}

