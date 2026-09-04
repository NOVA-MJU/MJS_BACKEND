package nova.mjs.domain.thingo.keywordAlarm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 수동 키워드 알림 발송 DTO.
 *
 * 특정 회원(email)에게 키워드에 매칭되는 과거 콘텐츠 1건을 골라 FCM 푸시로 보낸다.
 */
public class ManualAlarmDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {

        @NotBlank(message = "대상 회원 이메일이 필요합니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        /** 알림함 스냅샷(matched_keyword) 제약과 동일하게 최대 5자. */
        @NotBlank(message = "키워드가 필요합니다.")
        @Size(max = 5, message = "키워드는 최대 5자까지 가능합니다.")
        private String keyword;

        /**
         * (선택) 발송할 과거 콘텐츠를 정확히 지정. 통합검색 id 형식 {TYPE}:{원본ID}.
         * 지정하면 이 콘텐츠로 발송하고, 없으면 keyword 최신 매칭으로 자동 선택한다.
         */
        @Size(max = 64, message = "searchIndexId 는 최대 64자입니다.")
        private String searchIndexId;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {

        private String email;
        private String keyword;

        /** 매칭된 과거 콘텐츠 */
        private String searchIndexId;
        private String matchedTitle;
        private String matchedType;
        private String link;

        /** 알림함(NotificationHistory) 레코드 id */
        private Long historyId;

        /** 발송 대상 기기 토큰 수 */
        private int tokenCount;

        /** 실제 FCM 발송 요청이 이루어졌는지(토큰이 1개 이상일 때 true) */
        private boolean pushDispatched;

        public static Response of(String email, String keyword, String searchIndexId,
                                  String matchedTitle, String matchedType, String link,
                                  Long historyId, int tokenCount) {
            return Response.builder()
                    .email(email)
                    .keyword(keyword)
                    .searchIndexId(searchIndexId)
                    .matchedTitle(matchedTitle)
                    .matchedType(matchedType)
                    .link(link)
                    .historyId(historyId)
                    .tokenCount(tokenCount)
                    .pushDispatched(tokenCount > 0)
                    .build();
        }
    }
}
