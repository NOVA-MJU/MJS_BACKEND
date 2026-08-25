package nova.mjs.domain.thingo.community.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 운영자 키워드 일괄 삭제 결과 응답 (L2).
 *
 * - 검색어(keyword)와 실제 삭제된 건수(deletedCount), 삭제된 게시글 요약을 담는다.
 * - 삭제 전 미리보기(조회) 응답에도 동일 구조를 재사용해 "무엇이 지워질지"를 그대로 보여준다.
 */
@Getter
@Builder
public class CommunityKeywordDeletionResponse {

    private String keyword;
    private int deletedCount;
    private List<DeletedBoard> boards;

    public static CommunityKeywordDeletionResponse of(String keyword, List<CommunityBoard> boards) {
        return CommunityKeywordDeletionResponse.builder()
                .keyword(keyword)
                .deletedCount(boards.size())
                .boards(boards.stream().map(DeletedBoard::from).toList())
                .build();
    }

    @Getter
    @Builder
    public static class DeletedBoard {
        private UUID uuid;
        private String title;
        private String author;
        private LocalDateTime createdAt;

        public static DeletedBoard from(CommunityBoard e) {
            return DeletedBoard.builder()
                    .uuid(e.getUuid())
                    .title(e.getTitle())
                    .author(e.getAuthor() != null ? e.getAuthor().getNickname() : "Unknown")
                    .createdAt(e.getCreatedAt())
                    .build();
        }
    }
}
