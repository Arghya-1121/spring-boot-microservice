package com.example.backend.controller;

import com.example.backend.entity.Comment;
import com.example.backend.entity.Post;
import com.example.backend.service.CommentService;
import com.example.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class API {

    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        if (postService.savePost(post)) return new ResponseEntity<>(HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<?> createComment(@RequestBody Comment comment, @PathVariable Long postId) {
        comment.setPostId(postId);
        var saved = commentService.saveComment(comment);
        if (saved) return new ResponseEntity<>(HttpStatus.ACCEPTED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        var happened = postService.likePost(postId);
        if (happened) return new ResponseEntity<>(HttpStatus.ACCEPTED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // NOTE: to add some user and bot to the database to test the other fields
    //
    // @Autowired
    // private UserService userService;
    // @Autowired
    // private BotService botService;
    //
    // @PostMapping("/users")
    // public void saveUser(@RequestBody User user){
    //     userService.saveUser(user);
    // }
    //
    // @PostMapping("/bots")
    // public void saveBot(@RequestBody Bot bot){
    //     botService.saveBot(bot);
    // }

}
