package nova.mjs.mentor.profile.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.mentor.profile.dto.MentorProfileDTO;

/**
 * 회원가입(회원+멘토 프로필) 통합 요청 DTO
 * - 컨트롤러에서 이 DTO 하나만 받으면 됨
 * - 서비스 내부에서 기존 MemberDTO / MentorProfileDTO 로 변환하여 재사용
 */
public class MentorRegistrationDTO {

    @Getter
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        // ===== Member =====
        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;

        @NotNull(message = "성별은 필수입니다.")
        private String gender; // "MALE", "FEMALE", "OTHERS"

        @NotNull(message = "학과 정보는 필수입니다.")
        private DepartmentName departmentName;

        @Pattern(regexp = "\\d{8}", message = "학번은 정확히 8자리 숫자여야 합니다.")
        @NotNull(message = "학번은 필수입니다.")
        private String studentNumber;

        private String profileImageUrl; // 선택

        // ===== Mentor =====
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

        /** 사실 여부 검증 - 선택 (null이면 false 처리) */
        private Boolean isVerified;

        /** 추가 노하우들 - 선택 */
        private String resumeTips;
        private String interviewTips;
        private String portfolioTips;
        private String networkingTips;

        // ===== 변환 헬퍼 (기존 로직 재사용) =====
        public MemberDTO.MemberRegistrationRequestDTO toMemberReq() {
            return MemberDTO.MemberRegistrationRequestDTO.builder()
                    .name(name)
                    .email(email)
                    .password(password)
                    .nickname(nickname)
                    .gender(gender)
                    .departmentName(departmentName)
                    .studentNumber(studentNumber)
                    .profileImageUrl(profileImageUrl)
                    .build();
        }

        public MentorProfileDTO.Request toMentorReq() {
            return MentorProfileDTO.Request.builder()
                    .phoneNumber(phoneNumber)
                    .graduationYear(graduationYear)
                    .careerYear(careerYear)
                    .workplace(workplace)
                    .jobTitle(jobTitle)
                    .description(description)
                    .isVerified(isVerified)
                    .resumeTips(resumeTips)
                    .interviewTips(interviewTips)
                    .portfolioTips(portfolioTips)
                    .networkingTips(networkingTips)
                    .build();
        }
    }
}
