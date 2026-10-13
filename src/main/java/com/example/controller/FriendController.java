package com.example.controller;

import com.example.model.Friend;
import com.example.model.User;
import com.example.model.exceptions.UserNotFoundException;
import com.example.service.impl.FriendServiceImpl;
import com.example.service.impl.UserServiceImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendServiceImpl friendService;
    private final UserServiceImpl userService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestParam("friendEmail") String friendEmail) {
        try {
            friendService.saveFriend(friendEmail);
            return ResponseEntity.ok(Map.of("message", "Friend added successfully"));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriends() {
        List <User> myFriends = friendService.getFriends();
        return ResponseEntity.ok(myFriends);
    }

    @PostMapping("/toggle-talk-flag")
    public ResponseEntity<?> toggleTalkFlag() {
        boolean isActive = friendService.toggleTalkFlag();
        return  ResponseEntity.ok(Map.of("talkFlagActive", isActive));
    }

    @GetMapping("/me/talk-flag")
    public ResponseEntity<?> getMyTalkFlag() {
        boolean isActive = friendService.getTalkFlag();
        return  ResponseEntity.ok(Map.of("talkFlagActive", isActive));
    }

    @PutMapping("/save-push-token")
    public ResponseEntity<?> savePushToken(@RequestParam("pushToken") String pushToken) {
        userService.savePushToken(pushToken);
        return ResponseEntity.ok().build();
    }


}
