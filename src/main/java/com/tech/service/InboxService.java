package com.tech.service;

import com.tech.dto.InboxDTO;
import com.tech.model.Inbox;
import com.tech.vo.ResponseResult;

import java.util.List;

public interface InboxService {

    ResponseResult<List<Inbox>> getAllInboxes();

    ResponseResult<Inbox> createInbox(InboxDTO inboxDTO);

}
