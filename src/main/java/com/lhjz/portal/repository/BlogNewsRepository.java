/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.BlogNews;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogNewsRepository extends JpaRepository<BlogNews, Long> {

	Page<BlogNews> findByToAndUsernameNotAndStatusNot(String to, String username, Status status, Pageable pageable);
}
