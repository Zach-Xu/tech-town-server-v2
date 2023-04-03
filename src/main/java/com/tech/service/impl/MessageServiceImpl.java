package com.tech.service.impl;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.tech.dto.MessageDTO;

import com.tech.exception.AuthException;
import com.tech.exception.NotFoundException;
import com.tech.model.Inbox;
import com.tech.model.Message;
import com.tech.model.User;
import com.tech.repo.InboxRepository;
import com.tech.repo.MessageRepository;
import com.tech.repo.UserRepository;
import com.tech.service.ChatGPTService;
import com.tech.service.MessageService;
import com.tech.utils.InboxType;
import com.tech.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    InboxRepository inboxRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatGPTService chatGPTService;


    @Override
    @Transactional
    public ResponseResult createMessage(MessageDTO messageDTO) {
        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // loginUser was retrieved earlier in userDetailServiceImpl, which was a separate transaction
        // loginUser is in detached state  error: detached entity passed to persist
        // many-to-many relationship will persist loginUser again

        // fetch receiver from database
        User sender = userRepository.findById(loginUser.getId()).orElseThrow(() -> new NotFoundException("Receiver not found"));
        User receiver = userRepository.findById(messageDTO.getReceiverId()).orElseThrow(() -> new NotFoundException("Receiver not found"));

        // check if an inbox already exists for this sender-receiver pair
        List<User> participants = new ArrayList<>(Arrays.asList(sender, receiver));
        List<Message> messages = new ArrayList<>();
        InboxType type = messageDTO.getType();
        Inbox inbox = inboxRepository.findByParticipants( new ArrayList<>(Arrays.asList(sender.getId(), receiver.getId())))
                .orElseGet(() -> {
                    Inbox newInbox = new Inbox();
                    newInbox.setParticipants(participants);
                    participants.forEach(p -> p.getInboxes().add(newInbox));
                    newInbox.setMessages(messages);
                    newInbox.setType(type);
                    return newInbox;
                });

        // create Message instance
        Message newMessage = new Message();
        newMessage.setSender(sender);
        newMessage.setReceiver(receiver);
        String content = messageDTO.getContent();
        newMessage.setContent(content);
        newMessage.setInbox(inbox);

        newMessage.setCreatedTime(LocalDateTime.now());
        switch (type) {
            case REGULAR -> {
                // set relationship
                inbox.getMessages().add(newMessage);
                inbox.setLastMessage(newMessage);
            }
            case BOT -> {
                // create another entity which represents the message generated by ChatGPT
                if( !receiver.getId().equals(1L) ) throw new IllegalArgumentException("BOT message can only be sent to ChatBot");
                String completion = chatGPTService.createCompletion(content);
                Message botMessage = new Message();
                botMessage.setSender(receiver);
                botMessage.setReceiver(sender);
                botMessage.setContent(completion);
                botMessage.setInbox(inbox);

                // set the last message to bot message
                inbox.setLastMessage(botMessage);

                botMessage.setCreatedTime(LocalDateTime.now());
                inbox.getMessages().addAll(Arrays.asList(newMessage, botMessage));
            }
            default -> throw new IllegalArgumentException("Invalid inbox type");
        }

        inboxRepository.save (inbox);

        return new ResponseResult(HTTPResponse.SC_CREATED, "Message was sent successfully", inbox.getLastMessage());
    }

    @Override
    public ResponseResult getAllMessagesByInboxId(Long inboxId) {

        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Inbox> inbox = inboxRepository.findById(inboxId);

        inbox.ifPresent(i -> {
            List<User> participants = i.getParticipants();
            participants.stream()
                    .filter(p -> p.getId().equals(loginUser.getId()))
                    .findAny()
                    .orElseThrow(() ->new AuthException("Not allowed to fetch messages that does not belong to you."));
        });

        // fetch recent 20 messages
        Page<Message> messagePage = messageRepository.findALLByInbox_IdOrderByCreatedTimeDesc(inboxId, PageRequest.ofSize(20 ));
        List<Message> messages = messagePage.getContent();

        return new ResponseResult(HTTPResponse.SC_OK,"Fetched messages successfully" , messages);

    }
}
