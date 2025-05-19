package nova.mjs.notice.notice;

import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.exception.NoticeNotFoundExcetion;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.notice.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

class NoticeServiceTest {

    private NoticeRepository noticeRepository;
    private NoticeService noticeService;

    @BeforeEach
    void setUp() {
        noticeRepository = mock(NoticeRepository.class);
        noticeService = new NoticeService(noticeRepository);
    }

    @Test
    @DisplayName("공지사항 조회 성공 - 카테고리 & 연도 기준")
    void testGetNotices_Success() {
        // given
        String category = "general";
        Integer year = 2024;
        int page = 0;
        int size = 10;
        String sort = "desc";

        List<NoticeResponseDto> mockResult = List.of(
                new NoticeResponseDto("제목1", "2024.03.01", "general", "https://link1"),
                new NoticeResponseDto("제목2", "2024.03.02", "general", "https://link2")
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

        given(noticeRepository.findNoticesByCategoryAndYear(category, year, pageable))
                .willReturn(mockResult);

        // when
        List<NoticeResponseDto> result = noticeService.getNotices(category, year, page, size, sort);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("제목1");
    }

    @Test
    @DisplayName("공지사항 조회 실패 - 카테고리 없음")
    void testGetNotices_NoCategory() {
        assertThatThrownBy(() -> noticeService.getNotices(null, 2024, 0, 10, "desc"))
                .isInstanceOf(NoticeNotFoundExcetion.class);
    }

    @Test
    @DisplayName("공지사항 조회 실패 - 결과 없음")
    void testGetNotices_EmptyResult() {
        // given
        String category = "general";
        Integer year = 2024;
        int page = 0;
        int size = 10;
        String sort = "desc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

        given(noticeRepository.findNoticesByCategoryAndYear(category, year, pageable))
                .willReturn(List.of());

        // when + then
        assertThatThrownBy(() -> noticeService.getNotices(category, year, page, size, sort))
                .isInstanceOf(NoticeNotFoundExcetion.class);
    }
}
