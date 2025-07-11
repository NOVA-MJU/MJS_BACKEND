package nova.mjs.domain.member.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.entity.enumList.College;
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
    private College college;
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
                .departmentName(member.getDepartmentName())
                .college(member.getCollege())
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
        private String name; // 실명
        private String password; // 비밀번호
        private String email; // 이메일 아이디
        private String nickname; // 닉네임
        private String gender; // 성별
        private DepartmentName departmentName;// 소속 학과
        private College college;
        private Integer studentNumber;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentCouncilRegistrationRequestDTO {
        private String email; // 이메일 아이디
        private String contactEmail;
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
