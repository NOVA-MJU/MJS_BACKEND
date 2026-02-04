package nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.notice;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.Document.NoticeDocument;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.EntityIndexEvent;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.SearchContentPreprocessor;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticeIndexEventHandler {

    private final SearchContentPreprocessor preprocessor;
    private final SearchIndexPublisher searchIndexPublisher;

    @EventListener
    public void handle(NoticeIndexEvent event) {

        if (event.isDelete()) {
            NoticeDocument document = NoticeDocument.builder()
                    .id(event.getNoticeId().toString())
                    .build();

            searchIndexPublisher.publish(
                    document,
                    EntityIndexEvent.IndexAction.DELETE
            );
            return;
        }

        NoticeDocument document =
                NoticeDocument.from(event.getNotice(), preprocessor);

        searchIndexPublisher.publish(
                document,
                event.getAction() == NoticeIndexEvent.Action.INSERT
                        ? EntityIndexEvent.IndexAction.INSERT
                        : EntityIndexEvent.IndexAction.UPDATE
        );
    }
}
