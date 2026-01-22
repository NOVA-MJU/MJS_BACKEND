package nova.mjs.domain.mentorship.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MentorAdminDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private String name;
        private String email;
        private String password;

        private String displayName;
        private String departmentName;
        private String introduction;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class Response {
        private Long memberId;
        private Long mentorProfileId;
        private String email;
        private String displayName;
    }
}
