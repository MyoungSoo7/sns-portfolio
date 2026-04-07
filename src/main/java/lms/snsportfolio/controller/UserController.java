package lms.snsportfolio.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lms.snsportfolio.controller.request.UserJoinRequest;
import lms.snsportfolio.controller.request.UserLoginRequest;
import lms.snsportfolio.controller.response.AlarmResponse;
import lms.snsportfolio.controller.response.Response;
import lms.snsportfolio.controller.response.UserJoinResponse;
import lms.snsportfolio.controller.response.UserLoginResponse;
import lms.snsportfolio.model.User;
import lms.snsportfolio.service.AlarmService;
import lms.snsportfolio.service.UserService;
import lms.snsportfolio.utils.ClassUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "User", description = "회원 가입/로그인/알림 조회 및 SSE 구독 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AlarmService alarmService;

    @Operation(summary = "회원 가입", description = "이름과 비밀번호로 회원을 생성한다. 비밀번호는 BCrypt로 암호화된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 성공"),
            @ApiResponse(responseCode = "409", description = "이름 중복")
    })
    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
        return Response.success(UserJoinResponse.fromUser(userService.join(request.getName(), request.getPassword())));
    }

    @Operation(summary = "로그인", description = "이름/비밀번호 검증 후 JWT 토큰을 발급한다. 토큰 만료는 30일.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request.getName(), request.getPassword());
        return Response.success(new UserLoginResponse(token));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public Response<UserJoinResponse> me(Authentication authentication) {
        return Response.success(UserJoinResponse.fromUser(userService.loadUserByUsername(authentication.getName())));
    }

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림을 페이지 단위로 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class);
        return Response.success(userService.alarmList(user.getId(), pageable).map(AlarmResponse::fromAlarm));
    }

    @Operation(summary = "알림 SSE 구독",
            description = "Server-Sent Events로 실시간 알림을 구독한다. Kafka 이벤트 수신 시 푸시된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping(value = "/alarm/subscribe")
    public SseEmitter subscribe(Authentication authentication) {
        log.info("subscribe");
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class);
        return alarmService.connectNotification(user.getId());
    }
}
