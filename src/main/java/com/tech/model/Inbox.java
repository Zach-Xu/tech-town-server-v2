package com.tech.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.tech.utils.InboxType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity(name = "Inbox")
@Table(name = "tb_inbox")
@Getter
@Setter
public class Inbox extends BaseEntity{

    @ManyToMany(
            cascade = {CascadeType.PERSIST,CascadeType.REMOVE},
            mappedBy = "inboxes"
    )
    @JsonIncludeProperties({"id","username"})
    private List<User> participants;

    @OneToMany(
            cascade = {CascadeType.PERSIST,CascadeType.REMOVE},
            mappedBy = "inbox"
    )
    private List<Message> messages;

    @OneToOne(
            cascade = {CascadeType.PERSIST,CascadeType.REMOVE}
    )
    @JoinColumn(
            name = "last_message_id",
            nullable = true,
            referencedColumnName = "id"
    )
    private Message lastMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InboxType type;
}

