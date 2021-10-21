package org.ssssssss.magicapi.modules.mybatis;

/**
 * 对应XML中 <where>
 * @author zhangxu
 * @version : 2020-12-05
 */
public class WhereSqlNode extends TrimSqlNode {
    public WhereSqlNode() {
        this.prefix = "WHERE";
        this.prefixOverrides = "AND | OR | AND\n| OR\n| AND\r| OR\r| AND\t| OR\t";
    }
}