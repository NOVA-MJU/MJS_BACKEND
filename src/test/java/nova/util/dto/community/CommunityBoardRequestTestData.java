package nova.util.dto.community;

import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.util.fixtureMonkey.FixtureMonkeyFactory;

import java.util.List;
import java.util.UUID;

public class CommunityBoardRequestTestData {

    public static CommunityBoardRequest randomSample() {
        return FixtureMonkeyFactory.fixtureMonkey
                .giveMeBuilder(CommunityBoardRequest.class)
                .set("contentImages", List.of(
                        "https://d2zppxfma88m3u.cloudfront.net/boards/temp/0d235097-12b1-43aa-8c03-171a49617539/b73c34d8fa2929ba1d1a11465bc144c78d0d04564aba5fb56f7e605366f36cb4.png"
                ))
                .set("tempUuid", UUID.fromString("0d235097-12b1-43aa-8c03-171a49617539")) // 매핑된 UUID도 명시적으로 설정
                .sample();
    }


}




