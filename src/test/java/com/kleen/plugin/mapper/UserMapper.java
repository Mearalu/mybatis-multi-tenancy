package com.kleen.plugin.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Meara on 2016/3/17.
 */
public interface UserMapper {
    List<Map> selectAll();
    List<Map> selectWhere(Long id);
    void update();

    void insert(Map map);
}
