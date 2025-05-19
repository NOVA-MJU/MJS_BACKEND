package nova.mjs.community;

import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DisplayName("CommunityBoard 엔티티 테스트")
class CommunityBoardTest {

    @Test
    @DisplayName("CommunityBoard 빌더 테스트")
    void testCommunityBoardBuilder() {
        UUID uuid = UUID.randomUUID();
        Member member = Member.builder().nickname("작성자").build();

        CommunityBoard board = CommunityBoard.builder()
                .uuid(uuid)
                .title("제목")
                .content("내용")
                .category(CommunityCategory.FREE)
                .published(true)
                .publishedAt(LocalDateTime.of(2025, 5, 18, 10, 0))
                .viewCount(0)
                .likeCount(0)
                .author(member)
                .contentImages(new ArrayList<>())
                .build();

        assertThat(board.getUuid()).isEqualTo(uuid);
        assertThat(board.getTitle()).isEqualTo("제목");
        assertThat(board.getContent()).isEqualTo("내용");
        assertThat(board.getCategory()).isEqualTo(CommunityCategory.FREE);
        assertThat(board.getPublished()).isTrue();
        assertThat(board.getPublishedAt()).isEqualTo(LocalDateTime.of(2025, 5, 18, 10, 0));
        assertThat(board.getViewCount()).isZero();
        assertThat(board.getLikeCount()).isZero();
        assertThat(board.getAuthor().getNickname()).isEqualTo("작성자");
        assertThat(board.getContentImages()).isEmpty();
    }

    @Test
    @DisplayName("CommunityBoard 업데이트 메서드 테스트")
    void testUpdate() {
        Member member = Member.builder().nickname("작성자").build();

        CommunityBoard board = CommunityBoard.create("초기제목", "초기내용", CommunityCategory.FREE, false, List.of("a.png"), member);
        board.update("수정된 제목", "수정된 내용", true, List.of("b.png", "c.png"));

        assertThat(board.getTitle()).isEqualTo("수정된 제목");
        assertThat(board.getContent()).isEqualTo("수정된 내용");
        assertThat(board.getPublished()).isTrue();
        assertThat(board.getContentImages()).containsExactly("b.png", "c.png");
        assertThat(board.getPublishedAt()).isNotNull(); // true로 바뀌었으므로 timestamp 갱신됨
    }
}
