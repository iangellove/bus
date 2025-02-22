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
package org.miaixz.bus.pager.builtin;

import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.Querying;

import java.util.Properties;

/**
 * 基础分页方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PageMethod {

    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();
    protected static boolean DEFAULT_COUNT = true;

    /**
     * 获取 Page 参数
     *
     * @return
     */
    public static <T> Page<T> getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * 设置 Page 参数
     *
     * @param page
     */
    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * 获取任意查询方法的count总数
     *
     * @param select
     * @return
     */
    public static long count(Querying select) {
        //单纯count查询时禁用异步count
        Page<?> page = startPage(1, -1, true).disableAsyncCount();
        select.doSelect();
        return page.getTotal();
    }

    /**
     * 开始分页
     *
     * @param params
     */
    public static <E> Page<E> startPage(Object params) {
        Page<E> page = PageObject.getPageFromObject(params, true);
        //当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNo   页码
     * @param pageSize 每页显示数量
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize) {
        return startPage(pageNo, pageSize, DEFAULT_COUNT);
    }

    /**
     * 开始分页
     *
     * @param pageNo   页码
     * @param pageSize 每页显示数量
     * @param count    是否进行count查询
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, boolean count) {
        return startPage(pageNo, pageSize, count, null, null);
    }

    /**
     * 开始分页
     *
     * @param pageNo   页码
     * @param pageSize 每页显示数量
     * @param orderBy  排序
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNo, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNo       页码
     * @param pageSize     每页显示数量
     * @param count        是否进行count查询
     * @param reasonable   分页合理化,null时用默认配置
     * @param pageSizeZero true且pageSize=0时返回全部结果，false时分页,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page<E> page = new Page<E>(pageNo, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        //当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset 起始位置，偏移位置
     * @param limit  每页显示数量
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, DEFAULT_COUNT);
    }

    /**
     * 开始分页
     *
     * @param offset 起始位置，偏移位置
     * @param limit  每页显示数量
     * @param count  是否进行count查询
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count) {
        Page<E> page = new Page<E>(new int[]{offset, limit}, count);
        // 当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page<?> page = getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
            if (page.getPageSizeZero() != null && page.getPageSizeZero() && page.getPageSize() == 0) {
                page.setOrderByOnly(true);
            }
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            setLocalPage(page);
        }
    }

    /**
     * 设置参数
     *
     * @param properties 插件属性
     */
    protected static void setStaticProperties(Properties properties) {
        // defaultCount，这是一个全局生效的参数，多数据源时也是统一的行为
        if (properties != null) {
            DEFAULT_COUNT = Boolean.valueOf(properties.getProperty("defaultCount", "true"));
        }
    }

}
