package org.meara.mybatis.plugin.test.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meara.mybatis.plugin.mapper.BookMapper;
import org.meara.mybatis.plugin.mapper.UserMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Spring 配置测试
 * Created by meara on 2017/03/03.
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:spring-root-context.xml"})
public class SpringTest {

    @Resource
    private BookMapper bookMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 匹配测试
     */
    @Test
    public void queryTest() {
        List<Map> books = bookMapper.selectAll();
        Assert.assertTrue(books.size() == 1);
    }

    @Test
    public void insertTest() {
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid", 3);
        newBook.put("bookName", "水浒传");
        bookMapper.insertBook(newBook);
        Map book = bookMapper.selectById(3);
        Assert.assertTrue("2".equals(Objects.toString(book.get("TENANT_ID"))));
    }

    @Test
    public void insertSelectTest() {
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid", 4);
        bookMapper.insertSelect(newBook);
        Map book = bookMapper.selectById(4);
        Assert.assertTrue("2".equals(Objects.toString(book.get("TENANT_ID"))));
    }

    @Test
    public void updateTest() {
        Map map = new HashMap();
        map.put("bid", "2");
        map.put("bookName", "封神榜");
        bookMapper.updateBook(map);
    }
}
