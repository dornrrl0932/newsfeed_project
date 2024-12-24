package org.example.newsfeed_project.service;

import lombok.RequiredArgsConstructor;
import org.example.newsfeed_project.post.dto.PostFindByPageRequestDto;
import org.example.newsfeed_project.post.dto.PostFindByPageResponseDto;
import org.example.newsfeed_project.dto.PostCreateRequestDto;
import org.example.newsfeed_project.dto.PostCreateResponseDto;
import org.example.newsfeed_project.entity.Post;
import org.example.newsfeed_project.entity.User;
import org.example.newsfeed_project.repository.PostRepository;
import org.example.newsfeed_project.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {
    public final PostRepository postRepository;
    public final UserRepository userRepository;

    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        User findUser = optionalUser.get();
        Post post = new Post(requestDto.getTitle(), requestDto.getContents(), LocalDateTime.now(), LocalDateTime.now(), 0L, findUser);
        Post savePost = postRepository.save(post);
        return new PostCreateResponseDto(savePost.getTitle(), savePost.getContents());
    }


    public Map<Long, List<PostFindByPageResponseDto>> findPostAll(Long requestPage, Long pageSize, PostFindByPageRequestDto requestDto) {
        List<Post> findPostList = postRepository.findAll();
       return sortAndMapByUpdatedDateDesc(findPostList,pageSize,requestPage,requestDto);
    }

    private Map<Long, List<PostFindByPageResponseDto>> sortAndMapByUpdatedDateDesc(List<Post> findPostList, Long pageSize, Long requestPage, PostFindByPageRequestDto requestDto) {
        Map<Long, List<PostFindByPageResponseDto>> result = new HashMap<>();
        List<PostFindByPageResponseDto> postFindByPageResponseDtoArrayList = new ArrayList<>();
        Long pageCount = 0L;
        Long count = 0L;


        switch (requestDto.getOrder()) {
            case "like":
                findPostList.sort(Comparator.comparing(Post::getUpdatedAt).reversed());
                break;
            case "update":
                findPostList.sort(Comparator.comparing(Post::getUpdatedAt).reversed());
                break;
            default:
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        for (Post post : findPostList) {
            if (count == pageSize) {
                postFindByPageResponseDtoArrayList.clear();
                count = 0L;
                pageCount++;
            }
            if (count == 0) {
                result.put(pageCount, new ArrayList<>(postFindByPageResponseDtoArrayList));
            }
            if (pageCount > requestPage) {
                break;
            }
            result.get(pageCount).add(new PostFindByPageResponseDto(post.getTitle(), post.getContents(),post.getUser().getUserName(), post.getUpdatedAt()));
            count++;
        }
        return result;
    }
}
