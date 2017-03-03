package org.meara.mybatis.plugin.util;

import java.util.Properties;

/**
 * Created by meara on 2016/11/08.
 */
public class ConfigUtil {
    public static String getCleanProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        return value == null || value.isEmpty() ? null : value;
    }
}
