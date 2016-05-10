package org.meara.mybatis.plugin;

import java.util.regex.Pattern;

/**
 * 正则过滤实现
 * Created by Meara on 2016/5/4.
 */
public class RegExMultiTenancyFilter implements MultiTenancyFilter {
    //默认过滤还是忽略 true表示按租户过滤
    private boolean filterDefault = false;
    private Pattern tablePatterns[];
    private Pattern statementPatterns[];

    @Override
    public boolean tableFilter(String table) {
        if (this.tablePatterns != null) {
            for (Pattern p : this.tablePatterns) {
                if (p.matcher(table).find()) return !filterDefault;
            }
        }
        return filterDefault;
    }

    @Override
    public boolean statementFilter(String statementId) {
        if (this.statementPatterns != null) {
            for (Pattern p : this.statementPatterns) {
                if (p.matcher(statementId).find()) return !filterDefault;
            }
        }
        return filterDefault;
    }

    private Pattern[] compile(String patterString) {
        if (patterString == null) return null;
        String[] patterStrings = patterString.split(",");
        Pattern[] patterns = new Pattern[patterStrings.length];
        for (int i = 0; i < patterStrings.length; i++) {
            Pattern pattern = Pattern.compile(patterStrings[i]);
            patterns[i] = pattern;
        }
        return patterns;
    }

    public RegExMultiTenancyFilter setFilterTablePatterns(String filterTablePatterns) {
        this.tablePatterns = compile(filterTablePatterns);
        return this;
    }

    public RegExMultiTenancyFilter setFilterStatementPatterns(String filterStatementPatterns) {
        this.statementPatterns = compile(filterStatementPatterns);
        return this;
    }

    public RegExMultiTenancyFilter setFilterDefault(boolean filterDefault) {
        this.filterDefault = filterDefault;
        return this;
    }

    public RegExMultiTenancyFilter setTablePatterns(Pattern[] tablePatterns) {
        this.tablePatterns = tablePatterns;
        return this;
    }

    public RegExMultiTenancyFilter setStatementPatterns(Pattern[] statementPatterns) {
        this.statementPatterns = statementPatterns;
        return this;
    }
}
