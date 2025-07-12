package nova.mjs.util.s3;

import lombok.Getter;

/**
 * S3에 업로드되는 파일들의 도메인 구분을 위한 Enum이 구성된 파일입니다.
 * 각 항목은 고유한 prefix 경로를 가짐.
 */
@Getter
public enum S3DomainType {

    COMMUNITY_TEMP("boards/temp/"), // 삭제 에정
    COMMUNITY_POST("boards/"),
    PROFILE("member/profiles/"),
    DEPARTMENT_LOGO("departments/logos/"),
    EVENT("events/");

    private final String prefix;

    S3DomainType(String prefix) {
        this.prefix = prefix;
    }
}

