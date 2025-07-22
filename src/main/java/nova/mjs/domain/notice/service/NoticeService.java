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

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date"));

        Page<NoticeResponseDto> notices;

        if (year != null) {
            LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            notices = noticeRepository.findNoticesByCategoryAndDateRange(category, startDate, endDate, pageable);
        } else {
            notices = noticeRepository.findNoticesByCategory(category, pageable);
        }

        if (notices.isEmpty()) {
            throw new NoticeNotFoundException();
        }

        return notices;
    }
}
