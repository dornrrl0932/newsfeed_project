package org.example.newsfeed_project.post.controller;

import org.example.newsfeed_project.common.exception.ResponseCode;
import org.example.newsfeed_project.common.exception.ValidateException;
import org.example.newsfeed_project.common.session.SessionConst;
import org.example.newsfeed_project.dto.ApiResponse;
import org.example.newsfeed_project.entity.Post;
import org.example.newsfeed_project.post.dto.*;
import org.example.newsfeed_project.post.service.PostService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;

	// 게시물 생성
	@PostMapping
	public ResponseEntity<ApiResponse<CreatedPostResponseDto>> createdPost(
		HttpServletRequest request,
		@RequestBody CreatedPostRequestDto createdPostRequest) {
		HttpSession session = request.getSession();
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		CreatedPostResponseDto createdPostResponse = postService.createdPost(userId, createdPostRequest);
		return ResponseEntity.ok(ApiResponse.success(201, "게시물 작성 성공", createdPostResponse));
	}

	// 기간별 조회
	@GetMapping("/dateRange/{page}")
	public ResponseEntity<ApiResponse<PostListDto>> findPostsByDateRange(@PathVariable int page,
		@RequestBody PostFindByDateRangeRequestDto requestDto) {
		int pageSize = 10;

		Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

		return ResponseEntity.ok(
			ApiResponse.success(200, "게시물 기간 별 작성 성공", postService.findPostByDateRange(pageable, requestDto)));
	}

	// 팔로잉 피드 보기
	@GetMapping("/follow/{page}")
	public ResponseEntity<ApiResponse<PostListDto>> getPostsBySessionUser(@PathVariable int page,
		HttpServletRequest request,
		@RequestParam(defaultValue = "updatedAt") String orderBy) {
		HttpSession session = request.getSession();
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);

		if (!orderBy.equals("updatedAt") && !orderBy.equals("likeCount")) {
			throw new ValidateException(ResponseCode.ORDER_NOT_FOUND);
		}

		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, orderBy));

		return ResponseEntity.ok(
			ApiResponse.success(200, "팔로워 피드 조회 성공", postService.getPostsByFriend(userId, pageable)));
	}

	// 게시물 전체 조회
	@GetMapping("/page/{page}")
	public ResponseEntity<ApiResponse<PostListDto>> findPostByPage(@PathVariable int page,
		@RequestParam(defaultValue = "updatedAt") String orderBy) {

		if (!orderBy.equals("updatedAt") && !orderBy.equals("likeCount")) {
			throw new ValidateException(ResponseCode.ORDER_NOT_FOUND);
		}

		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, orderBy));

		return ResponseEntity.ok(
			ApiResponse.success(200, "전체 게시글 조회 성공", postService.findPostByPage(pageable)));
	}

	//게시물 단건 조회
	@GetMapping("/{post_id}")
	public ResponseEntity<ApiResponse<PostFindDetailByIdResponseDto>> findPostByPostId(
		@PathVariable(name = "post_id") Long postId) {

		return ResponseEntity.ok(
			ApiResponse.success(200, "게시물 단건 조회 성공", postService.findPostByPostId(postId)));
	}

	//게시물 수정
	@PatchMapping("/{post_id}")
	public ResponseEntity<ApiResponse<UpdatedPostResponseDto>> updatedPost(HttpServletRequest request,
		@PathVariable(name = "post_id") Long postId,
		@RequestBody UpdatedPostRequestDto updatedPostRequest) {
		HttpSession session = request.getSession();
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		UpdatedPostResponseDto updatedPostResponse = postService.updatePost(userId, postId, updatedPostRequest);
		return ResponseEntity.ok(
			ApiResponse.success(200, "게시글 수정 성공", updatedPostResponse));
	}

	//게시물 삭제
	@DeleteMapping("/{post_id}")
	public ResponseEntity<ApiResponse<Void>> deletedPost(HttpServletRequest request,
		@PathVariable(name = "post_id") Long postId) {
		HttpSession session = request.getSession();
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		postService.deletePost(userId, postId);
		return ResponseEntity.ok(
			ApiResponse.success(200, "게시글 삭제 성공", null));
	}

	//게시글 좋아요 상태 토글
	@PutMapping("/{post_id}/{user_id}/like")
	public ResponseEntity<ApiResponse<LikeNumResponseDto>> toggleLikeStatus(@PathVariable(name = "post_id") Long postId,
		@PathVariable(name = "user_id") Long userId) {

		Post post = postService.toggleLikeStatus(postId, userId);

		return ResponseEntity.ok(
			ApiResponse.success(200, "게시글 좋아요 상태 토글 실행 성공", new LikeNumResponseDto(post.getLikeCount())));
	}
}
