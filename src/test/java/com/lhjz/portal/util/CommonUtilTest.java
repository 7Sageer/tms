package com.lhjz.portal.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Ignore;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Code;
import com.lhjz.portal.pojo.Enum.GitAction;
import com.lhjz.portal.pojo.Enum.Prop;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.ToType;

public class CommonUtilTest {
	@Ignore
	@org.junit.Test
	public void word2html() throws IOException {
		DocumentConverter converter = new DocumentConverter();
		//		Result<String> result = converter.convertToHtml(new File("test.docx"));
		Result<String> result = converter.convertToHtml(CommonUtilTest.class.getResourceAsStream("test2.docx"));
		String html = result.getValue(); // The generated HTML
		System.out.println(html);
		//		Set<String> warnings = result.getWarnings(); // Any warnings during conversion
		//		System.out.println(warnings.toString());
	}

	@org.junit.Test
	public void testGitAction() {
		System.out.println(GitAction.valueOf("OPEN"));
		System.out.println(GitAction.valueOf("CLOSE"));
		System.out.println(GitAction.valueOf("ACCEPT"));
	}

	@Test
	public void replaceLinebreak() {
		String winString = "你好!\r\n好的啊\ndfgdfg";
		System.out.println(winString);
		String html = CommonUtil.replaceLinebreak(winString);
		System.out.println(html);
		Assert.assertTrue(html.contains("<br/>"));
	}

	@Test
	public void regex() {
		Assert.assertTrue("aa1".matches("^[a-z][a-z0-9]{2,3}$"));
		Assert.assertTrue("aaa1".matches("^[a-z][a-z0-9]{2,3}$"));
		Assert.assertFalse("a1".matches("^[a-z][a-z0-9]{2,3}$"));
		Assert.assertFalse("aaaa1".matches("^[a-z][a-z0-9]{2,3}$"));
	}

	@Test
	public void format() {
		String d = DateUtil.format(new Date(), DateUtil.FORMAT7);
		System.out.println(d);
		d = DateUtil.format(new Date(), DateUtil.FORMAT8);
		System.out.println(d);
	}

	@Test
	public void status() {
		System.out.println(Status.valueOf("New"));
		// System.out.println(Status.valueOf("New_"));

		System.out.println(Prop.Key.name());
	}

	@Test
	public void stringJoin() {
		System.out.println(StringUtil.join(",", new ArrayList<>()));
		Assert.assertEquals(StringUtil.join(",", new ArrayList<>()), "");
	}

	@Test
	public void replaceAll() {
		System.out.println("(111)[]{}".replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$"));
		System.out.println("111".replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$"));
		System.out.println("".replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$"));
		// String res = StringUtils.replaceEach("()[]{}",
		// new String[] { "(", ")" },
		// new String[] { "$", "$" });
		//
		// System.out.println(res);
	}

	@org.junit.Test
	public void substring() {

		System.out.println(Arrays.asList(new String[0]).size());
		System.out.println(Arrays.asList("aaa", "bbb").contains("aaa"));
		System.out.println("0123".substring(4));
	}

	@Test
	public void split() {
		System.out.println(Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains("fetch"));
	}

	@Test
	public void toType() {
		System.out.println(ToType.valueOf("Channel"));
	}

	@Test
	public void newLine() {
		System.out.println("11111\r\n22222\r\n33333");
		System.out.println("11111\r\n22222\r\n33333".replaceAll("\n", "<br/>"));
	}

	public static void main(String[] args) {
		//		System.out.println(ToType.valueOf("Channel"));
		//		System.out.println(SchedulePriority.Middle.name());
		//		System.out.println(StringUtil.replace("xx{~{?1}}xxx", "cccc"));
		//		
		//		System.out.println(StringUtil.parseUrl("http://jira00.tms.com/rest/api/2/issue/13701"));
		//	
		//		String read = JsonPath.read("{}", "$.name");
		//		System.out.println(read);
		//		
		//		System.out.println(Code.Created);
		//		System.out.println(Code.Created.equals(Code.Readed));
		//		System.out.println(Code.Created.equals(Code.Created));

		System.out.println("a-dsdf".matches("^[a-z][a-z0-9]{2,49}$"));
		System.out.println("adsdf121".matches("^[a-z][a-z0-9]{2,49}$"));
		System.out.println("af".matches("^[a-z][a-z0-9]{2,49}$"));

		System.out.println(JsonUtil.toJson(RespBody.succeed().code(Code.Created)));

		System.out.println(ThreadUtil.getCurrentAuditor());
		ThreadUtil.setCurrentAuditor("all");
		ThreadUtil.setCurrentAuditor("all2");
		System.out.println(ThreadUtil.getCurrentAuditor());
		System.out.println(ThreadUtil.getCurrentAuditor());
		ThreadUtil.clearCurrentAuditor();
		System.out.println(ThreadUtil.getCurrentAuditor());
	}

	// public static void main(String[] args) {
	// String d = DateUtil.format(new Date(), DateUtil.FORMAT7);
	// System.out.println(d);
	// }
}
