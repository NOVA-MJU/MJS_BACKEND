package nova.mjs.community.util;

public class ContentPreviewUtil {

    private static final int DEFAULT_PREVIEW_LENGTH = 100;

    // 미리보기 생성 메서드
    public static String makePreview(String content) {
        if (content == null) return "";
        return content.length() <= DEFAULT_PREVIEW_LENGTH
                ? content
                : content.substring(0, DEFAULT_PREVIEW_LENGTH);
    }

    // 필요하면 길이 조정 가능한 버전도 만들 수 있음
    public static String makePreview(String content, int length) {
        if (content == null) return "";
        return content.length() <= length ? content : content.substring(0, length);
    }
}
