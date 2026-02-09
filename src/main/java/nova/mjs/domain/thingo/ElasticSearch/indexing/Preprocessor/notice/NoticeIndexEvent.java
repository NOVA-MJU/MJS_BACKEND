package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.notice;

import lombok.Getter;
import nova.mjs.domain.thingo.notice.entity.Notice;

@Getter
public class NoticeIndexEvent {

    private final Notice notice;
    private final Long noticeId;
    private final Action action;

    public enum Action {
        INSERT,
        UPDATE,
        DELETE
    }

    private NoticeIndexEvent(Notice notice, Long noticeId, Action action) {
        this.notice = notice;
        this.noticeId = noticeId;
        this.action = action;
    }

    public static NoticeIndexEvent insert(Notice notice) {
        return new NoticeIndexEvent(notice, null, Action.INSERT);
    }

    public static NoticeIndexEvent update(Notice notice) {
        return new NoticeIndexEvent(notice, null, Action.UPDATE);
    }

    public static NoticeIndexEvent delete(Long noticeId) {
        return new NoticeIndexEvent(null, noticeId, Action.DELETE);
    }

    public boolean isDelete() {
        return action == Action.DELETE;
    }
}
