/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.entity;

import jakarta.persistence.Table;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.xyz.StringKit;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityTable {

    public static final Pattern DELIMITER = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");
    /**
     * 属性和列对应
     */
    protected Map<String, EntityColumn> propertyMap;
    private String name;
    private String catalog;
    private String schema;
    private String orderByClause;
    private String baseSelect;
    /**
     * 实体类 => 全部列属性
     */
    private LinkedHashSet<EntityColumn> entityClassColumns;
    /**
     * 实体类 => 主键信息
     */
    private LinkedHashSet<EntityColumn> entityClassPKColumns;
    /**
     * useGenerator包含多列的时候需要用到
     */
    private List<String> keyProperties;
    private List<String> keyColumns;
    /**
     * resultMap对象
     */
    private ResultMap resultMap;
    /**
     * 类
     */
    private Class<?> entityClass;

    public EntityTable(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 生成当前实体的resultMap对象
     *
     * @param configuration 配置信息
     * @return the object
     */
    public ResultMap getResultMap(Configuration configuration) {
        if (this.resultMap != null) {
            return this.resultMap;
        }
        if (entityClassColumns == null || entityClassColumns.size() == 0) {
            return null;
        }
        List<ResultMapping> resultMappings = new ArrayList<>();
        for (EntityColumn entityColumn : entityClassColumns) {
            String column = entityColumn.getColumn();
            // 去掉可能存在的分隔符
            Matcher matcher = DELIMITER.matcher(column);
            if (matcher.find()) {
                column = matcher.group(1);
            }
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, entityColumn.getProperty(), column, entityColumn.getJavaType());
            if (entityColumn.getJdbcType() != null) {
                builder.jdbcType(entityColumn.getJdbcType());
            }
            if (entityColumn.getTypeHandler() != null) {
                try {
                    builder.typeHandler(getInstance(entityColumn.getJavaType(), entityColumn.getTypeHandler()));
                } catch (Exception e) {
                    throw new MapperException(e);
                }
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (entityColumn.isId()) {
                flags.add(ResultFlag.ID);
            }
            builder.flags(flags);
            resultMappings.add(builder.build());
        }
        ResultMap.Builder builder = new ResultMap.Builder(configuration, "BaseMapperResultMap", this.entityClass, resultMappings, true);
        this.resultMap = builder.build();
        return this.resultMap;
    }

    /**
     * 初始化 - Condition 会使用
     */
    public void initPropertyMap() {
        propertyMap = new HashMap<>(getEntityClassColumns().size());
        for (EntityColumn column : getEntityClassColumns()) {
            propertyMap.put(column.getProperty(), column);
        }
    }

    /**
     * 实例化TypeHandler
     *
     * @param <T>              泛型对象
     * @param javaTypeClass    Java类型
     * @param typeHandlerClass 拦截处理对象
     * @return the object
     */
    public <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (TypeHandler<T>) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
                // ignored
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (TypeHandler<T>) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }

    public String getBaseSelect() {
        return baseSelect;
    }

    public void setBaseSelect(String baseSelect) {
        this.baseSelect = baseSelect;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public LinkedHashSet<EntityColumn> getEntityClassColumns() {
        return entityClassColumns;
    }

    public void setEntityClassColumns(LinkedHashSet<EntityColumn> entityClassColumns) {
        this.entityClassColumns = entityClassColumns;
    }

    public LinkedHashSet<EntityColumn> getEntityClassPKColumns() {
        return entityClassPKColumns;
    }

    public void setEntityClassPKColumns(LinkedHashSet<EntityColumn> entityClassPKColumns) {
        this.entityClassPKColumns = entityClassPKColumns;
    }

    public String[] getKeyColumns() {
        if (keyColumns != null && keyColumns.size() > 0) {
            return keyColumns.toArray(new String[]{});
        }
        return new String[]{};
    }

    public void setKeyColumns(String keyColumn) {
        if (this.keyColumns == null) {
            this.keyColumns = new ArrayList<>();
            this.keyColumns.add(keyColumn);
        } else {
            this.keyColumns.add(keyColumn);
        }
    }

    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    public String[] getKeyProperties() {
        if (keyProperties != null && keyProperties.size() > 0) {
            return keyProperties.toArray(new String[]{});
        }
        return new String[]{};
    }

    public void setKeyProperties(String keyProperty) {
        if (this.keyProperties == null) {
            this.keyProperties = new ArrayList<>();
            this.keyProperties.add(keyProperty);
        } else {
            this.keyProperties.add(keyProperty);
        }
    }

    public void setKeyProperties(List<String> keyProperties) {
        this.keyProperties = keyProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getPrefix() {
        if (StringKit.isNotEmpty(catalog)) {
            return catalog;
        }
        if (StringKit.isNotEmpty(schema)) {
            return schema;
        }
        return Normal.EMPTY;
    }

    public Map<String, EntityColumn> getPropertyMap() {
        return propertyMap;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setTable(Table table) {
        this.name = table.name();
        this.catalog = table.catalog();
        this.schema = table.schema();
    }

}
