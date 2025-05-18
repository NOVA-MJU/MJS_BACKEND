package nova.mjs.notice.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.exception.NoticeNotFoundExcetion;
import nova.mjs.notice.repository.NoticeRepository;
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
            throw new NoticeNotFoundExcetion();
        }

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date"));

        Page<NoticeResponseDto> notices;

        if (year != null) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            notices = noticeRepository.findNoticesByCategoryAndDateRange(category, startDate, endDate, pageable);
        } else {
            notices = noticeRepository.findNoticesByCategory(category, pageable);
        }

        if (notices.isEmpty()) {
            throw new NoticeNotFoundExcetion();
        }

        return notices;
    }
}
