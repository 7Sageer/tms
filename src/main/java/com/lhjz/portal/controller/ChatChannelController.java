/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatAt;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatChannelFollower;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatLabel;
import com.lhjz.portal.entity.ChatPin;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.entity.ChatStow;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.Poll;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatLabelType;
import com.lhjz.portal.pojo.Enum.ChatReplyType;
import com.lhjz.portal.pojo.Enum.Code;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatAtRepository;
import com.lhjz.portal.repository.ChatChannelFollowerRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.ChatLabelRepository;
import com.lhjz.portal.repository.ChatPinRepository;
import com.lhjz.portal.repository.ChatReplyRepository;
import com.lhjz.portal.repository.ChatStowRepository;
import com.lhjz.portal.repository.ScheduleRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.ValidateUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/chat/channel")
public class ChatChannelController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChatChannelController.class);
	
	@Value("${tms.chat.channel.upload.path}")
	private String uploadPath;
	
	@Value("${tms.blog.md2pdf.path}")
	private String md2pdfPath;

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatAtRepository chatAtRepository;
	
	@Autowired
	ChatStowRepository chatStowRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;
	
	@Autowired
	ChatDirectRepository chatDirectRepository;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ScheduleRepository scheduleRepository;
	
	@Autowired
	ChatLabelRepository chatLabelRepository;
	
	@Autowired
	ChatPinRepository chatPinRepository;
	
	@Autowired
	ChatReplyRepository chatReplyRepository;
	
	@Autowired
	ChatChannelFollowerRepository chatChannelFollowerRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam("channelId") Long channelId, @RequestParam("content") String content,
			@RequestParam("contentHtml") String contentHtml) {
		
		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("提交内容不能为空!");
		}

		Channel channel = channelRepository.findOne(channelId);
		
		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel);
		chatChannel.setContent(content);

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		
		final String href = url + "?id=" + chatChannel2.getId();
		final String html = contentHtml; // StringUtil.md2Html(contentHtml, false, true);
		final User loginUser = getLoginUser();

		final Mail mail = Mail.instance();
		mail.addUsers(channel.getSubscriber(), loginUser);
		
		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {
				ChatAt chatAt = new ChatAt();
				chatAt.setChatChannel(chatChannel2);
				chatAt.setAtUser(user);

				chatAtList.add(chatAt);
			});

			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

		}

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-沟通频道@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "下面的沟通频道消息中有@到你", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(chatChannel2);
	}

	@RequestMapping(value = "listBy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy(@RequestParam(value = "id", required = false) Long id,
			@RequestParam("channelId") Long channelId,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Channel channel = channelRepository.findOne(channelId);
		
		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}
		
		int limit = pageable.getPageSize();

		if (StringUtil.isNotEmpty(id)) {
			long cntGtId = chatChannelRepository.countGtId(channel, id);
			int size = limit;
			long page = cntGtId / size;
			if (cntGtId % size == 0) {
				page--;
			}

			pageable = new PageRequest(page > -1 ? (int) page : 0, size, Direction.DESC, "id");
		}

		Page<ChatChannel> page = chatChannelRepository.findByChannel(channel, pageable);
		page.forEach(cc -> reduceChatchannel(cc));

		return RespBody.succeed(page);
	}
	
	private void reduceChatchannel(ChatChannel chatChannel) {
		Channel channel = chatChannel.getChannel();
		Channel channel2 = new Channel();
		channel2.setId(channel.getId());
		channel2.setName(channel.getName());
		channel2.setTitle(channel.getTitle());
		channel2.setMembers(null);
		channel2.setPrivated(null);
		channel2.setStatus(null);
		channel2.setSubscriber(null);
		channel2.setType(null);
		
		chatChannel.setChannel(channel2);

		chatChannel.setUpdater(null);
		chatChannel.setUpdateDate(null);
		chatChannel.setChatChannelFollowers(null);
		chatChannel.setStatus(null);
		chatChannel.setType(null);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames, @RequestParam("id") Long id,
			@RequestParam("version") Long version,
			@RequestParam("content") String content, @RequestParam(value = "diff", required = false) String diff,
			@RequestParam(value = "contentHtml", required = false) String contentHtml,
			@RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("更新内容不能为空!");
		}

		ChatChannel chatChannel = chatChannelRepository.findOne(id);
		
		Boolean isOpenEdit = chatChannel.getOpenEdit() == null ? false : chatChannel
				.getOpenEdit();

		if (!isSuperOrCreator(chatChannel.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}
		
		if (isOpenEdit && !AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}
		
		if (chatChannel.getVersion() != version.longValue()) {
			return RespBody.failed("该频道消息已经被其他人更新,请刷新消息重新编辑提交!");
		}

		if (content.equals(chatChannel.getContent())) {
			return RespBody.failed("更新内容没有任何变更的内容!");
		}
		
		String contentOld = chatChannel.getContent();

		chatChannel.setContent(content);

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		
		logWithProperties(Action.Update, Target.ChatChannel, chatChannel2.getId(), "content", contentOld);

		final User loginUser = getLoginUser();
		final String href = url + "?id=" + chatChannel2.getId();
		final String html;
		if(StringUtil.isNotEmpty(diff)) {
			html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
		} else {
			html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
		}

		final Mail mail = Mail.instance();
		mail.addUsers(chatChannel.getChannel().getSubscriber(), loginUser);
		mail.addUsers(chatChannel.getChatChannelFollowers().stream().map(ccf -> ccf.getCreator())
				.collect(Collectors.toList()), loginUser);

		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {

				ChatAt chatAt2 = chatAtRepository.findOneByChatChannelAndAtUser(chatChannel2, user);
				if (chatAt2 == null) {
					ChatAt chatAt = new ChatAt();
					chatAt.setChatChannel(chatChannel2);
					chatAt.setAtUser(user);

					chatAtList.add(chatAt);
				} else {
					chatAt2.setStatus(Status.New);

					chatAtList.add(chatAt2);
				}
			});
			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

		}

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-沟通频道编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "下面编辑的沟通频道消息中有@到你", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return RespBody.succeed(chatChannel2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!isSuperOrCreator(chatChannel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该频道消息内容!");
		}
		
		List<ChatAt> chatAts = chatAtRepository.findByChatChannel(chatChannel);
		chatAtRepository.delete(chatAts);
		chatAtRepository.flush();
		
		List<ChatStow> chatStows = chatStowRepository.findByChatChannel(chatChannel);
		chatStowRepository.delete(chatStows);
		chatStowRepository.flush();
		
		List<ChatLabel> chatLabels = chatChannel.getChatLabels();
		chatLabels.forEach(cl -> {
			Set<User> voters = cl.getVoters();
			voters.forEach(voter -> voter.getVoterChatLabels().remove(cl));
			userRepository.save(voters);
			userRepository.flush();
		});

		chatChannelRepository.delete(id);
		
		logWithProperties(Action.Delete, Target.ChatChannel, id, "content", chatChannel.getContent());

		return RespBody.succeed(id);
	}
		
	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(id);
		
		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("您没有权限查看该频道消息内容!");
		}
	
		return RespBody.succeed(chatChannel);
	}

	@RequestMapping(value = "latest", method = RequestMethod.GET)
	@ResponseBody
	public RespBody latest(@RequestParam("id") Long id,
			@RequestParam("channelId") Long channelId) {
		
		Channel channel = channelRepository.findOne(channelId);
		
		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}

		List<ChatChannel> chats = chatChannelRepository.latest(channel, id);
		chats.forEach(cc -> reduceChatchannel(cc));
		
		return RespBody.succeed(chats);
	}
	
	@RequestMapping(value = "more", method = RequestMethod.GET)
	@ResponseBody
	public RespBody more(@RequestParam("start") Long start, @RequestParam("last") Boolean last,
			@RequestParam("size") Integer size, @RequestParam("channelId") Long channelId) {

		long count = 0;
		List<ChatChannel> chats = new ArrayList<>();
		
		Channel channel = channelRepository.findOne(channelId);
		
		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}
		
		if (last) {
			count = chatChannelRepository.countAllOld(channel, start);
			chats = chatChannelRepository.queryMoreOld(channel, start, size);
		} else {
			count = chatChannelRepository.countAllNew(channel, start);
			chats = chatChannelRepository.queryMoreNew(channel, start, size);
		}
		
		chats.forEach(cc -> reduceChatchannel(cc));

		return RespBody.succeed(chats).addMsg(count);
	}
	
	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search, @RequestParam("channelId") Long channelId,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}
		
		Channel channel = channelRepository.findOne(channelId);
		
		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}

		String _search = "%" + search + "%";
		List<ChatChannel> chats = chatChannelRepository.queryAboutMe(channel, _search, pageable.getOffset(),
				pageable.getPageSize());
		long cnt = chatChannelRepository.countAboutMe(channel, _search);

		Page<ChatChannel> page = new PageImpl<>(chats, pageable, cnt);
		
		page.forEach(cc -> reduceChatchannel(cc));

		return RespBody.succeed(page);
	}
	

	@RequestMapping(value = "stow", method = RequestMethod.POST)
	@ResponseBody
	public RespBody stow(@RequestParam("id") Long id, @RequestParam(value = "rid", required = false) Long rid) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			return RespBody.failed("收藏频道消息不存在,可能已经被删除!");
		}
		
		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		User loginUser = getLoginUser();
		ChatStow chatStow = null;
		ChatReply chatReply = null;
		if (rid == null) {
			chatStow = chatStowRepository.findOneByChatChannelAndStowUser(chatChannel, loginUser);
		} else {
			chatReply = chatReplyRepository.findOne(rid);
			chatStow = chatStowRepository.findOneByChatChannelAndChatReplyAndStowUser(chatChannel, chatReply,
					loginUser);
		}

		if (chatStow != null) {
			return RespBody.failed("收藏频道消息重复!").addMsg(chatStow);
		}

		ChatStow chatStow2 = new ChatStow();
		chatStow2.setChatChannel(chatChannel);
		chatStow2.setStowUser(loginUser);
		chatStow2.setChatReply(chatReply);

		ChatStow chatStow3 = chatStowRepository.saveAndFlush(chatStow2);

		return RespBody.succeed(chatStow3);
	}
	
	@RequestMapping(value = "removeStow", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeStow(@RequestParam("id") Long id) {
		
		ChatStow stow = chatStowRepository.findOne(id);
		
		if (!isSuperOrCreator(stow.getCreator().getUsername())) {
			return RespBody.failed("权限不足!");
		}

		chatStowRepository.delete(id);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "getStows", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getStows() {

		List<ChatStow> chatStows = chatStowRepository.findByChatChannelNotNullAndStowUserAndStatus(
				getLoginUser(), Status.New);
		
		chatStows.forEach(cs -> reduceChatStow(cs));

		return RespBody.succeed(chatStows);
	}
	
	private void reduceChatStow(ChatStow chatStow) {
		reduceChatchannel(chatStow.getChatChannel());
		chatStow.setStowUser(null);
		chatStow.setCreator(null);
		chatStow.setUpdateDate(null);
		chatStow.setUpdater(null);
		chatStow.setStatus(null);
	}
	
	@RequestMapping(value = "getAts", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getAts(
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<ChatAt> chatAts = chatAtRepository.findByChatChannelNotNullAndAtUserAndStatus(
				getLoginUser(), Status.New, pageable);
		
		chatAts.forEach(ca -> reduceChatAt(ca));

		return RespBody.succeed(chatAts);
	}
	
	private void reduceChatAt(ChatAt chatAt) {
		reduceChatchannel(chatAt.getChatChannel());
		chatAt.setAtUser(null);
		chatAt.setUpdateDate(null);
		chatAt.setUpdater(null);
		chatAt.setStatus(null);
	}

	@RequestMapping(value = "markAsReaded", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAsReaded(@RequestParam("chatAtId") Long chatAtId) {

		ChatAt chatAt = chatAtRepository.findOne(chatAtId);
		if (chatAt == null) {
			return RespBody.failed("@消息不存在,可能已经被删除!");
		}
		
		if (!isSuperOrCreator(chatAt.getAtUser().getUsername())) {
			return RespBody.failed("权限不足!");
		}
		
		chatAt.setStatus(Status.Readed);
		chatAtRepository.saveAndFlush(chatAt);

		return RespBody.succeed(chatAt);
	}
	
	@RequestMapping(value = "markAsReadedByChat", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAsReadedByChat(@RequestParam("chatId") Long chatId) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(chatId);
		if (chatChannel == null) {
			return RespBody.failed("@頻道消息不存在,可能已经被删除!");
		}
		
		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}
		
		int cnt = chatAtRepository.markChatChannelAsReaded(chatChannel, getLoginUser());
		
		return RespBody.succeed(cnt);
	}

	@RequestMapping(value = "markAllAsReaded", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAllAsReaded() {

		int cnt = chatAtRepository.markChatChannelAllAsReaded(getLoginUser());

		return RespBody.succeed(cnt);
	}
	
	@RequestMapping(value = "openEdit", method = RequestMethod.POST)
	@ResponseBody
	public RespBody openEdit(@RequestParam("id") Long id,
			@RequestParam("open") Boolean open) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			return RespBody.failed("操作频道消息不存在,可能已经被删除!");
		}

		if (!isSuperOrCreator(chatChannel.getCreator().getUsername())) {
			return RespBody.failed("权限不足!");
		}

		chatChannel.setOpenEdit(open);
		chatChannelRepository.saveAndFlush(chatChannel);

		return RespBody.succeed();
	}
	
	private boolean isVoterExists(String voters) {
		boolean isExits = false;
		if (voters != null) {
			String loginUsername = WebUtil.getUsername();
			String[] voterArr = voters.split(",");

			for (String voter : voterArr) {
				if (voter.equals(loginUsername)) {
					isExits = true;
					break;
				}
			}
		}

		return isExits;
	}
	
	@RequestMapping(value = "vote", method = RequestMethod.POST)
	@ResponseBody
	public RespBody vote(@RequestParam("id") Long id,
			@RequestParam("url") String url,
			@RequestParam("contentHtml") String contentHtml,
			@RequestParam(value = "type", required = false) String type) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);
		if (chatChannel == null) {
			return RespBody.failed("投票频道消息不存在!");
		}
		
		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}
		
		String loginUsername = WebUtil.getUsername();

		ChatChannel chatChannel2 = null;

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = chatChannel.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				chatChannel.setVoteZan(voteZan == null ? loginUsername : voteZan + ','
						+ loginUsername);
				
				Integer voteZanCnt = chatChannel.getVoteZanCnt();
				if (voteZanCnt == null) {
					voteZanCnt = 0;
				}
				chatChannel.setVoteZanCnt(++voteZanCnt);

				chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
				title = getLoginUserName(loginUser) + "[" + loginUsername
						+ "]赞了你的频道消息!";
			}

		} else {
			String voteCai = chatChannel.getVoteCai();
			if (isVoterExists(voteCai)) {
				return RespBody.failed("您已经投票[踩]过！");
			} else {
				chatChannel.setVoteCai(voteCai == null ? loginUsername : voteCai + ','
						+ loginUsername);
				
				Integer voteCaiCnt = chatChannel.getVoteCaiCnt();
				if (voteCaiCnt == null) {
					voteCaiCnt = 0;
				}
				chatChannel.setVoteCaiCnt(++voteCaiCnt);
				
				chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
				title = getLoginUserName(loginUser) + "[" + loginUsername
						+ "]踩了你的频道消息!";
			}
		}

		final String href = url + "?id=" + id;
		final String titleHtml = title;
		final Mail mail = Mail.instance().addUsers(chatChannel.getCreator());
		final String html = "<h3>投票频道消息内容:</h3><hr/>" + contentHtml;

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-沟通频道消息投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser, "date",
							new Date(), "href", href, "title", titleHtml, "content", html)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		log(Action.Vote, Target.ChatChannel, chatChannel.getId(), chatChannel2);

		return RespBody.succeed(chatChannel2);
	}
	
	@RequestMapping(value = "poll", method = RequestMethod.GET)
	@ResponseBody
	public RespBody poll(
			@RequestParam("channelId") Long channelId,
			@RequestParam("lastChatChannelId") Long lastChatChannelId,
			@RequestParam(value = "isAt", required = false, defaultValue = "false") Boolean isAt) {

		long cnt = isAt ? chatAtRepository.countChatChannelRecentAt(
				WebUtil.getUsername(), lastChatChannelId) : chatChannelRepository
				.countQueryRecent(channelId, lastChatChannelId);
				
		long cntAtUserNew = chatAtRepository.countChatChannelAtUserNew(WebUtil
				.getUsername());
		
		long countMyRecentSchedule = scheduleRepository.countRecentScheduleByUser(WebUtil.getUsername());

		return RespBody.succeed(new Poll(channelId, lastChatChannelId, isAt, cnt, cntAtUserNew, countMyRecentSchedule));
	}
	
	@RequestMapping(value = "download/{id}", method = RequestMethod.GET)
	public void download(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @RequestParam(value = "type", defaultValue = "pdf") String type)
			throws Exception {

		logger.debug("download channel chat start...");
		
		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			try {
				response.sendError(404, "下载频道消息不存在!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!AuthUtil.hasChannelAuth(chatChannel)) {
			try {
				response.sendError(401, "没有权限下载该频道消息!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
		String path = WebUtil.getRealPath(request);
		
		String blogUpdateDate = DateUtil.format(chatChannel.getUpdateDate(), DateUtil.FORMAT9);
		
		String mdFileName = chatChannel.getId() + "_" + blogUpdateDate + ".md";
		String pdfFileName = chatChannel.getId() + "_" + blogUpdateDate + ".pdf";
		
		String mdFilePath = path + uploadPath + mdFileName;
		String pdfFilePath = path + uploadPath + pdfFileName;
		
		File fileMd = new File(mdFilePath);

		if (!fileMd.exists()) {
			try {
				FileUtils.writeStringToFile(fileMd, chatChannel.getContent(), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		File filePdf = new File(pdfFilePath);
		
		if (!filePdf.exists()) {
			try {
				String pathNode = StringUtil.isNotEmpty(md2pdfPath) ? md2pdfPath : new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();
				
				String nodeCmd = StringUtil.replace("node {?1} {?2} {?3}", pathNode, mdFilePath, pdfFilePath);
				logger.debug("Node CMD: " + nodeCmd);
				Process process = Runtime.getRuntime().exec(nodeCmd);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s = null;
				while ((s = bufferedReader.readLine()) != null) {
					logger.debug(s);
				}
				process.waitFor();
				logger.debug("Md2pdf done!");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
		// response.setContentType("multipart/form-data");
		response.setContentType("application/x-msdownload;");
		response.addHeader("Content-Type", "text/html; charset=utf-8");
		String dnFileName = null;
		String dnFileLength = null;
		File dnFile = null;
		if("md".equalsIgnoreCase(type)) {
			dnFileName = StringUtil.replace("{?1}_{?2}", chatChannel.getChannel().getTitle(), mdFileName);
			dnFileLength = String.valueOf(fileMd.length());
			dnFile = fileMd;
		} else {
			dnFileName = StringUtil.replace("{?1}_{?2}", chatChannel.getChannel().getTitle(), pdfFileName);
			dnFileLength = String.valueOf(filePdf.length());
			dnFile = filePdf;
		}
		// 2.设置文件头：最后一个参数是设置下载文件名
		response.setHeader("Content-Disposition", "attachment; fileName="
				+ StringUtil.encodingFileName(dnFileName));
		response.setHeader("Content-Length", dnFileLength);

		java.io.BufferedInputStream bis = null;
		java.io.BufferedOutputStream bos = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(dnFile));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}
	
//	private boolean hasAuth(ChatChannel cc) {
//
//		if (cc == null) {
//			return false;
//		}
//
//		if (isSuperOrCreator(cc.getCreator().getUsername())) {
//			return true;
//		}
//
//		return AuthUtil.hasChannelAuth(cc.getChannel());
//	}
	
//	private boolean hasAuth(Channel c) {
//
//		if (c == null) {
//			return false;
//		}
//
//		if (!c.getPrivated()) {
//			return true;
//		}
//
//		User loginUser = new User(WebUtil.getUsername());
//		return c.getMembers().contains(loginUser);
//	}

	@RequestMapping(value = "share", method = RequestMethod.POST)
	@ResponseBody
	public RespBody share(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("href") final String href, @RequestParam("html") String html,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "users", required = false) String users,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "mails", required = false) String mails) {

		ChatChannel chatChannel2 = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel2)) {
			return RespBody.failed("您没有权限分享该沟通消息!");
		}

		final User loginUser = getLoginUser();

		final String html2 = StringUtil.replace(
				"<h1 style=\"color: blue;\">分享沟通消息: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
				"沟通消息链接", html);

		final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的沟通消息有分享到你";

		Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				if (user != null) {
					mail.addUsers(user);

					ChatDirect chatDirect = new ChatDirect();
					chatDirect.setChatTo(user);
					chatDirect.setContent(
							StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的沟通消息分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "沟通消息链接", href, chatChannel2.getContent()));

					chatDirectRepository.saveAndFlush(chatDirect);
				}
			});
		}
		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(name -> {
				Channel channel = channelRepository.findOneByName(name);
				if (channel != null) {
					channel.getMembers().forEach(user -> {
						mail.addUsers(user);
					});

					ChatChannel chatChannel = new ChatChannel();
					chatChannel.setChannel(channel);
					chatChannel.setContent(
							StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的沟通消息分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "沟通消息链接", href, chatChannel2.getContent()));

					chatChannelRepository.saveAndFlush(chatChannel);
				}
			});
		}

		if (StringUtil.isNotEmpty(mails)) {
			Stream.of(mails.split(",")).forEach(m -> {
				if (ValidateUtil.isEmail(m)) {
					mail.add(m);
				}
			});
		}

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-沟通消息分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser, "date",
							new Date(), "href", href, "title", title, "content", html2)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed();
	}
	
	@PostMapping("label/toggle")
	@ResponseBody
	public RespBody toggleLabel(@RequestParam("url") String url, @RequestParam("id") Long id,
			@RequestParam(value = "type", defaultValue = "Emoji") String type, @RequestParam("meta") String meta,
			@RequestParam("contentHtml") String contentHtml, @RequestParam("name") String name,
			@RequestParam(value = "desc", required = false) String desc) {

		if (StringUtil.isEmpty(name)) {
			return RespBody.failed("标签内容不能为空!");
		}

		if (name.length() > 15) {
			return RespBody.failed("标签内容不能超过15个字符!");
		}

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			return RespBody.failed("标签关联频道消息不存在!");
		}

		ChatLabel chatLabel = chatLabelRepository.findOneByNameAndChatChannel(name, chatChannel);

		User loginUser = getLoginUser();
		ChatLabelType chatLabelType = ChatLabelType.valueOf(type);

		String href = url + "?id=" + id;
		Mail mail = Mail.instance().addUsers(Arrays.asList(chatChannel.getCreator()), loginUser);
		String title = null;

		if (chatLabelType.equals(ChatLabelType.Emoji)) {
			title = StringUtil.replace(
					"{?1}对你的频道消息添加了表情: <img class=\"emoji\" style=\"width: 21px; height: 21px;\" src=\"{?2}\">",
					getLoginUserName(loginUser), meta);
		} else {
			title = StringUtil.replace("{?1}对你的频道消息添加了标签: {?2}", getLoginUserName(loginUser), meta);
		}

		if (chatLabel == null) {
			chatLabel = new ChatLabel();
			chatLabel.setName(name);
			chatLabel.setDescription(desc);
			chatLabel.setChatChannel(chatChannel);
			chatLabel.setType(chatLabelType);

			ChatLabel chatLabel2 = chatLabelRepository.saveAndFlush(chatLabel);

			chatLabel2.getVoters().add(loginUser);

			loginUser.getVoterChatLabels().add(chatLabel2);

			userRepository.saveAndFlush(loginUser);

			logWithProperties(Action.Create, Target.ChatLabel, chatLabel2.getId(), "name", name);

			try {
				mailSender
						.sendHtmlByQueue(
								String.format("TMS-沟通频道消息投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
												title, "content", contentHtml)),
								getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return RespBody.succeed(chatLabel2);
		} else {

			if (!StringUtil.isNotEmpty(desc) && !desc.equals(chatLabel.getDescription())) {
				chatLabel.setDescription(desc);
				chatLabel = chatLabelRepository.saveAndFlush(chatLabel);
			}

			Set<User> voters = chatLabel.getVoters();
			if (voters.contains(loginUser)) {
				loginUser.getVoterChatLabels().remove(chatLabel);
				voters.remove(loginUser);

				logWithProperties(Action.Vote, Target.ChatLabel, chatLabel.getId(), "name", name);
			} else {
				loginUser.getVoterChatLabels().add(chatLabel);
				voters.add(loginUser);

				logWithProperties(Action.UnVote, Target.ChatLabel, chatLabel.getId(), "name", name);

				try {
					mailSender
							.sendHtmlByQueue(
									String.format("TMS-沟通频道消息投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
									TemplateUtil.process("templates/mail/mail-dynamic",
											MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href,
													"title", title, "content", contentHtml)),
									getLoginUserName(loginUser), mail.get());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			userRepository.saveAndFlush(loginUser);

			return RespBody.succeed(chatLabel);
		}

	}
	
	@PostMapping("pin/toggle")
	@ResponseBody
	public RespBody togglePin(@RequestParam("id") Long id, @RequestParam("cid") Long cid,
			@RequestParam(value = "pin", defaultValue = "false") Boolean pin) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		Channel channel = channelRepository.findOne(cid);

		ChatPin chatPin = chatPinRepository.findOneByChannelAndChatChannel(channel, chatChannel);

		if (chatPin != null) {
			if (pin) {
				return RespBody.succeed(chatPin).code(Code.Created);
			}
			chatPinRepository.delete(chatPin);
			return RespBody.succeed(chatPin).code(Code.Deleted);
		} else {
			chatPin = new ChatPin();
			chatPin.setChannel(channel);
			chatPin.setChatChannel(chatChannel);

			ChatPin chatPin2 = chatPinRepository.saveAndFlush(chatPin);
			return RespBody.succeed(chatPin2).code(Code.Created);
		}
	}
	
	@GetMapping("pin/list")
	@ResponseBody
	public RespBody listPin(@RequestParam("cid") Long cid) {

		Channel channel = channelRepository.findOne(cid);

		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足!");
		}

		List<ChatPin> chatPins = chatPinRepository.findByChannel(channel);

		return RespBody.succeed(chatPins);

	}

	@PostMapping("reply/add")
	@ResponseBody
	public RespBody addReply(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam("content") String content, @RequestParam("contentHtml") String contentHtml,
			@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		ChatReply chatReply = new ChatReply();
		chatReply.setChatChannel(chatChannel);
		chatReply.setContent(content);

		ChatReply chatReply2 = chatReplyRepository.saveAndFlush(chatReply);
		
		// auto follow this chatchannel
		ChatChannelFollower chatChannelFollower = chatChannelFollowerRepository
				.findOneByChatChannelAndCreator(chatChannel, getLoginUser());

		if (chatChannelFollower == null) {
			chatChannelFollower = new ChatChannelFollower();
			chatChannelFollower.setChatChannel(chatChannel);

			chatChannelFollowerRepository.saveAndFlush(chatChannelFollower);

		}

		final String href = url + "?id=" + chatChannel.getId() + "&rid=" + chatReply2.getId();
		final String html = contentHtml; // StringUtil.md2Html(contentHtml, false, true);
		final User loginUser = getLoginUser();

		final Mail mail = Mail.instance();
		mail.addUsers(chatChannel.getChannel().getSubscriber(), loginUser);
		mail.addUsers(Arrays.asList(chatChannel.getCreator()), loginUser);
		mail.addUsers(chatChannel.getChatChannelFollowers().stream().map(ccf -> ccf.getCreator())
				.collect(Collectors.toList()), loginUser);

		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {
				ChatAt chatAt2 = chatAtRepository.findOneByChatChannelAndChatReplyAndAtUser(chatChannel, chatReply2, user);
				if (chatAt2 == null) {
					ChatAt chatAt = new ChatAt();
					chatAt.setChatChannel(chatChannel);
					chatAt.setChatReply(chatReply2);
					chatAt.setAtUser(user);

					chatAtList.add(chatAt);
				} else {
					chatAt2.setStatus(Status.New);

					chatAtList.add(chatAt2);
				}
			});

			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

		}

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-沟通频道回复消息@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic",
									MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
											"下面的沟通频道消息的回复消息中有@到你", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(chatReply2);

	}
	
	@PostMapping("reply/update")
	@ResponseBody
	public RespBody updateReply(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam("content") String content, @RequestParam("diff") String diff,
			@RequestParam("rid") Long rid) {

		ChatReply chatReply = chatReplyRepository.findOne(rid);

		if (!isSuperOrCreator(chatReply.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		String contentOld = chatReply.getContent();
		chatReply.setContent(content);

		ChatReply chatReply2 = chatReplyRepository.saveAndFlush(chatReply);

		logWithProperties(Action.Update, Target.ChatReply, rid, "content", contentOld);

		final String href = url + "?id=" + chatReply.getChatChannel().getId() + "&rid=" + chatReply2.getId();
		final User loginUser = getLoginUser();
		final String html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;

		final Mail mail = Mail.instance();
		mail.addUsers(chatReply.getChatChannel().getChannel().getSubscriber(), loginUser);
		mail.addUsers(Arrays.asList(chatReply.getChatChannel().getCreator()), loginUser);
		mail.addUsers(chatReply.getChatChannel().getChatChannelFollowers().stream().map(ccf -> ccf.getCreator())
				.collect(Collectors.toList()), loginUser);
		
		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {
				ChatAt chatAt2 = chatAtRepository.findOneByChatChannelAndChatReplyAndAtUser(chatReply2.getChatChannel(), chatReply2, user);
				if (chatAt2 == null) {
					ChatAt chatAt = new ChatAt();
					chatAt.setChatChannel(chatReply.getChatChannel());
					chatAt.setChatReply(chatReply2);
					chatAt.setAtUser(user);

					chatAtList.add(chatAt);
				} else {
					chatAt2.setStatus(Status.New);

					chatAtList.add(chatAt2);
				}
			});

			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

		}

		try {
			mailSender.sendHtmlByQueue(
					String.format("TMS-沟通频道回复消息编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/mail-dynamic",
							MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
									"下面编辑的沟通频道消息的回复消息中有@到你", "content", html)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(chatReply2);

	}
	
	@PostMapping("reply/remove")
	@ResponseBody
	public RespBody removeReply(@RequestParam("rid") Long rid) {

		ChatReply chatReply = chatReplyRepository.findOne(rid);

		if (!isSuperOrCreator(chatReply.getCreator())) {
			return RespBody.failed("权限不足!");
		}
		
		List<ChatAt> chatAts = chatAtRepository.findByChatReply(chatReply);
		chatAtRepository.delete(chatAts);
		chatAtRepository.flush();

		chatReplyRepository.delete(chatReply);

		logWithProperties(Action.Delete, Target.ChatReply, rid, "content", chatReply.getContent());

		return RespBody.succeed(rid);

	}
	
	@GetMapping("reply/list")
	@ResponseBody
	public RespBody listReply(@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		List<ChatReply> chatReplies = chatReplyRepository.findByChatChannelAndTypeAndStatusNot(chatChannel,
				ChatReplyType.ChatChannel, Status.Deleted);

		return RespBody.succeed(chatReplies);

	}
	
	@GetMapping("reply/get")
	@ResponseBody
	public RespBody getReply(@RequestParam("rid") Long rid) {

		ChatReply chatReply = chatReplyRepository.findOne(rid);

		if (!AuthUtil.hasChannelAuth(chatReply.getChatChannel())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(chatReply);

	}
	
	@GetMapping("reply/poll")
	@ResponseBody
	public RespBody pollReply(@RequestParam("id") Long id, @RequestParam(value = "rid", required = false) Long rid) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		List<ChatReply> chatReplies = null;
		if (rid == null) {
			chatReplies = chatReplyRepository.findByChatChannelAndTypeAndStatusNot(chatChannel,
					ChatReplyType.ChatChannel, Status.Deleted);
		} else {
			chatReplies = chatReplyRepository.findByChatChannelAndTypeAndStatusNotAndIdGreaterThan(chatChannel,
					ChatReplyType.ChatChannel, Status.Deleted, rid);
		}

		return RespBody.succeed(chatReplies);

	}

	@GetMapping("changed/check")
	@ResponseBody
	public RespBody checkChanged(@RequestParam("id") Long id, @RequestParam("version") Long version,
			@RequestParam("rcnt") Long cnt) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		boolean isVerEql = chatChannel.getVersion() == version.longValue();
		boolean isRcntEql = chatChannel.getChatReplies().size() == cnt.longValue();

		return RespBody.succeed(!isVerEql || !isRcntEql);

	}
	
	@PostMapping("follower/add")
	@ResponseBody
	public RespBody addFollower(@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		ChatChannelFollower chatChannelFollower = chatChannelFollowerRepository
				.findOneByChatChannelAndCreator(chatChannel, getLoginUser());

		if (chatChannelFollower != null) {
			return RespBody.failed("已经关注过!");
		}

		chatChannelFollower = new ChatChannelFollower();
		chatChannelFollower.setChatChannel(chatChannel);

		ChatChannelFollower chatChannelFollower2 = chatChannelFollowerRepository.saveAndFlush(chatChannelFollower);

		return RespBody.succeed(chatChannelFollower2);

	}
	
	@PostMapping("follower/remove")
	@ResponseBody
	public RespBody removeFollower(@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		ChatChannelFollower chatChannelFollower = chatChannelFollowerRepository
				.findOneByChatChannelAndCreator(chatChannel, getLoginUser());

		chatChannelFollowerRepository.delete(chatChannelFollower);

		return RespBody.succeed(chatChannelFollower);

	}
	
	@GetMapping("follower/list")
	@ResponseBody
	public RespBody listFollower(@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足!");
		}

		List<ChatChannelFollower> followers = chatChannelFollowerRepository.findByChatChannel(chatChannel);

		return RespBody.succeed(followers);

	}
}
