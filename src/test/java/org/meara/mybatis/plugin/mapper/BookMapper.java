package org.meara.mybatis.plugin.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Meara on 2016/3/17.
 */
public interface BookMapper {
    List<Map> selectAll();
    void insertBook(Map book);
    void insertSelect(Map book);
    Map selectById(Integer bid);
    void updateBook(Map book);
    void updateNoWhereBook(Map book);
}
