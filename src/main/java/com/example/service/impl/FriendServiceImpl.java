package com.example.service.impl;

import com.example.jpaRepository.FriendRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.Friend;
import com.example.model.User;
import com.example.model.exceptions.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PushNotificationService pushNotificationService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void saveFriend(String friendEmail) {
        User firstUser = getCurrentUser();
        User secondUser = userRepository.findByEmail(friendEmail).orElseThrow(() -> new UserNotFoundException("No user found with that email"));

        if (firstUser.getId().equals(secondUser.getId())) {
            throw new IllegalArgumentException("You can't add yourself as a friend");
        }

        if(firstUser.getId() > secondUser.getId()){
            User temp = firstUser;
            firstUser = secondUser;
            secondUser = temp;
        }

        Friend friend = new Friend();
        if( !(friendRepository.existsByFirstUserAndSecondUser(firstUser,secondUser)) ){
            friend.setFirstUser(firstUser);
            friend.setSecondUser(secondUser);
            friendRepository.save(friend);
        }
    }

    public List<User> getFriends() {
        User currentUser = getCurrentUser();
        List<Friend> friends1 = friendRepository.findByFirstUser(currentUser);
        List<Friend> friends2 = friendRepository.findBySecondUser(currentUser);
        List<User> friendUsers = new ArrayList<>();

        for ( Friend friend : friends1 ){
            friendUsers.add(friend.getSecondUser());
        }
        for ( Friend friend : friends2 ){
            friendUsers.add(friend.getFirstUser());
        }
        return friendUsers;
    }

    public boolean toggleTalkFlag(){
        User currentUser = getCurrentUser();

        if (currentUser.isTalkFlagActive()){
            currentUser.setTalkRequestedAt(null);
        }else {
            currentUser.setTalkRequestedAt(LocalDateTime.now());
        }
        userRepository.save(currentUser);

        if (currentUser.isTalkFlagActive()){
            List<String> friendTokens = new ArrayList<>();
            for ( User user : getFriends() ){
                if (user.getPushToken()!=null){
                    friendTokens.add(user.getPushToken());
                }
            }
            pushNotificationService.send(friendTokens,"Kind<3",currentUser.getFirstName()+ " wants to talk");
            System.out.println("Would notify: " + friendTokens);
        }
        return currentUser.isTalkFlagActive();
    }

    public boolean getTalkFlag(){
        User currentUser = getCurrentUser();
        return currentUser.isTalkFlagActive();
    }

    public void sendHeart(Long friendId) {
        User sender = getCurrentUser();
        User receiver = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("No user found with that id"));

        // friendships are stored with the lower id as firstUser, same as in saveFriend()
        User first = sender.getId() < receiver.getId() ? sender : receiver;
        User second = sender.getId() < receiver.getId() ? receiver : sender;
        if (!friendRepository.existsByFirstUserAndSecondUser(first, second)) {
            throw new IllegalArgumentException("You can only send hearts to friends");
        }

        if (receiver.getPushToken() == null) return; // they don't have notifications set up

        pushNotificationService.send(
                List.of(receiver.getPushToken()),
                "Kind<3",
                sender.getFirstName() + " is sending you some love 🩷."
        );
    }


}
