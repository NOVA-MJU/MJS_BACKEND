package nova.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import nova.mjs.MjsApplication;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.util.JwtData;
import nova.util.dto.community.CommunityBoardRequestTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = MjsApplication.class)
@AutoConfigureMockMvc
@Import(JwtData.class)
//@AutoConfigureRestDocs(outputDir = "build/generated-snippets") //
class CommunityBoardControllerTest {

    @Autowired
    JwtData jwtData;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 게시글_생성_성공() throws Exception {
        String token = jwtData.generateTestAccessToken();
        CommunityBoardRequest request = CommunityBoardRequestTestData.randomSample();

        mockMvc.perform(post("/api/v1/boards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
//                .andDo(MockMvcRestDocumentation.document("community-board-controller-test/게시글_생성_성공"));

    }
}
