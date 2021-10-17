package org.meara.mybatis.plugin.test.spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meara.mybatis.plugin.mapper.BookMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Spring 配置测试
 * Created by meara on 2017/03/03.
 */
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(value = {"classpath:spring-root-context.xml"})
public class SpringTest {

    @Resource
    private BookMapper bookMapper;

    /**
     * 匹配测试
     */
    @Test
    public void queryTest() {
        List<Map> books = bookMapper.selectAll();
        Assertions.assertEquals(1, books.size());
    }

    @Test
    public void insertTest() {
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid", 3);
        newBook.put("bookName", "水浒传");
        bookMapper.insertBook(newBook);
        Map book = bookMapper.selectById(3);
        Assertions.assertEquals("2", Objects.toString(book.get("TENANT_ID")));
    }

    @Test
    public void insertSelectTest() {
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid", 4);
        bookMapper.insertSelect(newBook);
        Map book = bookMapper.selectById(4);
        Assertions.assertEquals("2", Objects.toString(book.get("TENANT_ID")));
    }

    @Test
    public void updateTest() {
        Map map = new HashMap();
        map.put("bid", "2");
        map.put("bookName", "封神榜");
        bookMapper.updateBook(map);
    }
}
