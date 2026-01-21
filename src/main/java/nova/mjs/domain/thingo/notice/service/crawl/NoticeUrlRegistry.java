package nova.mjs.domain.thingo.notice.service.crawl;

import java.util.Map;

/**
 * 공지 URL 레지스트리
 * - 공지 출처별 URL 정의
 * - URL 확장은 이 클래스만 수정
 */
public final class NoticeUrlRegistry {

    private NoticeUrlRegistry() {}

    /**
     * 학교 공지 URL 목록
     */
    public static Map<String, String> schoolNoticeUrls() {
        return Map.of(
                "general", "mjukr/255/subview.do",
                "academic", "mjukr/257/subview.do",
                "scholarship", "mjukr/259/subview.do",
                "career", "mjukr/260/subview.do",
                "activity", "mjukr/5364/subview.do",
                "rule", "mjukr/4450/subview.do"
        );
    }

    /**
     * 학과 공지 URL 목록
     */
    public static Map<String, String> departmentNoticeUrls() {
        return Map.of(
                "law", "col/1299/subview.do"
                // 이후 학과 추가
        );
    }
}
