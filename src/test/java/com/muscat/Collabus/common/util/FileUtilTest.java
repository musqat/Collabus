package com.muscat.Collabus.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.muscat.Collabus.common.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("FileUtil 단위 테스트")
class FileUtilTest {

  @TempDir
  Path uploadRoot;

  private FileUtil fileUtil;

  @BeforeEach
  void setUp() {
    fileUtil = new FileUtil();
    ReflectionTestUtils.setField(fileUtil, "uploadDir", uploadRoot.toString());
  }

  @Test
  @DisplayName("허용된 확장자는 저장된다")
  void saveFile_AllowedExtension() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "report.pdf", "application/pdf", "hello".getBytes());

    String saved = fileUtil.saveFile(file);

    assertThat(saved).endsWith("_report.pdf");
    // 원본 파일명 앞에 UUID 가 붙어 덮어쓰기를 막는다
    assertThat(Path.of(saved).getFileName().toString()).isNotEqualTo("report.pdf");
    assertThat(Files.exists(Path.of(saved))).isTrue();
  }

  @Test
  @DisplayName("허용되지 않은 확장자는 거부된다")
  void saveFile_Fail_DisallowedExtension() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "malware.exe", "application/octet-stream", "MZ".getBytes());

    assertThatThrownBy(() -> fileUtil.saveFile(file))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("확장자가 없으면 거부된다")
  void saveFile_Fail_NoExtension() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "noext", "application/octet-stream", "x".getBytes());

    assertThatThrownBy(() -> fileUtil.saveFile(file))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("빈 파일은 거부된다")
  void saveFile_Fail_Empty() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "empty.txt", "text/plain", new byte[0]);

    assertThatThrownBy(() -> fileUtil.saveFile(file))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("파일명에 경로가 섞여 있어도 업로드 루트를 벗어나지 않는다")
  void saveFile_StripsPathTraversal() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "../../etc/passwd.txt", "text/plain", "x".getBytes());

    String saved = fileUtil.saveFile(file);

    assertThat(Path.of(saved).normalize()).startsWith(uploadRoot);
    assertThat(Path.of(saved).getFileName().toString()).endsWith("_passwd.txt");
  }

  @Test
  @DisplayName("업로드 루트 밖의 파일은 읽을 수 없다")
  void loadFile_Fail_OutsideRoot(@TempDir Path outside) throws IOException {
    Path secret = outside.resolve("secret.txt");
    Files.writeString(secret, "top secret");

    assertThatThrownBy(() -> fileUtil.loadFile(secret.toString()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("업로드 루트 밖의 파일은 삭제되지 않는다")
  void deleteFile_Ignores_OutsideRoot(@TempDir Path outside) throws IOException {
    Path secret = outside.resolve("secret.txt");
    Files.writeString(secret, "top secret");

    fileUtil.deleteFile(secret.toString());

    assertThat(Files.exists(secret)).isTrue();
  }

  @Test
  @DisplayName("저장한 파일을 다시 읽고 삭제할 수 있다")
  void saveThenLoadAndDelete() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
        "file", "note.txt", "text/plain", "hello".getBytes());
    String saved = fileUtil.saveFile(file);

    Resource resource = fileUtil.loadFile(saved);
    assertThat(resource.getContentAsByteArray()).isEqualTo("hello".getBytes());

    fileUtil.deleteFile(saved);
    assertThat(Files.exists(Path.of(saved))).isFalse();
  }

  @Test
  @DisplayName("null 파일은 저장할 수 없다")
  void saveFile_Fail_Null() {
    assertThatThrownBy(() -> fileUtil.saveFile(null))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("원본 이름이 없으면 unknown 으로 다룬다")
  void saveFile_Fail_NullOriginalName() {
    MockMultipartFile file = new MockMultipartFile("file", null, "text/plain", "a".getBytes());

    // unknown 에는 확장자가 없어 허용 목록 검사에서 막힌다
    assertThatThrownBy(() -> fileUtil.saveFile(file))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("빈 경로는 읽을 수 없다")
  void loadFile_Fail_BlankPath() {
    assertThatThrownBy(() -> fileUtil.loadFile("   "))
        .isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> fileUtil.loadFile(null))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("루트 안이라도 없는 파일은 읽을 수 없다")
  void loadFile_Fail_Missing() {
    String missing = uploadRoot.resolve("none.txt").toString();

    assertThatThrownBy(() -> fileUtil.loadFile(missing))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("빈 경로를 지워도 조용히 넘어간다")
  void deleteFile_BlankPath() {
    assertThatCode(() -> fileUtil.deleteFile(null)).doesNotThrowAnyException();
    assertThatCode(() -> fileUtil.deleteFile("")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("없는 파일을 지워도 조용히 넘어간다")
  void deleteFile_Missing() {
    String missing = uploadRoot.resolve("none.txt").toString();

    assertThatCode(() -> fileUtil.deleteFile(missing)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("대문자 확장자도 허용한다")
  void saveFile_UppercaseExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "보고서.PNG", "image/png", "a".getBytes());

    assertThatCode(() -> fileUtil.saveFile(file)).doesNotThrowAnyException();
  }
}
