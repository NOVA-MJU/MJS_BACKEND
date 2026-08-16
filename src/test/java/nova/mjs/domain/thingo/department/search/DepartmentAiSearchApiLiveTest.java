package nova.mjs.domain.thingo.department.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MJU_DEPARTMENT_API_LIVE", matches = "true")
class DepartmentAiSearchApiLiveTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsPersistedDataScienceProfileThroughActualController() throws Exception {
        String response = mockMvc.perform(get("/api/v1/ai/departments/search")
                        .param("query", "데이터사이언스전공 소개")
                        .param("category", "AUTO"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        System.out.println("DEPARTMENT_API_RESPONSE=" + response);
        assertThat(response).contains(
                "DEPARTMENT:AI_SOFTWARE:DATA_SCIENCE",
                "PROFILE_CARD",
                "https://www.mju.ac.kr/software/9783/subview.do");
    }
}
