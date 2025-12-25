package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.*;
import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.Post;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.PostRepository;
import com.socialmedia.connecto.repositories.RepostRepository;
import com.socialmedia.connecto.services.BlockService;
import com.socialmedia.connecto.services.FollowService;
import com.socialmedia.connecto.services.PostService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final FollowService followService;
    private final BlockService blockService;
    private final RepostRepository repostRepository;

    public PostServiceImpl(PostRepository postRepository, UserService userService, FollowService followService, BlockService blockService, RepostRepository repostRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.followService = followService;
        this.blockService = blockService;
        this.repostRepository = repostRepository;
    }

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto) {
        Post post = new Post();
        post.setContent(dto.getContent());
        post.setUser(userService.getCurrentUser());

        Post savedPost = postRepository.save(post);

        return mapPostToResponseDTO(savedPost);
    }

    @Override
    public PostResponseDTO updatePost(Long postId, PostRequestDTO dto) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        User user = userService.getCurrentUser();

        if (!post.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("You can only edit your posts");

        if (post.getContent().equals(dto.getContent()))
            return mapPostToResponseDTO(post);

        post.setContent(dto.getContent());

        Post savedPost = postRepository.save(post);

        return mapPostToResponseDTO(savedPost);
    }

    private static PostResponseDTO mapPostToResponseDTO(Post savedPost) {
        PostResponseDTO responseDTO = new PostResponseDTO();
        responseDTO.setId(savedPost.getId());
        responseDTO.setContent(savedPost.getContent());
        responseDTO.setUserId(savedPost.getUser().getId());
        responseDTO.setUserName(savedPost.getUser().getName());
        responseDTO.setCreatedAt(savedPost.getCreatedAt());
        return responseDTO;
    }

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public PagedDTO<ReposterDTO> getReposters(Long postId, int page, int size) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        if (post.getUser().isPrivate() && !followService.isFollowing(currentUser.getId(), post.getUser().getId()))
            throw new AccessDeniedException("You cannot access a post of a private user you are not following");

        if (blockService.isBlocked(currentUser.getId(), post.getUser().getId()))
            throw new AccessDeniedException("You cannot access a post of a user you blocked");

        if (blockService.isBlocked(post.getUser().getId(), currentUser.getId()))
            throw new AccessDeniedException("You cannot access a post of a user that blocked you");

        Pageable pageable = PageRequest.of(page, size);

        Page<ReposterDTO> reposterDTOPage = repostRepository.findRepostersExcludingBlocked(currentUser.getId(), postId, pageable);

        List<ReposterDTO> dtos = reposterDTOPage.getContent();

        return new PagedDTO<ReposterDTO>(
                dtos,
                reposterDTOPage.getNumber(),
                reposterDTOPage.getTotalPages(),
                reposterDTOPage.getTotalElements()
        );
    }

}
