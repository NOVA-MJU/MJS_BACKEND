package nova.util.dto;

import nova.mjs.news.DTO.NewsResponseDTO;
import nova.util.fixtureMonkey.FixtureMonkeyFactory;

public class NewsResponseDTOFixture {

    public static NewsResponseDTO sample() {
        return FixtureMonkeyFactory.fixtureMonkey.giveMeOne(NewsResponseDTO.class);
    }
}
