package nova.mjs.mentor.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import nova.mjs.mentor.profile.entity.Mentor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mentor 프로필 요청 DTO
 * - Request: 생성용(필수/선택 혼재)
 * - Update : 부분수정용(null인 필드는 유지)
 *
 * skills / careers / portfolios 는 별도 Command DTO로 다루는 것을 권장.
 */
public class MentorProfileDTO {

    /**
     * 생성 요청 DTO
     * - uuid 미지정 시 엔티티에서 Random UUID 생성
     * - graduationYear / careerYear 는 primitive(int)로 필수 처리
     */
    @Getter
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        // == MEMBER ENTITY == //
        private String name;

        private String email;

        private String password;

        // == Mentor ENTITY == //

        /** 연락처 (예: +49-160-1234-5678 / 010-1234-5678) */
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "전화번호 형식이 올바르지 않습니다.")
        private String phoneNumber;

        /** 졸업연도 (예: 2022) */
        @Min(1900)
        private int graduationYear;

        /** 총 경력 연차 (예: 3) */
        @Min(0)
        private int careerYear;

        /** 현재 직장(회사명) - 선택 */
        @Size(max = 128)
        private String workplace;

        /** 현재 직무/포지션 - 선택 */
        @Size(max = 128)
        private String jobTitle;

        /** 프로필 소개 - 필수 (긴 텍스트 허용) */
        @NotBlank
        private String description;

        /** 사실 여부 검증 - 선택 (null 이면 false로 처리) */
        private Boolean isVerified;

        /** 추가 노하우들 - 선택 */
        private String resumeTips;
        private String interviewTips;
        private String portfolioTips;
        private String networkingTips;
    }

    @Getter
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long mentorId;
        private UUID memberUuid;
        private String phoneNumber;
        private int graduationYear;
        private int careerYear;
        private String workplace;
        private String jobTitle;
        private String description;
        private boolean verified;
        private List<String> skills;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Mentor m) {
            return Response.builder()
                    .mentorId(m.getId())
                    .memberUuid(m.getMember().getUuid())
                    .phoneNumber(m.getPhoneNumber())        // 엔티티에서 숫자만 저장됨
                    .graduationYear(m.getGraduationYear())
                    .careerYear(m.getCareerYear())
                    .workplace(m.getWorkplace())
                    .jobTitle(m.getJobTitle())
                    .description(m.getDescription())
                    .verified(m.isVerified())
                    .skills(m.getSkills())
                    .createdAt(m.getCreatedAt())
                    .updatedAt(m.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 부분 수정 요청 DTO
     * - null 전달 시 해당 필드는 변경하지 않음
     */
    @Getter
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        @Size(max = 32)
        @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "전화번호 형식이 올바르지 않습니다.")
        private String phoneNumber;

        @Min(1900)
        private Integer graduationYear;

        @Min(0)
        private Integer careerYear;

        @Size(max = 128)
        private String workplace;

        @Size(max = 128)
        private String jobTitle;

        private String  description;

        private Boolean isVerified;

        private String  resumeTips;
        private String  interviewTips;
        private String  portfolioTips;
        private String  networkingTips;
    }
}
