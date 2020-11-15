/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.component;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.BlogCommentPayload;
import com.lhjz.portal.model.ChannelAtPayload;
import com.lhjz.portal.model.ChannelPayload;
import com.lhjz.portal.model.ChannelPayload.Cmd;
import com.lhjz.portal.model.DirectPayload;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.ChatReplyRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.util.HtmlUtil;
import com.lhjz.portal.util.StringUtil;

import lombok.extern.log4j.Log4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年6月14日 上午10:31:32
 * 
 */
@Component
@Log4j
public class AsyncTask {

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	ChatDirectRepository chatDirectRepository;

	@Autowired
	ChatReplyRepository chatReplyRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	IChatMsg chatMsg;

	private final int LIMIT = 25;

	@Async
	public void updateChatChannel(String content, Long id, SimpMessagingTemplate messagingTemplate, String username,
			String atUsernames) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatChannel chatChannel = chatChannelRepository.findOne(id);

				if (chatChannel != null) {
					chatChannel.setContent(content);
					ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);

					chatMsg.put(chatChannel2, Action.Update, ChatMsgType.Content, username, atUsernames, null);
					wsSendChannel(chatChannel2, messagingTemplate, username, atUsernames);
				}
			}
		}
	}

	@Async
	public void updateBlogComment(String content, Long id, SimpMessagingTemplate messagingTemplate, String username,
			String atUsernames) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				Comment comment = commentRepository.findOne(id);

				if (comment != null) {
					comment.setContent(content);
					Comment comment2 = commentRepository.saveAndFlush(comment);

					messagingTemplate.convertAndSend("/blog/comment/update",
							BlogCommentPayload.builder().id(comment.getId()).version(comment2.getVersion())
									.bid(comment.getTargetId()).cmd(com.lhjz.portal.model.BlogCommentPayload.Cmd.U)
									.username(username).atUsernames(atUsernames).build());
				}
			}
		}
	}

	@Async
	public void updateChatReply(String content, Long id, SimpMessagingTemplate messagingTemplate, String username,
			String atUsernames) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatReply chatReply = chatReplyRepository.findOne(id);

				if (chatReply != null) {
					chatReply.setContent(content);
					ChatReply chatReply2 = chatReplyRepository.saveAndFlush(chatReply);

					chatMsg.put(chatReply2.getChatChannel(), Action.Update, ChatMsgType.Reply, username, atUsernames,
							chatReply2);
					wsSendChannel(chatReply2.getChatChannel(), messagingTemplate, username, atUsernames);
				}
			}
		}
	}

	private void wsSendChannel(ChatChannel chatChannel, SimpMessagingTemplate messagingTemplate, String username,
			String atUsernames) {
		try {
			messagingTemplate.convertAndSend("/channel/update",
					ChannelPayload.builder().uuid(UUID.randomUUID().toString()).username(username)
							.atUsernames(atUsernames).cmd(Cmd.R).id(chatChannel.getChannel().getId())
							.cid(chatChannel.getId()).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Async
	public void updateChatDirect(String content, Long id, SimpMessagingTemplate messagingTemplate, String username) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatDirect chatDirect = chatDirectRepository.findOne(id);

				if (chatDirect != null) {
					chatDirect.setContent(content);
					ChatDirect chatDirect2 = chatDirectRepository.saveAndFlush(chatDirect);

					wsSendDirect(chatDirect2, com.lhjz.portal.model.DirectPayload.Cmd.U, messagingTemplate, username);
				}
			}
		}
	}

	private void wsSendDirect(ChatDirect chatDirect, com.lhjz.portal.model.DirectPayload.Cmd cmd,
			SimpMessagingTemplate messagingTemplate, String username) {
		try {
			messagingTemplate.convertAndSendToUser(chatDirect.getChatTo().getUsername(), "/direct/update",
					DirectPayload.builder().uuid(UUID.randomUUID().toString()).cmd(cmd).username(username)
							.id(chatDirect.getId()).build());
			if (!StringUtils.equals(chatDirect.getChatTo().getUsername(), username)) {
				messagingTemplate.convertAndSendToUser(username, "/direct/update", DirectPayload.builder()
						.uuid(UUID.randomUUID().toString()).cmd(cmd).username(username).id(chatDirect.getId()).build());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Async
	public void wsSendAtMsg(ChatChannel chatChannel, com.lhjz.portal.model.ChannelAtPayload.Cmd cmd,
			SimpMessagingTemplate messagingTemplate, User sender, String... usernames) {
		try {

			for (String username : usernames) {
				String from = StringUtils.isNotEmpty(sender.getName()) ? sender.getName() : sender.getUsername();
				messagingTemplate.convertAndSendToUser(username, "/channel/at",
						ChannelAtPayload.builder().uuid(UUID.randomUUID().toString()).from(from)
								.username(sender.getUsername()).to(username).cid(chatChannel.getChannel().getId())
								.cname(chatChannel.getChannel().getName()).ctitle(chatChannel.getChannel().getTitle())
								.id(chatChannel.getId()).version(chatChannel.getVersion())
								.content(StringUtil.limitLength(chatChannel.getContent(), LIMIT)).cmd(cmd).build());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Async
	public void wsSendAtReplyMsg(ChatReply chatReply, com.lhjz.portal.model.ChannelAtPayload.Cmd cmd,
			SimpMessagingTemplate messagingTemplate, User sender, String... usernames) {
		try {

			for (String username : usernames) {
				String from = StringUtils.isNotEmpty(sender.getName()) ? sender.getName() : sender.getUsername();
				messagingTemplate.convertAndSendToUser(username, "/channel/at",
						ChannelAtPayload.builder().uuid(UUID.randomUUID().toString()).from(from)
								.username(sender.getUsername()).to(username)
								.cid(chatReply.getChatChannel().getChannel().getId())
								.cname(chatReply.getChatChannel().getChannel().getName())
								.ctitle(chatReply.getChatChannel().getChannel().getTitle()).id(chatReply.getId())
								.ccid(chatReply.getChatChannel().getId()).version(chatReply.getVersion())
								.content(StringUtil.limitLength(chatReply.getContent(), LIMIT)).cmd(cmd).build());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Async
	public void wsSendChannelNotice(Long id, Long cid, Set<String> users, String cmd,
			SimpMessagingTemplate messagingTemplate) {
		try {

			users.forEach(username -> {
				HashMap<Object, Object> msg = Maps.newHashMap();
				msg.put("id", id);
				msg.put("cid", cid);
				msg.put("cmd", cmd);

				messagingTemplate.convertAndSendToUser(username, "/channel/notice", msg);
			});

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
