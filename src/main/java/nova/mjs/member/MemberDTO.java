package nova.mjs.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String gender;
    private String nickname;
    private String department;
    private Integer studentNumber;
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
                .department(member.getDepartment())
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
    public static class MemberRequestDTO {
        private String name;
        private String password;
        private String email;
        private String gender;
        private String nickname;
        private String department;
        private Integer studentNumber;
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
