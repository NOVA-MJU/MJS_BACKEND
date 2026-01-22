package nova.mjs.domain.mentorship.program.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProgramAdminDTO {

    /* ===============================
       프로그램 등록 요청
       =============================== */
    @Getter
    public static class CreateRequest {
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;

        private int capacity;
        private String targetAudience;
        private String location;
        private String contact;
        private String preparation;

        /** 참여 멘토 이메일 */
        private List<String> mentorEmails;
    }

    /* ===============================
       프로그램 등록 응답
       =============================== */
    @Getter
    @Builder
    public static class CreateResponse {
        private UUID programUuid;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private int capacity;

        /** 참여 멘토 이메일 */
        private List<String> mentorEmails;
    }

    /* ===============================
       프로그램 목록 조회
       =============================== */
    @Getter
    @Builder
    public static class SummaryResponse {
        private UUID programUuid;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private int mentorCount;
    }

    /* ===============================
       프로그램 상세 조회
       =============================== */
    @Getter
    @Builder
    public static class DetailResponse {
        private UUID programUuid;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private int capacity;
        private String targetAudience;
        private String location;
        private String contact;
        private String preparation;

        /** 참여 멘토 이메일 */
        private List<String> mentorEmails;
    }
}
