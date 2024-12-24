package org.example.newsfeed_project.user.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.example.newsfeed_project.user.dto.LoginRequestDto;
import org.example.newsfeed_project.user.dto.SignUpRequestDto;
import org.example.newsfeed_project.user.dto.UpdateUserInfoRequestDto;
import org.example.newsfeed_project.user.encoder.PasswordEncoder;
import org.example.newsfeed_project.entity.User;
import org.example.newsfeed_project.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	//회원 가입
	public void signupUser(SignUpRequestDto signUpRequestDto) {

		//회원 생성
		User user = new User(signUpRequestDto.getEmail(), signUpRequestDto.getPassword(),
			signUpRequestDto.getUserName());

		//DB에 저장
		userRepository.save(user);

	}

	//로그인
	public User login(LoginRequestDto loginRequestDto) {

		User user = userRepository.findUserByEmailOrElseThrow(loginRequestDto.getEmail());

		//비밀번호 검증
		if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password.");
		}
		return user;
	}

	public void updateUserInfo(Long id, UpdateUserInfoRequestDto dto) {

		User findUser = userRepository.findUserByUserIdOrElseThrow(id);

		//비밀번호 null 값 확인 및 유효성 검증 후 수정
		Optional.ofNullable(dto.getPassword()).ifPresent(password -> {
			if (isVaildPassword(dto, password, findUser)) {
				findUser.setPassword(passwordEncoder.encode(password));
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"바꾸려는 비밀번호가 이전과 동일하거나, 입력한 비밀번호가 서로 다릅니다.");
			}
		});

		//회원 이름 null 값 확인 후 수정
		Optional.ofNullable(dto.getUserName()).ifPresent(findUser::setUserName);

		userRepository.save(findUser);
	}

	//수정하려는 비빌번호의 유효성을 검증하는 메서드
	private boolean isVaildPassword(UpdateUserInfoRequestDto dto, String password, User findUser) {

		boolean isPasswordSame = passwordEncoder.matches(password, findUser.getPassword());//암호화된 기존 비밀번호와 비교
		boolean isPasswordsMatch = password.equals(dto.getRenterPassword()); //입력한 비밀번호와 재입력 비밀번호가 같은지 비교

		return !isPasswordSame && isPasswordsMatch;
	}
}