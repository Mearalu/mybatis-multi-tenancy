# mybatis-multi-tenancy
这是一个mybatis的基于共享数据表模式的多租户插件

#### 目前在小系统使用单并未进行全面测试
   因为没人关注所以一直没上传.看到@kleen提交的pr还是非常的感谢(本人收到的第一个pr),所以决定把前段时间的成果稍加修改了下重新上传了,
当然因为系统中不允许更改数据租户 update语句并未去实现它

添加insert语句支持
现在支持用逗号隔开租户ID(多租户身份) 但不建议使用in语句可能会影响性能
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
        <plugin interceptor="org.xue.MultiTenancy">
            <!--数据库中租户ID的列名-->
            <property name="tenantIdColumn" value="tenant_id"/>
            <!--数据库方言-->
            <property name="dialect" value="mysql"/>
            <!--实现租户信息接口(TenantInfo)的实现类-->
            <property name="tenantInfo" value="org.meara.mybatis.plugin.TenantInfoImpl"/>

            <!--默认过滤还是忽略,true表示按租户过滤  默认值为false-->
            <property name="filterDefault" value="true"/>
            <!--table匹配正则表达式 逗号分隔-->
            <property name="filterTablePatterns" value=""/>
            <!--statement匹配正则表达式(filterDefault为true,则这里为忽略UserMapper)-->
            <property name="filterStatementPatterns" value="^org\.meara\.mybatis\.plugin\.mapper\.UserMapper"/>
        </plugin>
    </plugins>
```
