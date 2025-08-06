package nova.mjs.admin.department.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDepartmentNoticeRequestDTO {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;               // 공지사항 제목
    private String content;             // 공지사항 내용
    private String contentPreview;             // 공지사항 내용
    private String thumbnailUrl;        // 썸네일 이미지
}