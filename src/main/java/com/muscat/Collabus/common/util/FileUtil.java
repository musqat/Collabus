package com.muscat.Collabus.common.util;

import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.TodoResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtil {

  // 실행 가능한 파일이 업로드 디렉터리에 저장되는 것을 막기 위한 허용 목록
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      "png", "jpg", "jpeg", "gif", "webp", "svg",
      "pdf", "txt", "md", "csv",
      "doc", "docx", "xls", "xlsx", "ppt", "pptx",
      "zip", "hwp", "hwpx");

  @Value("${file.upload-dir}")
  private String uploadDir;

  public String saveFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(TodoResponse.FILE_EMPTY);
    }

    String originalName = file.getOriginalFilename();
    String safeOriginal = originalName == null ? "unknown" : Paths.get(originalName).getFileName().toString();
    validateExtension(safeOriginal);

    String uniqueName = UUID.randomUUID() + "_" + safeOriginal;

    try {
      Path uploadRoot = uploadRoot();
      Files.createDirectories(uploadRoot);

      Path destination = uploadRoot.resolve(uniqueName).normalize();
      if (!destination.startsWith(uploadRoot)) {
        throw new BusinessException(TodoResponse.FILE_EXTENSION_NOT_ALLOWED);
      }

      file.transferTo(destination.toFile());
      return destination.toString().replace('\\', '/');
    } catch (IOException e) {
      log.error("파일 저장 실패: {}", uniqueName, e);
      throw new BusinessException(TodoResponse.FILE_SAVE_FAILED);
    }
  }

  /**
   * 저장된 파일을 읽어 반환한다. 업로드 루트를 벗어난 경로는 거부한다.
   */
  public Resource loadFile(String path) {
    Path target = resolveWithinRoot(path);
    if (target == null || !Files.isReadable(target)) {
      throw new BusinessException(TodoResponse.FILE_NOT_FOUND);
    }
    try {
      return new UrlResource(target.toUri());
    } catch (IOException e) {
      log.error("파일 읽기 실패: {}", path, e);
      throw new BusinessException(TodoResponse.FILE_READ_FAILED);
    }
  }

  public void deleteFile(String path) {
    Path target = resolveWithinRoot(path);
    if (target == null) {
      return;
    }
    try {
      Files.deleteIfExists(target);
    } catch (IOException e) {
      log.warn("파일 삭제 실패: {}", path, e);
    }
  }

  private void validateExtension(String fileName) {
    int dot = fileName.lastIndexOf('.');
    String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(TodoResponse.FILE_EXTENSION_NOT_ALLOWED);
    }
  }

  private Path uploadRoot() {
    return Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  /**
   * 업로드 루트 하위 경로만 반환한다. 루트를 벗어나거나 값이 비어 있으면 null
   */
  private Path resolveWithinRoot(String path) {
    if (path == null || path.isBlank()) {
      return null;
    }
    Path uploadRoot = uploadRoot();
    Path target = Paths.get(path).toAbsolutePath().normalize();
    return target.startsWith(uploadRoot) ? target : null;
  }
}
