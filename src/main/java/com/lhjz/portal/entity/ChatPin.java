/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString()
@EqualsAndHashCode(of = "id")
public class ChatPin implements Serializable {

	private static final long serialVersionUID = -6545388037941646407L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "creator")
	@CreatedBy
	private User creator;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Version
	private long version;

	@OneToOne
	@JoinColumn(name = "chat_channel")
	private ChatChannel chatChannel;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

}
