package nova.mjs.domain.notice.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.notice.dto.NoticeResponseDto;
import nova.mjs.domain.notice.exception.NoticeNotFoundException;
import nova.mjs.domain.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeResponseDto> getNotices(String category, Integer year, int page, int size, String sort) {
        if (category == null || category.isEmpty()) {
            throw new NoticeNotFoundException();
        }

        // category가 null 또는 빈 문자열이면 "all"로 처리
        if (category == null || category.trim().isEmpty()) {
            category = "all";
        }

        boolean isAll = "all".equalsIgnoreCase(category);

        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                Math.max(0, page), // 음수 방어
                Math.max(1, size), // 최소 1개 보장
                Sort.by(direction, "date")
        );


        Page<NoticeResponseDto> notices;

        if (year != null) {
            // 연도 필터 있을 경우
            LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);

            notices = isAll
                    ? noticeRepository.findNoticesByDateRange(startDate, endDate, pageable) // 전체 조회
                    : noticeRepository.findNoticesByCategoryAndDateRange(category, startDate, endDate, pageable); // 카테고리별 조회
        } else {
            // 연도 필터 없을 경우
            notices = isAll
                    ? noticeRepository.findAllNotices(pageable) // 전체 조회
                    : noticeRepository.findNoticesByCategory(category, pageable); // 카테고리별 조회
        }


        if (notices.isEmpty()) {
            throw new NoticeNotFoundException();
        }

        return notices;
    }
}
