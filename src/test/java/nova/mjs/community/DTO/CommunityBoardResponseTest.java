package nova.mjs.community.DTO;

import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityBoardResponseTest {

    @Test
    @DisplayName("CommunityBoard 엔티티를 DTO로 변환 - fromEntity 테스트")
    void testFromEntity() {
        // given
        UUID uuid = UUID.randomUUID();
        Member author = Member.builder()
                .nickname("작성자닉네임")
                .build();

        CommunityBoard board = CommunityBoard.builder()
                .uuid(uuid)
                .title("테스트 제목")
                .content("본문 내용")
                .contentImages(List.of("img1.png", "img2.png"))
                .viewCount(123)
                .published(true)
                .publishedAt(LocalDateTime.of(2025, 5, 18, 10, 0))
                .author(author)
                .build();

        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.of(2025, 5, 18, 9, 0));
        ReflectionTestUtils.setField(board, "updatedAt", LocalDateTime.of(2025, 5, 18, 9, 30));

        int likeCount = 10;
        int commentCount = 5;
        boolean isLiked = true;

        // when
        CommunityBoardResponse response = CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);

        // then
        assertThat(response.getUuid()).isEqualTo(uuid);
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("본문 내용");
        assertThat(response.getContentImages()).containsExactly("img1.png", "img2.png");
        assertThat(response.getViewCount()).isEqualTo(123);
        assertThat(response.getPublished()).isTrue();
        assertThat(response.getPublishedAt()).isEqualTo(LocalDateTime.of(2025, 5, 18, 10, 0));
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 5, 18, 9, 0));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 5, 18, 9, 30));
        assertThat(response.getLikeCount()).isEqualTo(10);
        assertThat(response.getCommentCount()).isEqualTo(5);
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getAuthor()).isEqualTo("작성자닉네임");
    }
}
