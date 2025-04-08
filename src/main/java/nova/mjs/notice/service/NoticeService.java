package nova.mjs.notice.service;

import java.time.LocalDate;
import java.util.List;
import lombok.*;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import nova.mjs.notice.exception.NoticeNotFoundExcetion;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 조회 메서드
     *
     * @param category 공지 카테고리 (예: general, academic 등)
     * @param year     조회할 연도 (선택)
     * @param page     페이지 번호 (기본값: 0)
     * @param size     한 페이지당 항목 수 (기본값: 15)
     * @param sort     정렬방식("asc" or "desc"), 기본값 "desc")
     * @return 공지사항 리스트
     */
    public Page<NoticeResponseDto> getNotices(String category, Integer year, int page, int size, String sort) {
        // 1) category validation
        if (category == null || category.isEmpty()) {
            throw new NoticeNotFoundExcetion();
        }

        // 2) 정렬 조건 생성 (기본: desc)
        Sort.Direction direction = sort.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date"));

        // 3) 날짜 범위 계산
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (year != null) {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        }

        // 4) repository 호출
        Page<NoticeResponseDto> notices = noticeRepository.findNoticesByCategoryAndYear(
                category, startDate, endDate, pageable
        );

        // 5) 결과 없을 시 예외 처리
        if (notices.isEmpty()) {
            throw new NoticeNotFoundExcetion();
        }

        return notices;
    }

}