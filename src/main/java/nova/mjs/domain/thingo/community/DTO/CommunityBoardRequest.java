package nova.mjs.domain.thingo.community.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.thingo.community.entity.enumList.CommunityCategory;

@Data
@Builder
public class CommunityBoardRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;               // 게시글 제목
    @NotBlank(message = "내용은 필수입니다.")
    private String content;             // 게시글 내용
    private String contentPreview;      // 게시글 요약(프론트에서 전달)
    private Boolean published;          // 게시글 공개 여부
    private CommunityCategory communityCategory;
}
