/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.TranslateForm;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
public class Mail {

	static Logger logger = LoggerFactory.getLogger(Mail.class);

	private Set<MailAddr> set = new HashSet<>();

	private Map<String, String> mapHref = new HashMap<String, String>();

	private Map<String, String> map = new HashMap<String, String>();

	private Mail() {
	}

	public static Mail instance() {
		return new Mail();
	}

	public Mail parseTranslateUpdated(TranslateForm translateForm,
			Translate translate) {
		List<String> labels = translate.getLabels().stream().map((l) -> {
			return l.getName();
		}).collect(Collectors.toList());

		Collections.sort(labels);
		String oldLabels = StringUtil.join(",", labels);

		List<String> tags = new ArrayList<>();
		if (StringUtil.isNotEmpty(translateForm.getTags())) {
			tags = Arrays.asList(translateForm.getTags().split(","));
		}
		Collections.sort(tags);
		String newTags = StringUtil.join(",", tags);

		if (!translate.getKey().equals(translateForm.getKey())) {
			this.put("翻译名称", translate.getKey() + SysConstant.CHANGE_TO
					+ translateForm.getKey());
		}

		if (!oldLabels.equals(newTags)) {
			this.put("翻译标签", oldLabels + SysConstant.CHANGE_TO + newTags);

		}

		return this;
	}

	public Mail parseTranslateForm(TranslateForm translateForm) {

		this.put("翻译名称", translateForm.getKey());
		this.put("翻译标签", translateForm.getTags());

		return this;
	}

	public Mail parseTranslate(Translate translate) {

		map.put("翻译名称", translate.getKey());

		List<String> list = new ArrayList<String>();
		for (Label label : translate.getLabels()) {
			list.add(label.getName());
		}
		this.put("翻译标签", StringUtil.join(",", list));

		Set<TranslateItem> translateItems = translate.getTranslateItems();
		for (TranslateItem translateItem : translateItems) {
			String name = translateItem.getLanguage().getDescription() + "["
					+ translateItem.getLanguage().getName() + "]";
			this.put(name, translateItem.getContent());
		}

		return this;
	}

	public Mail put(String name, Object value) {

		map.put(name, String.valueOf(value));

		return this;
	}

	public String body() {

		List<String> list = new ArrayList<String>();
		for (String name : map.keySet()) {
			list.add(name + ": " + map.get(name));
		}

		return StringUtil.join("<br/>", list);
	}

	/**
	 * 添加发邮件用户的邮件地址
	 * 
	 * @param objs
	 * @return
	 */
	public Mail add(MailAddr... mails) {

		if (mails != null) {
			for (MailAddr mail : mails) {
				this.set.add(mail);
			}
		}

		return this;
	}
	
	public Mail add(String... mails) {

		if (mails != null) {
			for (String mail : mails) {
				String[] arr = StringUtil.split(mail, "@");
				String personal = (arr != null && arr.length > 0) ? arr[0] : mail;
				this.add(new MailAddr(mail, personal));
			}
		}

		return this;
	}

	/**
	 * 添加发邮件用户
	 * 
	 * @param users
	 * @return
	 */
	public Mail addUsers(User... users) {

		if (users != null) {
			for (User user : users) {
				if (user.isEnabled()) {
					String personal = user.getName();
					if (StringUtil.isEmpty(personal)) {
						personal = user.getUsername();
					}

					this.add(new MailAddr(user.getMails(), personal));
				}
			}
		}

		return this;
	}
	
	/**
	 * 添加发邮件用户
	 * 
	 * @param users
	 * @return
	 */
	public Mail addUsers(Collection<User> users, User... exclusive) {

		if (users != null) {
			for (User user : users) {
				if (user.isEnabled() && !isContained(user, exclusive)) {
					String personal = user.getName();
					if (StringUtil.isEmpty(personal)) {
						personal = user.getUsername();
					}

					this.add(new MailAddr(user.getMails(), personal));
				}
			}
		}

		return this;
	}
	
	private boolean isContained(User user, User... users) {

		if (user != null && users != null && users.length > 0) {
			for (int i = 0; i < users.length; i++) {
				if (user.equals(users[i])) {
					return true;
				}
			}
		}

		return false;
	}

	public Mail addWatchers(Translate translate) {

		// 项目关注者
		this.addWatchers(translate.getProject());

		// 翻译关注者
		Set<User> watchers = translate.getWatchers();
		Set<User> enabledUsers = watchers.stream().filter(user -> user.isEnabled()).collect(Collectors.toSet());
		this.addUsers(enabledUsers);

		return this;
	}

	public Mail addWatchers(Project project) {

		// 项目关注者
		Set<User> watchers = project.getWatchers();
		Set<User> enabledUsers = watchers.stream().filter(user -> user.isEnabled()).collect(Collectors.toSet());
		this.addUsers(enabledUsers);

		return this;
	}

	public Mail removeUsers(User... users) {

		for (User user : users) {
			this.set.remove(new MailAddr(user.getMails(), null));
		}

		return this;
	}

	public Mail remove(String... mails) {

		for (String mail : mails) {
			this.set.remove(new MailAddr(mail, null));
		}

		return this;
	}
	
	public MailAddr[] get() {
		return this.set.toArray(new MailAddr[0]);
	}

	public boolean isEmpty() {
		return this.set.size() == 0;
	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId,
			List<Translate> translates) {

		for (Translate translate2 : translates) {
			this.addHref(name, baseURL, translateAction, projectId, translate2);
		}

	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId, String search) {

		String href = baseURL + translateAction + "?projectId=" + projectId
				+ "&search=" + search;
		this.mapHref.put(name,
				StringUtil.replaceByKV(
						"<a target=\"_blank\" href=\"{href}\">{text}</a>",
						"href", href, "text", href));

	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId,
			Translate translate) {

		String href = baseURL + translateAction + "?projectId=" + projectId
				+ "&id=" + translate.getId();
		this.mapHref.put(name, StringUtil.replaceByKV(
				"<a target=\"_blank\" href=\"{href}\">{text}</a>",
				"href", href, "text", href));
	}

	public String hrefs() {

		StringBuilder sb = new StringBuilder();
		for (String name : mapHref.keySet()) {
			sb.append("<b>").append(name).append(": </b>")
					.append(mapHref.get(name)).append("<br/>");
		}

		return sb.toString();
	}

}
