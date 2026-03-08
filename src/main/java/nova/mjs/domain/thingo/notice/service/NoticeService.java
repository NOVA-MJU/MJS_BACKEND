package nova.mjs.domain.thingo.notice.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.notice.dto.NoticeResponseDto;
import nova.mjs.domain.thingo.notice.entity.Notice;
import nova.mjs.domain.thingo.notice.exception.NoticeNotFoundException;
import nova.mjs.domain.thingo.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeResponseDto.Summary> getNotices(String category, Integer year, int page, int size, String sort) {
        if (category == null || category.isEmpty()) {
            throw new NoticeNotFoundException();
        }

        if (category == null || category.trim().isEmpty()) {
            category = "all";
        }

        boolean isAll = "all".equalsIgnoreCase(category);

        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(direction, "date")
        );

        Page<Notice> notices;

        if (year != null) {
            LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);

            notices = isAll
                    ? noticeRepository.findByDateBetween(startDate, endDate, pageable)
                    : noticeRepository.findByCategoryAndDateBetween(category, startDate, endDate, pageable);
        } else {
            notices = isAll
                    ? noticeRepository.findAll(pageable)
                    : noticeRepository.findByCategory(category, pageable);
        }

        if (notices.isEmpty()) {
            throw new NoticeNotFoundException();
        }

        return notices.map(NoticeResponseDto.Summary::fromEntity);

    }

    public List<NoticeResponseDto.Trending> getDailyTrendingNotices(int size) {
        Pageable pageable = PageRequest.of(0, Math.max(1, size));

        return noticeRepository.findTrendingByTodayDelta(LocalDate.now(), pageable)
                .stream()
                .map(NoticeResponseDto.Trending::fromEntity)
                .toList();
    }
}
