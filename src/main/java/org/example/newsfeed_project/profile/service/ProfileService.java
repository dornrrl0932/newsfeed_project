package org.example.newsfeed_project.profile.service;

import java.util.List;

import org.example.newsfeed_project.entity.User;
import org.example.newsfeed_project.follow.repository.FollowRepository;
import org.example.newsfeed_project.post.repository.PostRepository;
import org.example.newsfeed_project.user.repository.UserRepository;

import org.example.newsfeed_project.post.dto.PostPageDto;
import org.example.newsfeed_project.profile.dto.*;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;

	// 프로필 조회
	public ProfileDto getProfile(Long userId, Pageable pageable) {
		// 해당 유저 조회
		User user = userRepository.findUserByUserIdOrElseThrow(userId);

		// 게시물 페이지로 갖고오고 List<PostPageDto>로 변환
		List<PostPageDto> posts = PostPageDto.convertFrom(
			postRepository.findByUserOrderByUpdatedAtDesc(user, pageable));

		// (user_id를) 팔로잉 한 유저의 수 -> 팔로우 한 유저를 알아야 함
		Long followingNum = followRepository.countByFollower(user);
		// (user_id가) 팔로우 한 유저의 수 -> 팔로잉 한 유저를 알아야 함
		Long followerNum = followRepository.countByFollowing(user);

		return ProfileDto.convertFrom(user, followingNum, followerNum, posts);
	}

	// 프로필 수정
	@Transactional
	public ProfileUpdateResponseDto updateProfile(Long userId, ProfileUpdateRequestDto requestDto) {
		User findUser = userRepository.findUserByUserIdOrElseThrow(userId);

		findUser.updateIntroduction(requestDto.getIntroduction());

		userRepository.save(findUser);

		return new ProfileUpdateResponseDto(findUser.getIntroduction());
	}
}
