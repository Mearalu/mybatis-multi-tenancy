package org.meara.mybatis.plugin.filter;

import org.meara.mybatis.plugin.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 正则过滤器实现
 * Created by Meara on 2016/5/4.
 */
public class RegExMultiTenancyFilter implements MultiTenancyFilter {
    private static final Logger logger = LoggerFactory.getLogger(RegExMultiTenancyFilter.class);
    //默认过滤还是忽略 true表示按租户过滤
    private boolean filterDefault = false;
    private Pattern tablePatterns[];
    private Pattern statementPatterns[];

    @Override
    public void setConfig(Properties properties) {
        this.setFilterStatementRegexStr(ConfigUtil.getCleanProperty(properties, "filterStatementRegexStr"));
        this.setFilterTableRegexStr(ConfigUtil.getCleanProperty(properties, "filterTableRegexStr"));
        String filterDefault = ConfigUtil.getCleanProperty(properties, "filterDefault");
        if (filterDefault != null) {
            this.setFilterDefault(Boolean.parseBoolean(filterDefault));
        }
    }

    @Override
    public boolean doTableFilter(String table) {
        boolean isOk = filterDefault;
        if (this.tablePatterns != null) {
            for (Pattern p : this.tablePatterns) {
                if (p.matcher(table).find()) {
                    isOk = !filterDefault;
                    break;
                }
            }
        }
        logger.debug("table:{}  isOK:{}   tablePatterns:{}", table, isOk, tablePatterns);
        return isOk;
    }

    @Override
    public boolean doStatementFilter(String statementId) {
        boolean isOk = filterDefault;
        if (this.statementPatterns != null) {
            for (Pattern p : this.statementPatterns) {
                if (p.matcher(statementId).find()) {
                    isOk = !filterDefault;
                    break;
                }
            }
        }
        logger.debug("statementId:{}  isOK:{}   statementPatterns:{}", statementId, isOk, statementPatterns);
        return isOk;
    }

    public static Pattern[] compile(String patterString) {
        if (patterString == null) {
            return new Pattern[]{};
        }
        String[] patterStrings = patterString.split(",");
        return compile(patterStrings);
    }

    public static Pattern[] compile(String[] patterStrings) {
        if (patterStrings == null) {
            return new Pattern[]{};
        }
        Pattern[] patterns = new Pattern[patterStrings.length];
        for (int i = 0; i < patterStrings.length; i++) {
            Pattern pattern = Pattern.compile(patterStrings[i]);
            patterns[i] = pattern;
        }
        return patterns;
    }

    public RegExMultiTenancyFilter setFilterTableRegexStr(String tableRegexStr) {
        if (tableRegexStr == null) {
            return this;
        }
        this.tablePatterns = compile(tableRegexStr);
        return this;
    }

    public RegExMultiTenancyFilter setFilterTableRegexArr(String[] tableRegexArr) {
        if (tableRegexArr == null) {
            return this;
        }
        this.tablePatterns = compile(tableRegexArr);
        return this;
    }

    public RegExMultiTenancyFilter setFilterStatementRegexStr(String statementRegexStr) {
        if (statementRegexStr == null) {
            return this;
        }
        this.statementPatterns = compile(statementRegexStr);
        return this;
    }

    public RegExMultiTenancyFilter setFilterStatementRegexArr(String[] statementRegexArr) {
        if (statementRegexArr == null) {
            return this;
        }
        this.statementPatterns = compile(statementRegexArr);
        return this;
    }

    public RegExMultiTenancyFilter setFilterDefault(boolean filterDefault) {
        this.filterDefault = filterDefault;
        return this;
    }
}
