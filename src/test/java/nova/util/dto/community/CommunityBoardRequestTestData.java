package nova.util.dto.community;

import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.util.fixtureMonkey.FixtureMonkeyFactory;

import java.util.List;

public class CommunityBoardRequestTestData {

    public static CommunityBoardRequest randomSample() {
        return FixtureMonkeyFactory.fixtureMonkey
                .giveMeBuilder(CommunityBoardRequest.class)
                .set("contentImages", List.of(
                        "https://d2zppxfma88m3u.cloudfront.net/boards/temp/sample-image1.png",
                        "https://d2zppxfma88m3u.cloudfront.net/boards/temp/sample-image2.png"

                ))
                .sample();
    }

}




