package com.example.zero.healthcare.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === Common ===
    INVALID_REQUEST(400, "INVALID_REQUEST", "잘못된 요청입니다."),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "서버 오류가 발생했습니다."),
    DATA_INTEGRITY_VIOLATION(400, "INVALID_REQUEST", "데이터 제약 조건 위반입니다."),

    // === User ===
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    UNAUTHORIZED(401, "UNAUTHORIZED", "인증이 필요합니다."),

    // === Journal ===
    JOURNAL_NOT_FOUND(404, "JOURNAL_NOT_FOUND", "존재하지 않는 일지입니다."),
    POST_ALREADY_RECORDED(409, "POST_ALREADY_RECORDED", "이미 운동 후 기록이 완료된 일지입니다."),

    // === Folder ===
    FOLDER_NOT_FOUND(404, "FOLDER_NOT_FOUND", "폴더를 찾을 수 없습니다."),
    FOLDER_FULL(409, "FOLDER_FULL", "폴더 정원이 가득 찼습니다."),
    FOLDER_CLOSED(400, "FOLDER_CLOSED", "닫힌 폴더입니다."),
    ALREADY_MEMBER(400, "ALREADY_MEMBER", "이미 해당 폴더의 활성 멤버입니다."),
    ALREADY_LEFT(400, "ALREADY_LEFT", "이미 나간 상태입니다."),
    INVALID_FOLDER_NAME(400, "INVALID_FOLDER_NAME", "폴더명 형식 또는 길이가 올바르지 않습니다."),
    INVALID_TOKEN(400, "INVALID_TOKEN", "유효하지 않은 초대 토큰입니다."),
    INVALID_CURSOR(400, "INVALID_CURSOR", "커서 값이 올바르지 않습니다."),
    FORBIDDEN(403, "FORBIDDEN", "해당 폴더에 대한 접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
