package com.example.backend.service;

import com.example.backend.entity.AuthorType;
import com.example.backend.entity.Comment;
import com.example.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostService postService;
    @Autowired
    private BotService botService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    @Transactional
    public boolean saveComment(Comment comment) {

        if (comment == null) return false;

        if (comment.getId() != null && findComment(comment.getId()).isPresent()) return false;

        if (!postService.exist(comment.getPostId())) return false;

        var commentAuthorName = comment.getAuthorName();
        var authorType = comment.getAuthorType();
        Long authorId = null;

        if (authorType == AuthorType.USER) {
            authorId = userService.exist(commentAuthorName);
        } else if (authorType == AuthorType.BOT) {
            authorId = botService.exist(commentAuthorName);
        } else {
            var userId = userService.exist(commentAuthorName);
            authorId = userId != null ? userId : botService.exist(commentAuthorName);
            authorType = authorId == null ? null : (userId != null ? AuthorType.USER : AuthorType.BOT);
        }

        if (authorId == null) return false;

        comment.setAuthorId(authorId);
        comment.setAuthorType(authorType);
        comment.setCreatedAt(LocalDateTime.now());

        Long postId = comment.getPostId();
        boolean isBot = authorType == AuthorType.BOT;

        if (comment.getDepthLevel() != null && comment.getDepthLevel() > 20) {
            throw new RuntimeException("Max depth exceeded");
        }

        if (isBot) {
            String botCountKey = "post:" + postId + ":bot_count";

            Long count = redisService.increment(botCountKey);

            if (count != null && count > 100) {
                redisService.decrement(botCountKey);
                throw new RuntimeException("Too many bot replies (429)");
            }
        }

        if (isBot) {
            Long postOwnerId = postService.getPostOwnerId(postId);

            String cooldownKey = "cooldown:bot_" + authorId + ":human_" + postOwnerId;

            if (redisService.exists(cooldownKey)) {
                throw new RuntimeException("Cooldown active");
            }

            redisService.set(cooldownKey, "1", 600L);
        }

        String viralityKey = "post:" + postId + ":virality_score";
        int points = isBot ? 1 : 50;

        redisService.increment(viralityKey, points);

        commentRepository.save(comment);

        return true;
    }

    @Transactional
    public boolean updateComment(Comment comment) {
        if (comment == null) return false;

        var prevComment = findComment(comment.getId());
        if (prevComment.isEmpty()) return false;

        var newComment = prevComment.get();
        newComment.setContent(comment.getContent());

        commentRepository.save(newComment);
        return true;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (findComment(commentId).isEmpty()) return;
        commentRepository.deleteById(commentId);
    }

    public Optional<Comment> findComment(Long id) {
        return commentRepository.findById(id);
    }
}
