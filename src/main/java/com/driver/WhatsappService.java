package com.driver;

import java.util.*;

public class WhatsappService {

    private static int groupNo = 1;
    private static int msgNo = 1;
    
    WhatsappRepository whatsappRepository = new WhatsappRepository();
    
    public String createUser(String name, String mobile) {
        if(whatsappRepository.userExists(mobile))
            throw new RuntimeException("User already exists");
        whatsappRepository.createUser(name,mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        Group group = new Group();
        if(users.size() > 2) {
            group.setName("Group " + groupNo + "");
            groupNo++;

        }
        else group.setName(users.get(1).getName());
        group.setNumberOfParticipants(users.size());
        
        whatsappRepository.createGroup(group,users);
        return group;
    }

    public int createMessage(String content) {
        Message msg = new Message(msgNo,content,new Date());
        msgNo++;
        whatsappRepository.createMessage(msg);
        return msgNo-1;
    }

    public int sendMessage(Message message, User sender, Group group) {
        return whatsappRepository.sendMessage(message,sender,group);
    }

    public String changeAdmin(User approver, User user, Group group) {
        whatsappRepository.changeAdmin(approver, user, group);
        return "SUCCESS";
    }

    public int removeUser(User user) {
        return whatsappRepository.removeUser(user);
    }

    public String findMessage(Date start, Date end, int k) {
        List<Message> messages = whatsappRepository.getAllMessages();
        List<Message> curatedMsg = new ArrayList<>();
        for(Message msg : messages){
            if(start.before(msg.getTimestamp()) && end.after(msg.getTimestamp()))
                curatedMsg.add(msg);
        }
        curatedMsg.sort(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
        return curatedMsg.get(messages.size() - k).getContent();
    }
}
