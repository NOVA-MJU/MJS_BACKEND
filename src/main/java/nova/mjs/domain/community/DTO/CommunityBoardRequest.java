package nova.mjs.domain.community.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommunityBoardRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;               // 게시글 제목
    @NotBlank(message = "내용은 필수입니다.")
    private String content;             // 게시글 내용
    @NotNull(message = "게시 여부는 필수입니다.")
    private Boolean published;          // 게시글 공개 여부
}
