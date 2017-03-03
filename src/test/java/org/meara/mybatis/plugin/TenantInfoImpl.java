package org.meara.mybatis.plugin;

import org.meara.mybatis.plugin.filter.MultiTenancyFilter;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;

import java.util.Properties;

/**
 * TenantInfo默认实现
 * Created by Meara on 2016/3/20.
 */
public class TenantInfoImpl implements TenantInfo{
    private static String tenantIdColumn="tenant_id";
    private static MultiTenancyFilter multiTenancyFilter = new RegExMultiTenancyFilter();


    @Override
    public String getTenantId() {
        return "2";
    }

    @Override
    public String toString() {
        return getTenantId();
    }

    @Override
    public TenantInfoImpl setFilterConfig(Properties properties) {
        multiTenancyFilter.setConfig(properties);
        return this;
    }

    @Override
    public MultiTenancyFilter getMultiTenancyFilter() {
        return multiTenancyFilter;
    }

    @Override
    public String getTenantIdColumn() {
        return tenantIdColumn;
    }

    @Override
    public TenantInfoImpl setTenantIdColumn(String tenantIdColumn) {
        TenantInfoImpl.tenantIdColumn=tenantIdColumn;
        return this;
    }

    public TenantInfoImpl setMultiTenancyFilter(MultiTenancyFilter multiTenancyFilter) {
        TenantInfoImpl.multiTenancyFilter = multiTenancyFilter;
        return this;
    }
}
