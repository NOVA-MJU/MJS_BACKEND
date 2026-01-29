package nova.mjs.domain.mentorship.application.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.mentorship.application.entity.MentorshipApplication;
import nova.mjs.domain.thingo.member.DTO.MemberDTO;
import nova.mjs.domain.thingo.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MentorshipApplicationDTO {

    /* ===============================
       신청 생성 요청
       =============================== */
    @Getter
    public static class CreateRequest {

        private UUID mentorUuid;

        private String name;
        private String studentNumber;
        private String department;
        private String grade;
        private String phone;
        private String email;

        private LocalDate expectedGraduation;
        private String topic;

        private boolean privacyAgreement;

        /** DTO → Entity */
        public MentorshipApplication toEntity(Member applicant, Member mentor) {
            return MentorshipApplication.create(
                    applicant,
                    mentor,
                    name,
                    studentNumber,
                    department,
                    grade,
                    phone,
                    email,
                    expectedGraduation,
                    topic,
                    privacyAgreement
            );
        }
    }

    /* ===============================
       신청 생성 응답
       =============================== */
    @Getter
    @Builder
    public static class CreateResponse {

        private UUID applicationUuid;
        private String status;

        public static CreateResponse fromEntity(MentorshipApplication application) {
            return CreateResponse.builder()
                    .applicationUuid(application.getUuid())
                    .status(application.getStatus().name())
                    .build();
        }
    }

    /* ===============================
       신청 목록 조회 (요약)
       =============================== */
    @Getter
    @Builder
    public static class SummaryResponse {

        private UUID applicationUuid;
        private String status;

        // 상대방 정보(목록에서 카드에 보여줄 용도)
        private UUID applicantUuid;
        private String applicantEmail;

        private UUID mentorUuid;
        private String mentorEmail;

        private LocalDateTime createdAt;

        public static SummaryResponse fromEntity(MentorshipApplication app) {
            return SummaryResponse.builder()
                    .applicationUuid(app.getUuid())
                    .status(app.getStatus().name())
                    .applicantUuid(app.getApplicant().getUuid())
                    .applicantEmail(app.getApplicant().getEmail())
                    .mentorUuid(app.getMentor().getUuid())
                    .mentorEmail(app.getMentor().getEmail())
                    .createdAt(app.getCreatedAt())
                    .build();
        }
    }

    /* ===============================
       신청 상세 조회
       =============================== */
    @Getter
    @Builder
    public static class DetailResponse {

        private UUID applicationUuid;
        private String status;

        // 신청자/멘토 식별
        private UUID applicantUuid;
        private String applicantEmail;

        private UUID mentorUuid;
        private String mentorEmail;

        // 신청 폼(스냅샷)
        private String name;
        private String studentNumber;
        private String department;
        private String grade;
        private String phone;
        private String email;

        private String topic;
        private boolean privacyAgreement;

        private LocalDateTime createdAt;

        public static DetailResponse fromEntity(MentorshipApplication app) {
            return DetailResponse.builder()
                    .applicationUuid(app.getUuid())
                    .status(app.getStatus().name())
                    .applicantUuid(app.getApplicant().getUuid())
                    .applicantEmail(app.getApplicant().getEmail())
                    .mentorUuid(app.getMentor().getUuid())
                    .mentorEmail(app.getMentor().getEmail())
                    .name(app.getName())
                    .studentNumber(app.getStudentNumber())
                    .department(app.getDepartment())
                    .grade(app.getGrade())
                    .phone(app.getPhone())
                    .email(app.getEmail())
                    .topic(app.getTopic())
                    .privacyAgreement(app.isPrivacyAgreement())
                    .createdAt(app.getCreatedAt())
                    .build();
        }
    }
}
