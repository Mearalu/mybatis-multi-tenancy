# mybatis-multi-tenancy

这是一个mybatis的基于共享数据表模式的多租户插件

#### 目前在小系统使用单并未进行全面测试

因为没人关注所以一直没上传.看到@kleen提交的pr还是非常的感谢(本人收到的第一个pr),所以决定把前段时间的成果稍加修改了下重新上传了, 当然因为系统中不允许更改数据租户 update语句并未去实现它

#### 2021-10-17

增加删除语句支持，删除、更新、查询均支持多租户身份，插入时需要获取当前租户身份 升级依赖版本

#### 2017-03-03

添加insert语句支持 现在支持用逗号隔开租户ID(多租户身份) 但不建议使用in语句可能会影响性能 改为使用h2数据库进行测试,更改配置类新增多种配置方式灵活配置,以及一些其他改动

#### 2016-11-17

修复update语句

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
        <property name="filterTableRegexStr" value=""/>
        <!--statement匹配正则表达式(filterDefault为true,则这里为忽略UserMapper)-->
        <property name="filterStatementRegexStr" value="^org\.meara\.mybatis\.plugin\.mapper\.UserMapper"/>
    </plugin>
</plugins>
```

spring配置方式1(推荐)

```xml

<bean name="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="plugins">
        <array>
            <bean class="org.meara.mybatis.plugin.MultiTenancy">
                <constructor-arg>
                    <bean class="org.meara.mybatis.plugin.TenantInfoImpl">
                        <property name="multiTenancyFilter">
                            <bean class="org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter">
                                <!--默认值-->
                                <property name="filterDefault" value="false"/>
                                <property name="filterStatementRegexArr">
                                    <list>
                                        <value>^com\.meara\.mapper\.FeePriceMapper</value>
                                    </list>
                                </property>
                                <property name="filterTableRegexArr">
                                    <list>
                                        <value>fee_price</value>
                                        <value>fee_item</value>
                                    </list>
                                </property>
                            </bean>
                        </property>
                    </bean>
                </constructor-arg>
                <property name="properties">
                    <value>
                        tenantIdColumn=tenant_id
                        dialect=mysql
                        tenantInfo=org.meara.mybatis.plugin.TenantInfoImpl
                    </value>
                </property>
            </bean>
        </array>
    </property>
</bean>
```

spring配置方式2(简单筛选时可以使用此方式)

```xml

<bean name="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="plugins">
        <array>
            <bean class="org.meara.mybatis.plugin.MultiTenancy">
                <property name="properties">
                    <value>
                        tenantIdColumn=tenant_id
                        dialect=mysql
                        tenantInfo=org.meara.mybatis.plugin.TenantInfoImpl
                        filterDefault=false
                        filterStatementRegexStr=^com\.meara\.mapper\.FeePrice
                        filterTableRegexStr=fee_price,fee_item
                    </value>
                </property>
            </bean>
        </array>
    </property>
</bean>
```
