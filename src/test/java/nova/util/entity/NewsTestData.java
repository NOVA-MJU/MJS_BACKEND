package nova.util.entity;

import nova.mjs.news.entity.News;
import nova.util.fixtureMonkey.FixtureMonkeyFactory;;

public class NewsTestData {

    public static News sample() {
        return FixtureMonkeyFactory.fixtureMonkey.giveMeOne(News.class);
    }

    public static News sampleWithCategory(News.Category category) {
        News news = sample();
        return News.builder()
                .newsIndex(news.getNewsIndex())
                .title(news.getTitle())
                .date(news.getDate())
                .reporter(news.getReporter())
                .imageUrl(news.getImageUrl())
                .summary(news.getSummary())
                .link(news.getLink())
                .category(category)
                .build();
    }
}
