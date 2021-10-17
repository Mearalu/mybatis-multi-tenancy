package org.meara.mybatis.plugin;

import org.meara.mybatis.plugin.filter.MultiTenancyFilter;

import java.util.List;
import java.util.Properties;

/**
 * 需要实现该接口用于获取租户id
 * Created by Meara on 2016/3/20.
 */
public interface TenantInfo {
    TenantInfo setFilterConfig(Properties properties);

    String getCurrentTenantId();

    List<String> getTenantIds();

    MultiTenancyFilter getMultiTenancyFilter();

    String getTenantIdColumn();

    TenantInfo setTenantIdColumn(String tenantIdColumn);

}
