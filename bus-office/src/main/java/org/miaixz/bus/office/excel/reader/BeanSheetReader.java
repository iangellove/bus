/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.office.excel.reader;

import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.beans.copier.CopyOptions;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.office.excel.cell.CellEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取{@link Sheet}为bean的List列表形式
 *
 * @param <T> 结果类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanSheetReader<T> implements SheetReader<List<T>> {

    private final Class<T> beanClass;
    private final MapSheetReader mapSheetReader;

    /**
     * 构造
     *
     * @param headerRowIndex 标题所在行，如果标题行在读取的内容行中间，这行做为数据将忽略
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     * @param beanClass      每行对应Bean的类型
     */
    public BeanSheetReader(final int headerRowIndex, final int startRowIndex, final int endRowIndex, final Class<T> beanClass) {
        mapSheetReader = new MapSheetReader(headerRowIndex, startRowIndex, endRowIndex);
        this.beanClass = beanClass;
    }

    @Override
    public List<T> read(final Sheet sheet) {
        final List<Map<String, Object>> mapList = mapSheetReader.read(sheet);
        if (Map.class.isAssignableFrom(this.beanClass)) {
            return (List<T>) mapList;
        }

        final List<T> beanList = new ArrayList<>(mapList.size());
        final CopyOptions copyOptions = CopyOptions.of().setIgnoreError(true);
        for (final Map<String, Object> map : mapList) {
            beanList.add(BeanKit.toBean(map, this.beanClass, copyOptions));
        }
        return beanList;
    }

    /**
     * 设置单元格值处理逻辑
     * 当Excel中的值并不能满足我们的读取要求时，通过传入一个编辑接口，可以对单元格值自定义，例如对数字和日期类型值转换为字符串等
     *
     * @param cellEditor 单元格值处理接口
     */
    public void setCellEditor(final CellEditor cellEditor) {
        this.mapSheetReader.setCellEditor(cellEditor);
    }

    /**
     * 设置是否忽略空行
     *
     * @param ignoreEmptyRow 是否忽略空行
     */
    public void setIgnoreEmptyRow(final boolean ignoreEmptyRow) {
        this.mapSheetReader.setIgnoreEmptyRow(ignoreEmptyRow);
    }

    /**
     * 设置标题行的别名Map
     *
     * @param headerAlias 别名Map
     */
    public void setHeaderAlias(final Map<String, String> headerAlias) {
        this.mapSheetReader.setHeaderAlias(headerAlias);
    }

    /**
     * 增加标题别名
     *
     * @param header 标题
     * @param alias  别名
     */
    public void addHeaderAlias(final String header, final String alias) {
        this.mapSheetReader.addHeaderAlias(header, alias);
    }

}
