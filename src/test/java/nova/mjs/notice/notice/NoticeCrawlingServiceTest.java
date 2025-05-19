package nova.mjs.notice.notice;

import nova.mjs.MjsApplication;
import nova.mjs.notice.entity.Notice;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.service.NoticeCrawlingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MjsApplication.class)
@Transactional
class NoticeCrawlingServiceTest {

    @Autowired
    private NoticeCrawlingService noticeCrawlingService;

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 notice 테이블 정리
        noticeRepository.deleteAll();
    }

    @Test
    @DisplayName("공지 크롤링 실행 후 DB 저장 확인")
    void testFetchNoticesAndSave() {
        // given
        String category = "general";

        // when
        List<NoticeResponseDto> notices = noticeCrawlingService.fetchNotices(category);

        // then
        assertThat(notices).isNotEmpty(); // 최소 하나 이상 크롤링되었는지
        assertThat(noticeRepository.count()).isEqualTo(notices.size()); // DB에도 저장되었는지

        NoticeResponseDto first = notices.get(0);
        assertThat(first.getTitle()).isNotBlank();
        assertThat(first.getDate()).contains(".");
        assertThat(first.getLink()).startsWith("http");
        assertThat(first.getCategory()).isEqualTo("general");
    }
}
