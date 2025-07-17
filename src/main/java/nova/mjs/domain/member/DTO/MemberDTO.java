package nova.mjs.domain.member.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.entity.enumList.DepartmentName;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private UUID uuid;
    private String name;
    private String email;
    private String profileImageUrl;
    private String gender;
    private String nickname;
    private DepartmentName departmentName;
    private String studentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Member.Role role;

    /**
     * Member 엔티티를 MemberDTO로 변환하는 메서드 (응답용)
     */
    public static MemberDTO fromEntity(Member member) {
        return MemberDTO.builder()
                .uuid(member.getUuid())
                .name(member.getName())
                .email(member.getEmail())
                .gender(String.valueOf(member.getGender()))
                .nickname(member.getNickname())
                .departmentName(member.getDepartmentName())
                .studentNumber(member.getStudentNumber())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .role(member.getRole())
                .build();
    }
    /**
     * 회원가입 요청을 위한 DTO (내부 클래스)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberRegistrationRequestDTO {
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
        private String gender;

        @NotNull(message = "학과 정보는 필수입니다.")
        private DepartmentName departmentName;

        @Pattern(regexp = "\\d{8}", message = "학번은 정확히 8자리 숫자여야 합니다.")
        @NotNull(message = "학번은 필수입니다.")
        private String studentNumber;

        private String profileImageUrl;
    }



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberUpdateRequestDTO {
        private String name;
        private String nickname;
        private String gender; // "MALE", "FEMALE", "OTHERS"
        private DepartmentName departmentName;
        private String studentNumber;
        private String profileImageUrl;
    }


    /**
     * 비밀번호 변경 요청 DTO (내부 클래스)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PasswordRequestDTO {
        private String password;
        private String newPassword;
    }
}
