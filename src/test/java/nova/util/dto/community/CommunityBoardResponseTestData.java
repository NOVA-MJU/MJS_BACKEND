package nova.util.dto.community;

import com.navercorp.fixturemonkey.FixtureMonkey;
import nova.mjs.community.DTO.CommunityBoardResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CommunityBoardResponseTestData {

    private static final FixtureMonkey fixture = FixtureMonkey.create();

    // SummaryDTO

    public static CommunityBoardResponse.SummaryDTO randomSummarySample() {
        return fixture.giveMeOne(CommunityBoardResponse.SummaryDTO.class);
    }

    public static CommunityBoardResponse.SummaryDTO sampleSummary() {
        return fixture.giveMeBuilder(CommunityBoardResponse.SummaryDTO.class)
                .set("uuid", UUID.randomUUID())
                .set("title", "요약 게시글 제목")
                .set("previewContent", "미리보기 요약")
                .set("contentImages", List.of("https://img.com/1.jpg"))
                .set("viewCount", 12)
                .set("published", true)
                .set("publishedAt", LocalDateTime.now())
                .set("createdAt", LocalDateTime.now())
                .set("updatedAt", LocalDateTime.now())
                .set("likeCount", 5)
                .set("commentCount", 3)
                .set("author", "작성자")
                .set("liked", true)
                .sample();
    }

    // DetailDTO

    public static CommunityBoardResponse.DetailDTO randomDetailSample() {
        return fixture.giveMeOne(CommunityBoardResponse.DetailDTO.class);
    }

    public static CommunityBoardResponse.DetailDTO sampleDetail() {
        return fixture.giveMeBuilder(CommunityBoardResponse.DetailDTO.class)
                .set("uuid", UUID.randomUUID())
                .set("title", "상세 게시글 제목")
                .set("content", "이것은 게시글의 본문 내용입니다.")
                .set("contentImages", List.of("https://img.com/1.jpg", "https://img.com/2.jpg"))
                .set("viewCount", 100)
                .set("published", true)
                .set("publishedAt", LocalDateTime.now())
                .set("createdAt", LocalDateTime.now())
                .set("updatedAt", LocalDateTime.now())
                .set("likeCount", 23)
                .set("commentCount", 4)
                .set("author", "작성자 닉네임")
                .set("liked", false)
                .sample();
    }
}
