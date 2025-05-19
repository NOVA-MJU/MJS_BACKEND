package nova.mjs.community.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 게시글 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityBoardRequest {

    /** 게시글 제목 */
    private String title;

    /** 게시글 본문 */
    private String content;

    /** 이미지 임시 그룹 UUID */
    private UUID tempUuid;

    /** 게시글 공개 여부 */
    private Boolean published;

    /** 이미지 URL 리스트 */
    private List<String> contentImages;
}
