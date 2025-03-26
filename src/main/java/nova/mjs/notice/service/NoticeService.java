package nova.mjs.notice.service;

import java.util.List;
import lombok.*;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.repository.NoticeRepository;
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
    public List<NoticeResponseDto> getNotices(
            String category,
            Integer year,
            int page,
            int size,
            String sort
    ) {

        // 1) 정렬 파라미터 처리
        // -sort가 "asc"면 오름차순, 그 외에는 내림차순
        Sort.Direction direction = sort.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC; // 기본값 desc

        // 2) Pageable 생성 (정렬 기준: date 컬럼)
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date"));

        // 3) Repo 호출
        List<NoticeResponseDto> notices = noticeRepository.findNoticesByCategoryAndYear(
                category, year, pageable);


        if (category == null || category.isEmpty()) {
            throw new NoticeNotFoundExcetion();
        }

        if (notices.isEmpty()) {
            throw new NoticeNotFoundExcetion();
        }

        return notices;
    }
}