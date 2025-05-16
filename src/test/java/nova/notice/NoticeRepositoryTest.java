package nova.notice;

import nova.mjs.MjsApplication;
import nova.mjs.notice.entity.Notice;
import nova.mjs.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MjsApplication.class)
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("공지사항 저장 및 조회 테스트")
    void saveAndFindNotice() {
        // given
        Notice notice = Notice.createNotice("공지사항 제목", "2025-05-16", "학사", "https://example.com");

        // when
        Notice saved = noticeRepository.save(notice);
        Optional<Notice> found = noticeRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("공지사항 제목");
        assertThat(found.get().getDate()).isEqualTo("2025-05-16");
        assertThat(found.get().getCategory()).isEqualTo("학사");
        assertThat(found.get().getLink()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("공지사항 전체 조회 테스트")
    void findAllNotices() {
        // given
        Notice notice1 = Notice.createNotice("공지1", "2025-05-16", "학사", "https://link1.com");
        Notice notice2 = Notice.createNotice("공지2", "2025-05-15", "일반", "https://link2.com");
        noticeRepository.saveAll(List.of(notice1, notice2));

        // when
        List<Notice> allNotices = noticeRepository.findAll();

        // then
        assertThat(allNotices).hasSize(2);
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() {
        // given
        Notice notice = Notice.createNotice("삭제 테스트", "2025-05-16", "장학", "https://delete.com");
        Notice saved = noticeRepository.save(notice);

        // when
        noticeRepository.deleteById(saved.getId());

        // then
        Optional<Notice> deleted = noticeRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
}
