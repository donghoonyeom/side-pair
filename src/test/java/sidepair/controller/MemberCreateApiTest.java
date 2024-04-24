package sidepair.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import sidepair.controller.helper.ControllerTestHelper;
import sidepair.service.dto.ErrorResponse;
import sidepair.controller.helper.FieldDescriptionHelper.FieldDescription;
import sidepair.service.dto.mamber.request.MemberSkillSaveRequest;
import sidepair.service.member.MemberService;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ConflictException;

@WebMvcTest(MemberController.class)
class MemberCreateApiTest extends ControllerTestHelper {

    @MockBean
    private MemberService memberService;

    @Test
    void 정상적으로_회원가입에_성공한다() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final List<FieldDescription> requestFieldDescription = makeSuccessRequestFieldDescription();

        회원가입(jsonRequest, status().isCreated())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(requestFieldDescription)),
                        responseHeaders(headerWithName(HttpHeaders.LOCATION).description("회원 단일 조회 api 경로"))));
    }

    @Test
    void 회원가입_시_이메일이_형식에_맞지않을때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@!#!@#.com", "password1!",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        doThrow(new BadRequestException("제약 조건에 맞지 않는 이메일입니다."))
                .when(memberService)
                .join(any());

        //then
        회원가입(jsonRequest, status().isBadRequest());
    }

    @Test
    void 회원가입_시_비밀번호가_형식에_맞지않을때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!₩",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        doThrow(new BadRequestException("정해진 비밀번호의 양식이 아닙니다."))
                .when(memberService)
                .join(any());

        //then
        회원가입(jsonRequest, status().isBadRequest());
    }

    @Test
    void 회원가입_시_닉네임이_형식에_맞지않을때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "a", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        doThrow(new BadRequestException("제약 조건에 맞지 않는 닉네임입니다."))
                .when(memberService)
                .join(any());

        //then
        회원가입(jsonRequest, status().isBadRequest());
    }

    @Test
    void 회원가입_시_기술_형식에_맞지않을때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "nickname", PositionType.BACKEND, List.of(new MemberSkillSaveRequest
                ("최대10글자12345456")));
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse expectedResponse = new ErrorResponse("형식에 맞지 않는 기술 이름입니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison().isEqualTo(List.of(expectedResponse));
    }

    @Test
    void 회원가입_시_이메일에_빈값이_들어올_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("", "password1!",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse expectedResponse = new ErrorResponse("이메일은 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison().isEqualTo(List.of(expectedResponse));
    }

    @Test
    void 회원가입_시_비밀번호에_빈값이_들어올_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse expectedResponse = new ErrorResponse("비밀번호는 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison().isEqualTo(List.of(expectedResponse));
    }

    @Test
    void 회원가입_시_닉네임에_빈값이_들어올_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "email1!",
                "", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse expectedResponse = new ErrorResponse("닉네임은 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison().isEqualTo(List.of(expectedResponse));
    }

    @Test
    void 회원가입_시_기술에_빈값이_들어올_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "nickname", PositionType.BACKEND, List.of());
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse patternResponse = new ErrorResponse("기술은 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(patternResponse));
    }

    @Test
    void 회원가입_시_이메일_비밀번호_닉네임_기술_필드에_빈값이_들어올_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("", "",
                "", PositionType.BACKEND, List.of());
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        //then
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        final ErrorResponse emailResponse = new ErrorResponse("이메일은 빈 값일 수 없습니다.");
        final ErrorResponse passwordResponse = new ErrorResponse("비밀번호는 빈 값일 수 없습니다.");
        final ErrorResponse nicknameResponse = new ErrorResponse("닉네임은 빈 값일 수 없습니다.");
        final ErrorResponse skillsResponse = new ErrorResponse("기술은 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(emailResponse, passwordResponse, nicknameResponse, skillsResponse));
    }

    @Test
    void 회원가입_시_중복된_이메일일_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        doThrow(new ConflictException("이미 존재하는 이메일입니다."))
                .when(memberService)
                .join(any());

        //then
        회원가입(jsonRequest, status().isConflict());
    }

    @Test
    void 회원가입_시_중복된_닉네임일_때() throws Exception {
        //given
        final MemberJoinRequest memberJoinRequest = new MemberJoinRequest("test@email.com", "password1!",
                "nickname", PositionType.BACKEND, DEFAULT_SKILLS);
        final String jsonRequest = objectMapper.writeValueAsString(memberJoinRequest);

        //when
        doThrow(new ConflictException("이미 존재하는 닉네임입니다."))
                .when(memberService)
                .join(any());

        //then
        회원가입(jsonRequest, status().isConflict());
    }

    private ResultActions 회원가입(final String jsonRequest, final ResultMatcher result) throws Exception {
        return mockMvc.perform(post(API_PREFIX + "/members/join")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(result)
                .andDo(print());
    }

    private List<FieldDescription> makeSuccessRequestFieldDescription() {
        return List.of(
                new FieldDescription("email", "사용자 이메일",
                        "- 이메일 형식"),
                new FieldDescription("password", "사용자 비밀번호",
                        "- 길이 : 8 ~ 15  +" + "\n" +
                                "- 영어 소문자, 숫자, 특수문자  +" + "\n" +
                                "- 특수문자[!,@,#,$,%,^,&,*,(,),~] 사용 가능"),
                new FieldDescription("nickname", "회원 닉네임", "- 길이 : 2 ~ 8"),
                new FieldDescription("positionType", "회원 포지션",
                        "- 길이 : 4 , 6  +" + "\n" + "- BACKEND, FRONTEND, DESIGNER, ETC"),
                new FieldDescription("skills[].name", "회원 기술 이름", "- 기술 이름")
        );
    }
}
