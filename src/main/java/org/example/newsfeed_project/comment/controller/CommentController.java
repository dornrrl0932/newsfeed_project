package org.example.newsfeed_project.comment.controller;

import org.example.newsfeed_project.comment.dto.*;
import org.example.newsfeed_project.comment.service.CommentService;
import org.example.newsfeed_project.common.session.SessionConst;
import org.example.newsfeed_project.dto.ApiResponse;
import org.example.newsfeed_project.entity.Comment;
import org.example.newsfeed_project.post.dto.LikeNumResponseDto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/feed/{post_id}/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	// 댓글 작성
	@PostMapping
	public ResponseEntity<ApiResponse<CommentDto>> saveComment(@PathVariable("post_id") Long postId,
		@Valid @RequestBody CommentRequestDto requestDto,
		HttpServletRequest request) {

		Long userId = (Long)request.getSession().getAttribute("loginUserId");
		CommentDto commentDto = commentService.saveComment(postId, userId, requestDto);
		return ResponseEntity.ok(
			ApiResponse.success(201, "댓글 등록 성공", commentDto));
	}

	// 댓글 조회
	@GetMapping
	public ResponseEntity<ApiResponse<CommentListDto>> findcomment(@PathVariable(name = "post_id") Long postId,
		@RequestParam(defaultValue = "1") int pageNum) {

		//페이징, 정렬조건
		PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "likeCount"));

		return ResponseEntity.ok(
			ApiResponse.success(200, "댓글 조회 성공", commentService.findcomment(postId, pageRequest)));
	}

	// 댓글 수정
	@PatchMapping("/{comment_id}")
	public ResponseEntity<ApiResponse<CommentDto>> modifyComment(@PathVariable(name = "post_id") Long postId,
		@PathVariable(name = "comment_id") Long commentId, @Valid @RequestBody CommentRequestDto dto,
		HttpServletRequest servletRequest) {

		HttpSession httpSession = servletRequest.getSession(false);
		Long loginUserId = (Long)httpSession.getAttribute(SessionConst.LOGIN_USER_ID);

		return ResponseEntity.ok(
			ApiResponse.success(200, "댓글 수정 성공", commentService.modifyComment(loginUserId, postId, commentId, dto)));
	}

	// 댓글 삭제
	@DeleteMapping("/{comment_id}")
	public ResponseEntity<ApiResponse<Void>> deleteComment(HttpServletRequest request,
		@PathVariable(name = "post_id") Long postId, @PathVariable(name = "comment_id") Long commentId) {

		HttpSession session = request.getSession();
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);

		commentService.deleteComment(userId, postId, commentId);

		return ResponseEntity.ok(ApiResponse.success(200, "댓글 삭제 성공", null));
	}

	// 댓글 좋아요 상태 토글
	@PutMapping("/{comment_id}/{user_id}/like")
	public ResponseEntity<ApiResponse<LikeNumResponseDto>> toggleCommentLikeStatus(
		@PathVariable(name = "post_id") Long postId,
		@PathVariable(name = "comment_id") Long commentId,
		@PathVariable(name = "user_id") Long userId
	) {

		Comment comment = commentService.toggleCommentLikeSatus(postId, commentId, userId);

		return ResponseEntity.ok(
			ApiResponse.success(200, "댓글 좋아요 상태 토글 성공", new LikeNumResponseDto(comment.getLikeCount())));
	}

}
