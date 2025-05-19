package nova.util.entity.community;

import com.navercorp.fixturemonkey.FixtureMonkey;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.util.entity.member.MemberTestData;

import java.util.List;
import java.util.UUID;

public class CommunityBoardTestData {

    private static final FixtureMonkey fixture = FixtureMonkey.create();

    public static CommunityBoard sample() {
        return fixture.giveMeBuilder(CommunityBoard.class)
                .set("uuid", UUID.randomUUID())
                .set("title", "샘플 게시글 제목")
                .set("content", "이것은 샘플 게시글 내용입니다.")
                .set("previewContent", "이것은 샘플...")
                .set("viewCount", 0)
                .set("likeCount", 0)
                .set("published", true)
                .set("category", CommunityCategory.FREE)
                .set("contentImages", List.of("https://image.com/img1.jpg", "https://image.com/img2.jpg"))
                .set("author", MemberTestData.sample()) // MemberTestData 유틸을 따로 만드세요
                .sample();
    }

    public static CommunityBoard sampleWithCategory(CommunityCategory category) {
        CommunityBoard board = sample();
        return CommunityBoard.builder()
                .uuid(board.getUuid())
                .title(board.getTitle())
                .content(board.getContent())
                .previewContent(board.getPreviewContent())
                .viewCount(board.getViewCount())
                .likeCount(board.getLikeCount())
                .published(board.getPublished())
                .publishedAt(board.getPublishedAt())
                .contentImages(board.getContentImages())
                .category(category)
                .author(board.getAuthor())
                .build();
    }
}

