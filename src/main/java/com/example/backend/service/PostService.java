package com.example.backend.service;

import com.example.backend.entity.AuthorType;
import com.example.backend.entity.Post;
import com.example.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BotService botService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    @Transactional
    public boolean savePost(Post post) {
        if (post == null) return false;
        if (post.getId() != null) {
            Optional<Post> exist = findPost(post.getId());
            if (exist.isPresent()) return false;
        }

        var authorName = post.getAuthorName();
        var authorType = post.getAuthorType();
        Long authorId = null;

        if (authorType == AuthorType.USER) {
            authorId = userService.exist(authorName);
        } else if (authorType == AuthorType.BOT) {
            authorId = botService.exist(authorName);
        } else {
            var userId = userService.exist(authorName);
            authorId = userId != null ? userId : botService.exist(authorName);
            authorType = authorId == null ? null : userId != null ? AuthorType.USER : AuthorType.BOT;
        }

        if (authorId != null) {
            post.setLikeCount(0);
            post.setDate(LocalDateTime.now());
            post.setAuthorType(authorType);
            post.setAuthorId(authorId);
            postRepository.save(post);
            return true;
        } else return false;
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public void deletePost(long id) {
        postRepository.deleteById(Long.valueOf(id));
    }

    @Transactional
    public boolean updatePost(Post post) {
        if (post == null) return false;
        Optional<Post> previous = findPost(post.getId());
        if (previous.isEmpty()) return false;
        var previousPost = previous.get();
        post.setAuthorId(previousPost.getAuthorId());
        post.setAuthorName(previousPost.getAuthorName());
        post.setAuthorType(previousPost.getAuthorType());
        post.setDate(previousPost.getDate());
        post.setLikeCount(previousPost.getLikeCount());
        postRepository.save(post);
        return true;
    }

    public Optional<Post> findPost(Long id) {
        return postRepository.findById(id);
    }

    public Optional<Post> findPost(long id) {
        return postRepository.findById(Long.valueOf(id));
    }

    @Transactional
    public boolean likePost(Long postId) {

        Optional<Post> exist = findPost(postId);
        if (exist.isEmpty()) return false;

        Post post = exist.get();

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        String viralityKey = "post:" + postId + ":virality_score";

        redisService.increment(viralityKey, 20);

        return true;
    }

    public boolean exist(Long postId) {
        var post = findPost(postId);
        return post.isPresent();
    }

    public Long getPostOwnerId(Long postId) {
        return postRepository.findById(postId).map(post -> post.getAuthorId()).orElse(null);
    }
}
