package org.meara.mybatis.plugin;

/**
 * 需要实现该接口用于获取租户id
 * Created by Meara on 2016/3/20.
 */
public interface TenantInfo {
    String getTenantId();
}
