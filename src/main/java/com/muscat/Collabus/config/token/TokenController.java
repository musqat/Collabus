package com.muscat.Collabus.config.token;

import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.enums.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

  private final TokenReissueService tokenReissueService;

  @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 Access Token을 재발급합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ResponseDto> refreshToken(@RequestBody @Valid RefreshRequestDto request) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        tokenReissueService.reissue(request.getRefreshToken())));
  }
}
