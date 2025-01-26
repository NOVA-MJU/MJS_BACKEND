package nova.mjs.notice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 조회 메서드
     *
     * @param category 공지 카테고리 (예: general, academic 등)
     * @param year 조회할 연도 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 한 페이지당 항목 수 (기본값: 15)
     * @return 공지사항 리스트
     */
    public List<NoticeResponseDto> getNotices(String category, Integer year, int page, int size) {
        if (category == null || category.isEmpty()) {
            throw new BusinessBaseException("공지사항 카테고리는 필수입니다.", ErrorCode.INVALID_PARAM_REQUEST);
        }

        try {
            // DB에서 공지사항 데이터 조회
            List<NoticeResponseDto> notices = noticeRepository.findNoticesByCategoryAndYear(
                    category, year, PageRequest.of(page, size)
            );

            if (notices.isEmpty()) {
                throw new BusinessBaseException("해당 조건의 공지사항이 없습니다.", ErrorCode.NOT_FOUND);
            }

            return notices;
        } catch (Exception e) {
            throw new BusinessBaseException("공지사항 조회 중 오류 발생: " + e.getMessage(), ErrorCode.DATABASE_ERROR);
        }
    }
}
