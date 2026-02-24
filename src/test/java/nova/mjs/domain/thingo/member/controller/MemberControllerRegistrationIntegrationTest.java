package nova.mjs.domain.thingo.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nova.mjs.domain.thingo.member.DTO.MemberDTO;
import nova.mjs.domain.thingo.member.controller.support.CollegeDepartmentTestData;
import nova.mjs.domain.thingo.member.controller.support.MemberRegistrationRequestFixture;
import nova.mjs.domain.thingo.member.service.command.MemberCommandService;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.util.security.AuthDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberQueryService memberQueryService;

    @MockBean
    private MemberCommandService memberCommandService;

    private static final AtomicInteger EMAIL_SEQUENCE = new AtomicInteger(1);

    @ParameterizedTest(name = "[{index}] {0} - {1}")
    @MethodSource("validCollegeDepartmentPairs")
    @DisplayName("회원가입 API: 단과대-학과 유효 조합 전체 요청 시 201 Created")
    void registerMember_shouldReturnCreated_whenAllValidCollegeDepartmentCombinations(
            String collegeName,
            CollegeDepartmentTestData.CollegeDepartmentPair pair
    ) throws Exception {
        int sequence = EMAIL_SEQUENCE.getAndIncrement();

        MemberDTO.MemberRegistrationRequestDTO request = MemberRegistrationRequestFixture.validRequest(
                pair.college(),
                pair.departmentName(),
                sequence
        );

        given(memberCommandService.registerMember(any(MemberDTO.MemberRegistrationRequestDTO.class)))
                .willReturn(AuthDTO.LoginResponseDTO.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .build());

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("API 요청 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(memberCommandService).registerMember(any(MemberDTO.MemberRegistrationRequestDTO.class));
    }

    @Test
    @DisplayName("회원가입 API: 학번 형식(8자리 숫자) 위반 시 400 Bad Request")
    void registerMember_shouldReturnBadRequest_whenStudentNumberPatternInvalid() throws Exception {
        MemberDTO.MemberRegistrationRequestDTO request = MemberRegistrationRequestFixture.invalidStudentNumberRequest();

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(memberCommandService, never()).registerMember(any(MemberDTO.MemberRegistrationRequestDTO.class));
    }

    private static Stream<Arguments> validCollegeDepartmentPairs() {
        return CollegeDepartmentTestData.validPairs()
                .map(pair -> Arguments.of(collegeDisplayName(pair.college().name()), pair));
    }

    private static String collegeDisplayName(String college) {
        return switch (college) {
            case "HUMANITIES" -> "인문대학";
            case "SOCIAL_SCIENCES" -> "사회과학대학";
            case "BUSINESS" -> "경영대학";
            case "MEDIA_HUMANLIFE" -> "미디어·휴먼라이프대학";
            case "AI_SOFTWARE" -> "인공지능·소프트웨어융합대학";
            case "FUTURE_CONVERGENCE" -> "미래융합대학";
            case "HONOR" -> "아너칼리지";
            default -> college;
        };
    }
}
