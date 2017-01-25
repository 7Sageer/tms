package com.lhjz.portal.util;

import java.util.Locale;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Template工具类.
 * 
 * @author xi
 * 
 * @date 2015年6月14日 上午11:09:34
 * 
 */
public class TemplateUtil {

	public static TemplateEngine templateEngine;

	static {

		templateEngine = new TemplateEngine();

		TemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setTemplateMode("LEGACYHTML5");
		resolver.setCharacterEncoding("UTF-8");
		resolver.setSuffix(".html");
		resolver.setCacheable(false);

		templateEngine.setTemplateResolver(resolver);
	}

	/**
	 * thymeleaf template engine handle template.
	 * 
	 * @param templateName
	 * @param modelMap
	 * @return
	 */
	public static String process(String templateName,
			Map<String, Object> modelMap) {

		return templateEngine.process(templateName,
				new Context(Locale.getDefault(), modelMap));
	}

}
