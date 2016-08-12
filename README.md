# mybatis-multi-tenancy
这是一个mybatis的基于共享数据表模式的多租户插件

**这仅仅是一个学习项目**
```xml
<!--
    plugins在配置文件中的位置必须符合要求，否则会报错，顺序如下:
    properties?, settings?,
    typeAliases?, typeHandlers?,
    objectFactory?,objectWrapperFactory?,
    plugins?,
    environments?, databaseIdProvider?, mappers?
-->
    <plugins>
        <!---->
        <plugin interceptor="com.kleen.plugin.MultiTenancy">
            <!--数据库中租户ID的列名-->
            <property name="tenantIdColumn" value="tenant_id"/>
            <!--数据库方言-->
            <property name="dialect" value="mysql"/>
            <!--实现租户信息接口(org.xue.TenantInfo)的实现类-->
            <property name="tenantInfo" value="com.kleen.plugin.TenantInfoImpl"/>
        </plugin>
    </plugins>
```
