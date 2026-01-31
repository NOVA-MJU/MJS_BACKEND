package nova.mjs.domain.mentorship.program.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import nova.mjs.domain.mentorship.program.entity.MentoringProgram;

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
            /** 신청 기간 */
        private LocalDate applyStartDate;
        private LocalDate applyEndDate;

        /** 프로그램 진행 기간 */
        private LocalDate programStartDate;
        private LocalDate programEndDate;
        private int capacity;
        private String targetAudience;
        private String location;
        private String contact;
        private String preparation;

        /** 참여 멘토 이메일 */
        private List<String> mentorEmails;

        /**
         * DTO → Entity 변환
         * 멘토 엔티티는 Service에서 조회 후 주입
         */
        public MentoringProgram toEntity(List<MentorProfile> mentors) {
            return MentoringProgram.create(
                    title,
                    description,
                    applyStartDate,
                    applyEndDate,
                    programStartDate,
                    programEndDate,
                    capacity,
                    targetAudience,
                    location,
                    contact,
                    preparation,
                    mentors
            );
        }
    }



    /* ===============================
       프로그램 등록 응답
       =============================== */
    @Getter
    @Builder
    public static class CreateResponse {
        private UUID programUuid;
        private String title;
            /** 신청 기간 */
        private LocalDate applyStartDate;
        private LocalDate applyEndDate;

        /** 프로그램 진행 기간 */
        private LocalDate programStartDate;
        private LocalDate programEndDate;
        private int capacity;

        /** 참여 멘토 이메일 */
        private List<String> mentorEmails;

        public static CreateResponse fromEntity(
                MentoringProgram program,
                List<MentorProfile> mentors
        ) {
            return CreateResponse.builder()
                    .programUuid(program.getUuid())
                    .title(program.getTitle())
                    .applyStartDate(program.getApplyStartDate())
                    .applyEndDate(program.getApplyEndDate())
                    .programStartDate(program.getProgramStartDate())
                    .programEndDate(program.getProgramEndDate())
                    .capacity(program.getCapacity())
                    .mentorEmails(
                            mentors.stream()
                                    .map(mp -> mp.getMember().getEmail())
                                    .toList()
                    )
                    .build();
        }
    }

    /* ===============================
       프로그램 목록 조회
       =============================== */
    @Getter
    @Builder
    public static class SummaryResponse {

        private UUID programUuid;
        private String title;

        /** 신청 기간 */
        private LocalDate applyStartDate;
        private LocalDate applyEndDate;

        /** 프로그램 진행 기간 */
        private LocalDate programStartDate;
        private LocalDate programEndDate;

        // 모집인원
        private int capacity;
        // 문의처
        private String contact;

        public static SummaryResponse fromEntity(MentoringProgram program) {
            return SummaryResponse.builder()
                    .programUuid(program.getUuid())
                    .title(program.getTitle())
                    .applyStartDate(program.getApplyStartDate())
                    .applyEndDate(program.getApplyEndDate())
                    .programStartDate(program.getProgramStartDate())
                    .programEndDate(program.getProgramEndDate())
                    .capacity(program.getCapacity())
                    .contact(program.getContact())
                    .build();
        }
    }


    /* ===============================
       프로그램 상세 조회
       =============================== */
    @Getter
    @Builder
    public static class DetailResponse {

        private UUID programUuid;
        private String title;

        /** 신청 기간 */
        private LocalDate applyStartDate;
        private LocalDate applyEndDate;

        /** 프로그램 진행 기간 */
        private LocalDate programStartDate;
        private LocalDate programEndDate;

        private int capacity;
        // 현재 신청 인원
        private long currentApplicants;

        private String contact;
        private String targetAudience;
        private String location;

        private String description;
        private String preparation;

        private List<String> mentorEmails;

        public static DetailResponse fromEntity(MentoringProgram program, long currentApplicants) {
            return DetailResponse.builder()
                    .programUuid(program.getUuid())
                    .title(program.getTitle())
                    .description(program.getDescription())
                    .applyStartDate(program.getApplyStartDate())
                    .applyEndDate(program.getApplyEndDate())
                    .programStartDate(program.getProgramStartDate())
                    .programEndDate(program.getProgramEndDate())
                    .capacity(program.getCapacity())
                    .currentApplicants(currentApplicants)
                    .targetAudience(program.getTargetAudience())
                    .location(program.getLocation())
                    .contact(program.getContact())
                    .preparation(program.getPreparation())
                    .mentorEmails(
                            program.getMentors().stream()
                                    .map(mp -> mp.getMember().getEmail())
                                    .toList()
                    )
                    .build();
        }
    }

}
