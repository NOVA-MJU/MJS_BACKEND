package nova.mjs.community.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;

import java.util.List;

@Data
@Builder
public class CommunityBoardRequest {
    private String title;               // 게시글 제목
    private String content;             // 게시글 내용
    private Boolean published;          // 게시글 공개 여부
    private List<String> contentImages; // 게시글 이미지 리스트

    // category는 무조건 FREE로 설정
    public CommunityBoard toEntity() {
        return CommunityBoard.create(
                this.title,
                this.content,
                CommunityCategory.FREE, // 무조건 FREE 설정
                this.published != null ? this.published : false,
                this.contentImages
        );
    }
}
