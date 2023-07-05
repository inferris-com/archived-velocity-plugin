package com.inferris.player.friends;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Friends {
    private final List<UUID> friendsList;
    private final List<UUID> pendingFriendsList;

    public Friends(){
        friendsList = new ArrayList<>();
        pendingFriendsList = new ArrayList<>();
    }
    public void addFriend(UUID friendUUID){ // Request
        if(!friendsList.contains(friendUUID) && !pendingFriendsList.contains(friendUUID)) {
            friendsList.add(friendUUID);
        }
    }

    public void acceptFriendRequest(UUID friendUUID) {
        if (pendingFriendsList.contains(friendUUID)) {
            pendingFriendsList.remove(friendUUID);
            friendsList.add(friendUUID);
        }
    }

    public void rejectFriendRequest(UUID friendUUID) {
        pendingFriendsList.remove(friendUUID);
    }

    public void removeFriend(UUID friendUUID){
        friendsList.remove(friendUUID);
    }
    public List<UUID> getFriendsList(){
        return friendsList;
    }

    public List<UUID> getPendingFriendsList() {
        return pendingFriendsList;
    }
}
