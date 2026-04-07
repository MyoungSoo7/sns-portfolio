package lms.snsportfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lms.snsportfolio.controller.request.PostCommentRequest;
import lms.snsportfolio.controller.request.PostModifyRequest;
import lms.snsportfolio.controller.request.PostWriteRequest;
import lms.snsportfolio.controller.response.CommentResponse;
import lms.snsportfolio.controller.response.PostResponse;
import lms.snsportfolio.controller.response.Response;
import lms.snsportfolio.model.User;
import lms.snsportfolio.service.PostService;
import lms.snsportfolio.utils.ClassUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시글 작성/조회/수정/삭제, 댓글, 좋아요 API")
@RestController
@RequestMapping("/api/v1/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "인증된 사용자가 새 게시글을 작성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public Response<Void> create(@RequestBody PostWriteRequest request, Authentication authentication) {
        postService.create(authentication.getName(), request.getTitle(), request.getBody());
        return Response.success();
    }

    @Operation(summary = "전체 게시글 목록 조회", description = "모든 게시글을 페이지 단위로 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public Response<Page<PostResponse>> list(Pageable pageable, Authentication authentication) {
        return Response.success(postService.list(pageable).map(PostResponse::fromPost));
    }

    @Operation(summary = "내 게시글 목록 조회", description = "로그인한 사용자가 작성한 게시글 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public Response<Page<PostResponse>> myPosts(Pageable pageable, Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class);
        return Response.success(postService.my(user.getId(), pageable).map(PostResponse::fromPost));
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{postId}")
    public Response<PostResponse> modify(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId,
            @RequestBody PostModifyRequest request, Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class);
        return Response.success(
                PostResponse.fromPost(
                        postService.modify(user.getId(), postId, request.getTitle(), request.getBody())));
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 soft delete한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public Response<Void> delete(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId,
            Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class);
        postService.delete(user.getId(), postId);
        return Response.success();
    }

    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 댓글을 페이지 단위로 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}/comments")
    public Response<Page<CommentResponse>> getComments(Pageable pageable,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId) {
        return Response.success(postService.getComments(postId, pageable).map(CommentResponse::fromComment));
    }

    @Operation(summary = "게시글 좋아요 수 조회", description = "특정 게시글의 좋아요 수를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}/likes")
    public Response<Integer> getLikes(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId,
            Authentication authentication) {
        return Response.success(postService.getLikeCount(postId));
    }


    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성한다. Kafka 알림 이벤트가 발행된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{postId}/comments")
    public Response<Void> comment(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId,
            @RequestBody PostCommentRequest request, Authentication authentication) {
        postService.comment(postId, authentication.getName(), request.getComment());
        return Response.success();
    }

    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누른다. 중복 방지 적용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{postId}/likes")
    public Response<Void> like(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Integer postId,
            Authentication authentication) {
        postService.like(postId, authentication.getName());
        return Response.success();
    }

}
