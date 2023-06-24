package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap = new HashMap<>();
    private HashMap<Group, List<Message>> groupMessageMap = new HashMap<>();
    private HashMap<Message, User> senderMap = new HashMap<>();
    private HashMap<Group, User> adminMap = new HashMap<>();
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    private Map<String,String> userMap = new HashMap<>();
    private Map<Integer,Message> msgMap = new HashMap<>();
    public WhatsappRepository(){
//        this.groupMessageMap = new HashMap<Group, List<Message>>();
//        this.groupUserMap = new HashMap<Group, List<User>>();
//        this.senderMap = new HashMap<Message, User>();
//        this.adminMap = new HashMap<Group, User>();
//        this.userMobile = new HashSet<>();
//        this.customGroupCount = 0;
//        this.messageId = 0;
    }

    public  void createMessage(Message msg) {
        msgMap.put(msg.getId(),msg);
    }

    public void createUser(String name, String mobile) {
        userMap.put(mobile,name);
    }

    public boolean userExists(String mobile) {
        return userMap.containsKey(mobile);
    }


    public void createGroup(Group group, List<User> users) {
        groupUserMap.computeIfAbsent(group, g -> new ArrayList<>()).addAll(users);
        adminMap.put(group,users.get(0));
    }

    public int sendMessage(Message message, User sender, Group group) {
        if(!groupUserMap.containsKey(group))
            throw new RuntimeException("Group does not exist");
        boolean isMember = false;
        List<User> users = groupUserMap.get(group);
        for(User user : users){
            if(user.getMobile().equals(sender.getMobile())) {
                isMember = true;
                break;
            }
        }

        if(!isMember)
            throw new RuntimeException("You are not allowed to send message");

        groupMessageMap.computeIfAbsent(group, g -> new ArrayList<>()).add(message);
        senderMap.put(message,sender);
        return groupMessageMap.get(group).size();
    }

    public void changeAdmin(User approver, User user, Group group) {

        if(!groupUserMap.containsKey(group))
            throw new RuntimeException("Group does not exist");

        if(!(adminMap.get(group).getMobile().equals(approver.getMobile())))
            throw new RuntimeException("Approver does not have rights");

        List<User> users = groupUserMap.get(group);

        boolean isMember = false;

        for(User usr : users){
            if(usr.getMobile().equals(user.getMobile())) {
                isMember = true;
                break;
            }
        }

        if(!isMember)
            throw new RuntimeException("User is not a participant");

        adminMap.put(group,user);

    }

    public int removeUser(User user) {


        for(Map.Entry<Group,List<User>> entry : groupUserMap.entrySet()){
            List<User> users = entry.getValue();
            Group group = entry.getKey();
            ListIterator<User> itr = users.listIterator();
            while(itr.hasNext()){
                User currUser = itr.next();
                if(currUser.getMobile().equals(user.getMobile())){
                    if(adminMap.get(group).equals(currUser))
                        throw new RuntimeException("Cannot remove admin");
                    itr.remove();
                    group.setNumberOfParticipants(groupUserMap.get(group).size());

                    //delete all messages of the user from the group
                    List<Message> messages = groupMessageMap.get(group);
                    ListIterator<Message> msgItr = messages.listIterator();
                    while (msgItr.hasNext()){
                        Message msg = msgItr.next();
                        if(senderMap.get(msg).getMobile().equals(currUser.getMobile()))
                            msgItr.remove();
                    }

                    return groupUserMap.get(group).size() + groupMessageMap.get(group).size() + getMessageCountOfAllGroups();


                }
            }

        }
        throw new RuntimeException("User not found");

    }

    private int getMessageCountOfAllGroups() {
        int count = 0;

        for(Map.Entry<Group,List<Message>> entry : groupMessageMap.entrySet()){
            count += entry.getValue().size();
        }
        return count;
    }

    public List<Message> getAllMessages() {
        return new ArrayList<>(senderMap.keySet());
    }
}
