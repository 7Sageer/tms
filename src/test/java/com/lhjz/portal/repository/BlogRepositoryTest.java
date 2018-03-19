package com.lhjz.portal.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.lhjz.portal.Application;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.pojo.Enum.Status;

@SpringBootTest(classes = Application.class)
public class BlogRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	BlogRepository blogRepository;

	UserRepository userRepository;

//	@Test
	public void search() {
		List<Blog> blogs = blogRepository.search("admin", "%s%", 0, 5);
		System.out.println(blogs.size());
		System.out.println(blogs.get(0).getCreator().getUsername());
		System.out.println(blogRepository.countSearch("admin", "%s%"));
	}
	
//	@Test
	public void findByStatusNotAndTags_nameIn() {
		List<Blog> page = blogRepository.findByStatusNotAndTags_nameIn(Status.Deleted, Arrays.asList("linux", "java", "tms"), new Sort(Direction.DESC, "updateDate"));
		System.out.println(page.size());
	}
}
