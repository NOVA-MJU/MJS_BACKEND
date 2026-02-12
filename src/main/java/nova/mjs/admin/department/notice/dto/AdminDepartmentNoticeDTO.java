package nova.mjs.admin.department.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminDepartmentNoticeDTO {

    @Data
    @Builder
    public static class Request{
        @NotBlank(message = "제목은 필수입니다.")
        private String title;               // 공지사항 제목
        private String content;             // 공지사항 내용
        private String contentPreview;             // 공지사항 내용
        private String thumbnailUrl;        // 썸네일 이미지
    }


    @Data
    @Builder
    public static class Response{
        private UUID uuid;
        private String title;
        private String content;
        private String contentPreview;
        private String thumbnailUrl;
        private LocalDateTime createAt;

        public static AdminDepartmentNoticeDTO.Response fromEntity(StudentCouncilNotice studentCouncilNotice){
            return AdminDepartmentNoticeDTO.Response.builder()
                    .uuid(studentCouncilNotice.getUuid())
                    .title(studentCouncilNotice.getTitle())
                    .content(studentCouncilNotice.getContent())
                    .contentPreview(studentCouncilNotice.getPreviewContent())
                    .thumbnailUrl(studentCouncilNotice.getThumbnailUrl())
                    .createAt(studentCouncilNotice.getCreatedAt())
                    .build();
        }
    }
    
    
}