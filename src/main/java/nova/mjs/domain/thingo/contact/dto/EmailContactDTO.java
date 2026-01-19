package nova.mjs.domain.thingo.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class EmailContactDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String subject;       // 제목
        private String content;       // 본문 (HTML 포함 가능)

    }

    @Getter
    @Builder
    public static class Response {
        private boolean success;
        private String message;
    }
}
