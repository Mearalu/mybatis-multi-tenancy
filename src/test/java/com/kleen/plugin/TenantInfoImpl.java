package com.kleen.plugin;

/**
 * 自行实现TenantInfo获取
 * Created by Meara on 2016/3/20.
 */
public class TenantInfoImpl  implements TenantInfo{
    @Override
    public String getTenantId() {
        return "2";
    }
}
