/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class UploadResult {

	String link;

	String error;

}
