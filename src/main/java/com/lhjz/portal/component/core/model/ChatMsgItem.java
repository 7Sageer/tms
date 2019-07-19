package com.lhjz.portal.component.core.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = { "id" })
public class ChatMsgItem {
	
	String uuid;

	// chat id
	Long id;
	
	// chatreply id
	Long rid;

	Action action;

	long version;
	
	String username;
	
	String atUsernames;
	
	Boolean openEdit;
	
	ChatMsgType type;

	@JsonIgnore
	LocalDateTime expire;
}
