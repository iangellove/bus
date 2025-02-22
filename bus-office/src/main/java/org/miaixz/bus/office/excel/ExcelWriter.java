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
package org.miaixz.bus.office.excel;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.center.map.multi.RowKeyTable;
import org.miaixz.bus.core.center.map.multi.Table;
import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumMap;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.office.excel.cell.CellEditor;
import org.miaixz.bus.office.excel.cell.CellKit;
import org.miaixz.bus.office.excel.cell.CellLocation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel 写入器
 * 此工具用于通过POI将数据写出到Excel，此对象可完成以下两个功能
 *
 * <pre>
 * 1. 编辑已存在的Excel，可写出原Excel文件，也可写出到其它地方（到文件或到流）
 * 2. 新建一个空的Excel工作簿，完成数据填充后写出（到文件或到流）
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriter extends ExcelBase<ExcelWriter> {

    /**
     * 当前行
     */
    private final AtomicInteger currentRow;
    /**
     * 是否只保留别名对应的字段
     */
    private boolean onlyAlias;
    /**
     * 标题顺序比较器
     */
    private Comparator<String> aliasComparator;
    /**
     * 样式集，定义不同类型数据样式
     */
    private StyleSet styleSet;
    /**
     * 标题项对应列号缓存，每次写标题更新此缓存
     */
    private Map<String, Integer> headLocationCache;
    /**
     * 单元格值处理接口
     */
    private CellEditor cellEditor;

    /**
     * 构造，默认生成xls格式的Excel文件
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     */
    public ExcelWriter() {
        this(false);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param isXlsx 是否为xlsx格式
     */
    public ExcelWriter(final boolean isXlsx) {
        this(WorkbookKit.createBook(isXlsx), null);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFilePath 目标文件路径，可以不存在
     */
    public ExcelWriter(final String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param isXlsx    是否为xlsx格式
     * @param sheetName sheet名，第一个sheet名并写出到此sheet，例如sheet1
     */
    public ExcelWriter(final boolean isXlsx, final String sheetName) {
        this(WorkbookKit.createBook(isXlsx), sheetName);
    }

    /**
     * 构造
     *
     * @param destFilePath 目标文件路径，可以不存在
     * @param sheetName    sheet名，第一个sheet名并写出到此sheet，例如sheet1
     */
    public ExcelWriter(final String destFilePath, final String sheetName) {
        this(FileKit.file(destFilePath), sheetName);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFile 目标文件，可以不存在
     */
    public ExcelWriter(final File destFile) {
        this(destFile, null);
    }

    /**
     * 构造
     *
     * @param destFile  目标文件，可以不存在
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public ExcelWriter(final File destFile, final String sheetName) {
        this(WorkbookKit.createBookForWriter(destFile), sheetName);
        this.destFile = destFile;
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param workbook  {@link Workbook}
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public ExcelWriter(final Workbook workbook, final String sheetName) {
        this(WorkbookKit.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param sheet {@link Sheet}
     */
    public ExcelWriter(final Sheet sheet) {
        super(sheet);
        this.styleSet = new StyleSet(workbook);
        this.currentRow = new AtomicInteger(0);
    }

    /**
     * 设置单元格值处理逻辑
     * 当Excel中的值并不能满足我们的读取要求时，通过传入一个编辑接口，可以对单元格值自定义，例如对数字和日期类型值转换为字符串等
     *
     * @param cellEditor 单元格值处理接口
     * @return this
     */
    public ExcelWriter setCellEditor(final CellEditor cellEditor) {
        this.cellEditor = cellEditor;
        return this;
    }

    @Override
    public ExcelWriter setSheet(final int sheetIndex) {
        // 切换到新sheet需要重置开始行
        reset();
        return super.setSheet(sheetIndex);
    }

    @Override
    public ExcelWriter setSheet(final String sheetName) {
        // 切换到新sheet需要重置开始行
        reset();
        return super.setSheet(sheetName);
    }

    /**
     * 重置Writer，包括：
     *
     * <pre>
     * 1. 当前行游标归零
     * 2. 清空别名比较器
     * 3. 清除标题缓存
     * </pre>
     *
     * @return this
     */
    public ExcelWriter reset() {
        resetRow();
        return this;
    }

    /**
     * 重命名当前sheet
     *
     * @param sheetName 新的sheet名
     * @return this
     */
    public ExcelWriter renameSheet(final String sheetName) {
        return renameSheet(this.workbook.getSheetIndex(this.sheet), sheetName);
    }

    /**
     * 重命名sheet
     *
     * @param sheet     sheet序号，0表示第一个sheet
     * @param sheetName 新的sheet名
     * @return this
     */
    public ExcelWriter renameSheet(final int sheet, final String sheetName) {
        this.workbook.setSheetName(sheet, sheetName);
        return this;
    }

    /**
     * 设置所有列为自动宽度，不考虑合并单元格
     * 此方法必须在指定列数据完全写出后调用才有效。
     * 列数计算是通过第一行计算的
     *
     * @return this
     */
    public ExcelWriter autoSizeColumnAll() {
        final int columnCount = this.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            autoSizeColumn(i);
        }
        return this;
    }

    /**
     * 设置某列为自动宽度，不考虑合并单元格
     * 此方法必须在指定列数据完全写出后调用才有效。
     *
     * @param columnIndex 第几列，从0计数
     * @return this
     */
    public ExcelWriter autoSizeColumn(final int columnIndex) {
        this.sheet.autoSizeColumn(columnIndex);
        return this;
    }

    /**
     * 设置某列为自动宽度
     * 此方法必须在指定列数据完全写出后调用才有效。
     *
     * @param columnIndex    第几列，从0计数
     * @param useMergedCells 是否适用于合并单元格
     * @return this
     */
    public ExcelWriter autoSizeColumn(final int columnIndex, final boolean useMergedCells) {
        this.sheet.autoSizeColumn(columnIndex, useMergedCells);
        return this;
    }

    /**
     * 禁用默认样式
     *
     * @return this
     * @see #setStyleSet(StyleSet)
     */
    public ExcelWriter disableDefaultStyle() {
        return setStyleSet(null);
    }

    /**
     * 获取样式集，样式集可以自定义包括：
     *
     * <pre>
     * 1. 头部样式
     * 2. 一般单元格样式
     * 3. 默认数字样式
     * 4. 默认日期样式
     * </pre>
     *
     * @return 样式集
     */
    public StyleSet getStyleSet() {
        return this.styleSet;
    }

    /**
     * 设置样式集，如果不使用样式，传入{@code null}
     *
     * @param styleSet 样式集，{@code null}表示无样式
     * @return this
     */
    public ExcelWriter setStyleSet(final StyleSet styleSet) {
        this.styleSet = styleSet;
        return this;
    }

    /**
     * 获取头部样式，获取样式后可自定义样式
     *
     * @return 头部样式
     */
    public CellStyle getHeadCellStyle() {
        return this.styleSet.headCellStyle;
    }

    /**
     * 获取单元格样式，获取样式后可自定义样式
     *
     * @return 单元格样式
     */
    public CellStyle getCellStyle() {
        if (null == this.styleSet) {
            return null;
        }
        return this.styleSet.cellStyle;
    }

    /**
     * 获得当前行
     *
     * @return 当前行
     */
    public int getCurrentRow() {
        return this.currentRow.get();
    }

    /**
     * 设置当前所在行
     *
     * @param rowIndex 行号
     * @return this
     */
    public ExcelWriter setCurrentRow(final int rowIndex) {
        this.currentRow.set(rowIndex);
        return this;
    }

    /**
     * 定位到最后一行的后边，用于追加数据
     *
     * @return this
     */
    public ExcelWriter setCurrentRowToEnd() {
        return setCurrentRow(getRowCount());
    }

    /**
     * 跳过当前行
     *
     * @return this
     */
    public ExcelWriter passCurrentRow() {
        this.currentRow.incrementAndGet();
        return this;
    }

    /**
     * 跳过指定行数
     *
     * @param rows 跳过的行数
     * @return this
     */
    public ExcelWriter passRows(final int rows) {
        this.currentRow.addAndGet(rows);
        return this;
    }

    /**
     * 重置当前行为0
     *
     * @return this
     */
    public ExcelWriter resetRow() {
        this.currentRow.set(0);
        return this;
    }

    /**
     * 设置写出的目标文件
     *
     * @param destFile 目标文件
     * @return this
     */
    public ExcelWriter setDestFile(final File destFile) {
        this.destFile = destFile;
        return this;
    }

    @Override
    public ExcelWriter setHeaderAlias(final Map<String, String> headerAlias) {
        // 新增别名时清除比较器缓存
        this.aliasComparator = null;
        return super.setHeaderAlias(headerAlias);
    }

    @Override
    public ExcelWriter clearHeaderAlias() {
        // 清空别名时清除比较器缓存
        this.aliasComparator = null;
        return super.clearHeaderAlias();
    }

    @Override
    public ExcelWriter addHeaderAlias(final String name, final String alias) {
        // 新增别名时清除比较器缓存
        this.aliasComparator = null;
        return super.addHeaderAlias(name, alias);
    }

    /**
     * 设置是否只保留别名中的字段值，如果为true，则不设置alias的字段将不被输出，false表示原样输出
     * Bean中设置@Alias时，setOnlyAlias是无效的，这个参数只和addHeaderAlias配合使用，原因是注解是Bean内部的操作，而addHeaderAlias是Writer的操作，不互通。
     *
     * @param isOnlyAlias 是否只保留别名中的字段值
     * @return this
     */
    public ExcelWriter setOnlyAlias(final boolean isOnlyAlias) {
        this.onlyAlias = isOnlyAlias;
        return this;
    }

    /**
     * 设置窗口冻结，之前冻结的窗口会被覆盖，如果rowSplit为0表示取消冻结
     *
     * @param rowSplit 冻结的行及行数，2表示前两行
     * @return this
     */
    public ExcelWriter setFreezePane(final int rowSplit) {
        return setFreezePane(0, rowSplit);
    }

    /**
     * 设置窗口冻结，之前冻结的窗口会被覆盖，如果colSplit和rowSplit为0表示取消冻结
     *
     * @param colSplit 冻结的列及列数，2表示前两列
     * @param rowSplit 冻结的行及行数，2表示前两行
     * @return this
     */
    public ExcelWriter setFreezePane(final int colSplit, final int rowSplit) {
        getSheet().createFreezePane(colSplit, rowSplit);
        return this;
    }

    /**
     * 设置列宽（单位为一个字符的宽度，例如传入width为10，表示10个字符的宽度）
     *
     * @param columnIndex 列号（从0开始计数，-1表示所有列的默认宽度）
     * @param width       宽度（单位1~255个字符宽度）
     * @return this
     */
    public ExcelWriter setColumnWidth(final int columnIndex, final int width) {
        if (columnIndex < 0) {
            this.sheet.setDefaultColumnWidth(width);
        } else {
            this.sheet.setColumnWidth(columnIndex, width * 256);
        }
        return this;
    }

    /**
     * 设置默认行高，值为一个点的高度
     *
     * @param height 高度
     * @return this
     */
    public ExcelWriter setDefaultRowHeight(final int height) {
        return setRowHeight(-1, height);
    }

    /**
     * 设置行高，值为一个点的高度
     *
     * @param rownum 行号（从0开始计数，-1表示所有行的默认高度）
     * @param height 高度
     * @return this
     */
    public ExcelWriter setRowHeight(final int rownum, final int height) {
        if (rownum < 0) {
            this.sheet.setDefaultRowHeightInPoints(height);
        } else {
            final Row row = this.sheet.getRow(rownum);
            if (null != row) {
                row.setHeightInPoints(height);
            }
        }
        return this;
    }

    /**
     * 设置Excel页眉或页脚
     *
     * @param text     页脚的文本
     * @param align    对齐方式枚举 {@link EnumMap.Align}
     * @param isFooter 是否为页脚，false表示页眉，true表示页脚
     * @return this
     */
    public ExcelWriter setHeaderOrFooter(final String text, final EnumMap.Align align, final boolean isFooter) {
        final HeaderFooter headerFooter = isFooter ? this.sheet.getFooter() : this.sheet.getHeader();
        switch (align) {
            case LEFT:
                headerFooter.setLeft(text);
                break;
            case RIGHT:
                headerFooter.setRight(text);
                break;
            case CENTER:
                headerFooter.setCenter(text);
                break;
            default:
                break;
        }
        return this;
    }

    /**
     * 设置忽略错误，即Excel中的绿色警告小标，只支持XSSFSheet
     * 见：https://stackoverflow.com/questions/23488221/how-to-remove-warning-in-excel-using-apache-poi-in-java
     *
     * @param cellRangeAddress  指定单元格范围
     * @param ignoredErrorTypes 忽略的错误类型列表
     * @return this
     * @throws UnsupportedOperationException 如果sheet不是XSSFSheet
     */
    public ExcelWriter addIgnoredErrors(final CellRangeAddress cellRangeAddress, final IgnoredErrorType... ignoredErrorTypes) throws UnsupportedOperationException {
        final Sheet sheet = this.sheet;
        if (sheet instanceof XSSFSheet) {
            ((XSSFSheet) sheet).addIgnoredErrors(cellRangeAddress, ignoredErrorTypes);
            return this;
        }

        throw new UnsupportedOperationException("Only XSSFSheet supports addIgnoredErrors");
    }

    /**
     * 增加下拉列表
     *
     * @param x          x坐标，列号，从0开始
     * @param y          y坐标，行号，从0开始
     * @param selectList 下拉列表
     * @return this
     */
    public ExcelWriter addSelect(final int x, final int y, final String... selectList) {
        return addSelect(new CellRangeAddressList(y, y, x, x), selectList);
    }

    /**
     * 增加下拉列表
     *
     * @param regions    {@link CellRangeAddressList} 指定下拉列表所占的单元格范围
     * @param selectList 下拉列表内容
     * @return this
     */
    public ExcelWriter addSelect(final CellRangeAddressList regions, final String... selectList) {
        final DataValidationHelper validationHelper = this.sheet.getDataValidationHelper();
        final DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(selectList);

        //设置下拉框数据
        final DataValidation dataValidation = validationHelper.createValidation(constraint, regions);

        //处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        return addValidationData(dataValidation);
    }

    /**
     * 增加单元格控制，比如下拉列表、日期验证、数字范围验证等
     *
     * @param dataValidation {@link DataValidation}
     * @return this
     */
    public ExcelWriter addValidationData(final DataValidation dataValidation) {
        this.sheet.addValidationData(dataValidation);
        return this;
    }

    /**
     * 合并当前行的单元格
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn 合并到的最后一个列号
     * @return this
     */
    public ExcelWriter merge(final int lastColumn) {
        return merge(lastColumn, null);
    }

    /**
     * 合并当前行的单元格，并写入对象到单元格
     * 如果写到单元格中的内容非null，行号自动+1，否则当前行号不变
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn 合并到的最后一个列号
     * @param content    合并单元格后的内容
     * @return this
     */
    public ExcelWriter merge(final int lastColumn, final Object content) {
        return merge(lastColumn, content, true);
    }

    /**
     * 合并某行的单元格，并写入对象到单元格
     * 如果写到单元格中的内容非null，行号自动+1，否则当前行号不变
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn       合并到的最后一个列号
     * @param content          合并单元格后的内容
     * @param isSetHeaderStyle 是否为合并后的单元格设置默认标题样式，只提取边框样式
     * @return this
     */
    public ExcelWriter merge(final int lastColumn, final Object content, final boolean isSetHeaderStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        final int rowIndex = this.currentRow.get();
        merge(rowIndex, rowIndex, 0, lastColumn, content, isSetHeaderStyle);

        // 设置内容后跳到下一行
        if (null != content) {
            this.currentRow.incrementAndGet();
        }
        return this;
    }

    /**
     * 合并某行的单元格，并写入对象到单元格
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param firstRow         起始行，0开始
     * @param lastRow          结束行，0开始
     * @param firstColumn      起始列，0开始
     * @param lastColumn       结束列，0开始
     * @param content          合并单元格后的内容
     * @param isSetHeaderStyle 是否为合并后的单元格设置默认标题样式，只提取边框样式
     * @return this
     */
    public ExcelWriter merge(final int firstRow, final int lastRow, final int firstColumn, final int lastColumn, final Object content, final boolean isSetHeaderStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        CellStyle style = null;
        if (null != this.styleSet) {
            style = styleSet.getStyleByValueType(content, isSetHeaderStyle);
        }

        return merge(firstRow, lastRow, firstColumn, lastColumn, content, style);
    }

    /**
     * 合并单元格，并写入对象到单元格,使用指定的样式
     * 指定样式传入null，则不使用任何样式
     *
     * @param firstRow    起始行，0开始
     * @param lastRow     结束行，0开始
     * @param firstColumn 起始列，0开始
     * @param lastColumn  结束列，0开始
     * @param content     合并单元格后的内容
     * @param cellStyle   合并后单元格使用的样式，可以为null
     * @return this
     */
    public ExcelWriter merge(final int firstRow, final int lastRow, final int firstColumn, final int lastColumn, final Object content, final CellStyle cellStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        CellKit.mergingCells(this.getSheet(), firstRow, lastRow, firstColumn, lastColumn, cellStyle);

        // 设置内容
        if (null != content) {
            final Cell cell = getOrCreateCell(firstColumn, firstRow);
            CellKit.setCellValue(cell, content, cellStyle, this.cellEditor);
        }
        return this;
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动增加
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     * 默认的，当当前行号为0时，写出标题（如果为Map或Bean），否则不写标题
     *
     * <p>
     * data中元素支持的类型有：
     *
     * <pre>
     * 1. Iterable，即元素为一个集合，元素被当作一行，data表示多行
     * 2. Map，即元素为一个Map，第一个Map的keys作为首行，剩下的行为Map的values，data表示多行
     * 3. Bean，即元素为一个Bean，第一个Bean的字段名列表会作为首行，剩下的行为Bean的字段值列表，data表示多行
     * 4. 其它类型，按照基本类型输出（例如字符串）
     * </pre>
     *
     * @param data 数据
     * @return this
     */
    public ExcelWriter write(final Iterable<?> data) {
        return write(data, 0 == getCurrentRow());
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动增加
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * <p>
     * data中元素支持的类型有：
     *
     * <pre>
     * 1. Iterable，即元素为一个集合，元素被当作一行，data表示多行
     * 2. Map，即元素为一个Map，第一个Map的keys作为首行，剩下的行为Map的values，data表示多行
     * 3. Bean，即元素为一个Bean，第一个Bean的字段名列表会作为首行，剩下的行为Bean的字段值列表，data表示多行
     * 4. 其它类型，按照基本类型输出（例如字符串）
     * </pre>
     *
     * @param data             数据
     * @param isWriteKeyAsHead 是否强制写出标题行（Map或Bean）
     * @return this
     */
    public ExcelWriter write(final Iterable<?> data, final boolean isWriteKeyAsHead) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        boolean isFirst = true;
        for (final Object object : data) {
            writeRow(object, isFirst && isWriteKeyAsHead);
            if (isFirst) {
                isFirst = false;
            }
        }
        return this;
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动增加
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     * data中元素支持的类型有：
     *
     * <p>
     * 1. Map，即元素为一个Map，第一个Map的keys作为首行，剩下的行为Map的values，data表示多行
     * 2. Bean，即元素为一个Bean，第一个Bean的字段名列表会作为首行，剩下的行为Bean的字段值列表，data表示多行
     * </p>
     *
     * @param data       数据
     * @param comparator 比较器，用于字段名的排序
     * @return this
     */
    public ExcelWriter write(final Iterable<?> data, final Comparator<String> comparator) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        boolean isFirstRow = true;
        Map<?, ?> map;
        for (final Object obj : data) {
            if (obj instanceof Map) {
                map = new TreeMap<>(comparator);
                map.putAll((Map) obj);
            } else {
                map = BeanKit.beanToMap(obj, new TreeMap<>(comparator), false, false);
            }
            writeRow(map, isFirstRow);
            if (isFirstRow) {
                isFirstRow = false;
            }
        }
        return this;
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中 / 默认图片类型png / 默认的起始坐标和结束坐标都为0
     *
     * @param imgFile 图片文件
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     * vhukze
     */
    public ExcelWriter writeImg(final File imgFile, final int col1, final int row1, final int col2, final int row2) {
        return this.writeImg(imgFile, 0, 0, 0, 0, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中 / 默认图片类型png
     *
     * @param imgFile 图片文件
     * @param dx1     起始单元格中的x坐标
     * @param dy1     起始单元格中的y坐标
     * @param dx2     结束单元格中的x坐标
     * @param dy2     结束单元格中的y坐标
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     * vhukze
     */
    public ExcelWriter writeImg(final File imgFile, final int dx1, final int dy1, final int dx2, final int dy2, final int col1, final int row1,
                                final int col2, final int row2) {
        return this.writeImg(imgFile, Workbook.PICTURE_TYPE_PNG, dx1, dy1, dx2, dy2, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中
     *
     * @param imgFile 图片文件
     * @param imgType 图片类型，对应poi中Workbook类中的图片类型2-7变量
     * @param dx1     起始单元格中的x坐标
     * @param dy1     起始单元格中的y坐标
     * @param dx2     结束单元格中的x坐标
     * @param dy2     结束单元格中的y坐标
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     * vhukze
     */
    public ExcelWriter writeImg(final File imgFile, final int imgType, final int dx1, final int dy1, final int dx2,
                                final int dy2, final int col1, final int row1, final int col2, final int row2) {
        return writeImg(FileKit.readBytes(imgFile), imgType, dx1,
                dy1, dx2, dy2, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中
     *
     * @param pictureData 数据bytes
     * @param imgType     图片类型，对应poi中Workbook类中的图片类型2-7变量
     * @param dx1         起始单元格中的x坐标
     * @param dy1         起始单元格中的y坐标
     * @param dx2         结束单元格中的x坐标
     * @param dy2         结束单元格中的y坐标
     * @param col1        指定起始的列，下标从0开始
     * @param row1        指定起始的行，下标从0开始
     * @param col2        指定结束的列，下标从0开始
     * @param row2        指定结束的行，下标从0开始
     * @return this
     * vhukze
     */
    public ExcelWriter writeImg(final byte[] pictureData, final int imgType, final int dx1, final int dy1, final int dx2,
                                final int dy2, final int col1, final int row1, final int col2, final int row2) {
        final Drawing<?> patriarch = this.sheet.createDrawingPatriarch();
        final ClientAnchor anchor = this.workbook.getCreationHelper().createClientAnchor();
        anchor.setDx1(dx1);
        anchor.setDy1(dy1);
        anchor.setDx2(dx2);
        anchor.setDy2(dy2);
        anchor.setCol1(col1);
        anchor.setRow1(row1);
        anchor.setCol2(col2);
        anchor.setRow2(row2);

        patriarch.createPicture(anchor, this.workbook.addPicture(pictureData, imgType));
        return this;
    }

    /**
     * 写出一行标题数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeHeadRow(final Iterable<?> rowData) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        this.headLocationCache = new SafeConcurrentHashMap<>();
        final Row row = this.sheet.createRow(this.currentRow.getAndIncrement());
        int i = 0;
        Cell cell;
        for (final Object value : rowData) {
            cell = row.createCell(i);
            CellKit.setCellValue(cell, value, this.styleSet, true, this.cellEditor);
            this.headLocationCache.put(StringKit.toString(value), i);
            i++;
        }
        return this;
    }

    /**
     * 写出复杂标题的第二行标题数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * <p>
     * 此方法的逻辑是：将一行数据写出到当前行，遇到已存在的单元格跳过，不存在的创建并赋值。
     * </p>
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeSecHeadRow(final Iterable<?> rowData) {
        final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
        final Iterator<?> iterator = rowData.iterator();
        //如果获取的row存在单元格，则执行复杂表头逻辑，否则直接调用writeHeadRow(Iterable<?> rowData)
        if (row.getLastCellNum() != 0) {
            for (int i = 0; i < this.workbook.getSpreadsheetVersion().getMaxColumns(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    continue;
                }
                if (iterator.hasNext()) {
                    cell = row.createCell(i);
                    CellKit.setCellValue(cell, iterator.next(), this.styleSet, true, this.cellEditor);
                } else {
                    break;
                }
            }
        } else {
            writeHeadRow(rowData);
        }
        return this;
    }

    /**
     * 写出一行，根据rowBean数据类型不同，写出情况如下：
     *
     * <pre>
     * 1、如果为Iterable，直接写出一行
     * 2、如果为Map，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * 3、如果为Bean，转为Map写出，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * </pre>
     *
     * @param rowBean          写出的Bean
     * @param isWriteKeyAsHead 为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * @return this
     * @see #writeRow(Iterable)
     * @see #writeRow(Map, boolean)
     */
    public ExcelWriter writeRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        final Map rowMap;
        if (rowBean instanceof Map) {
            if (MapKit.isNotEmpty(this.headerAlias)) {
                rowMap = MapKit.newTreeMap((Map) rowBean, getCachedAliasComparator());
            } else {
                rowMap = (Map) rowBean;
            }
        } else if (rowBean instanceof Iterable) {
            // MapWrapper由于实现了Iterable接口，应该优先按照Map处理
            return writeRow((Iterable<?>) rowBean);
        } else if (rowBean instanceof Hyperlink) {
            // Hyperlink当成一个值
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        } else if (BeanKit.isWritableBean(rowBean.getClass())) {
            if (MapKit.isEmpty(this.headerAlias)) {
                rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
            } else {
                // 别名存在情况下按照别名的添加顺序排序Bean数据
                rowMap = BeanKit.beanToMap(rowBean, new TreeMap<>(getCachedAliasComparator()), false, false);
            }
        } else {
            // 其它转为字符串默认输出
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        }
        return writeRow(rowMap, isWriteKeyAsHead);
    }

    /**
     * 将一个Map写入到Excel，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * 如果rowMap为空（包括null），则写出空行
     *
     * @param rowMap           写出的Map，为空（包括null），则写出空行
     * @param isWriteKeyAsHead 为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * @return this
     */
    public ExcelWriter writeRow(final Map<?, ?> rowMap, final boolean isWriteKeyAsHead) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        if (MapKit.isEmpty(rowMap)) {
            // 如果写出数据为null或空，跳过当前行
            return passCurrentRow();
        }

        final Table<?, ?, ?> aliasTable = aliasTable(rowMap);
        if (isWriteKeyAsHead) {
            // 写出标题行，并记录标题别名和列号的关系
            writeHeadRow(aliasTable.columnKeys());
            // 记录原数据key对应列号
            int i = 0;
            for (final Object key : aliasTable.rowKeySet()) {
                this.headLocationCache.putIfAbsent(StringKit.toString(key), i);
                i++;
            }
        }

        // 如果已经写出标题行，根据标题行找对应的值写入
        if (MapKit.isNotEmpty(this.headLocationCache)) {
            final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
            Integer location;
            for (final Table.Cell<?, ?, ?> cell : aliasTable) {
                // 首先查找原名对应的列号
                location = this.headLocationCache.get(StringKit.toString(cell.getRowKey()));
                if (null == location) {
                    // 未找到，则查找别名对应的列号
                    location = this.headLocationCache.get(StringKit.toString(cell.getColumnKey()));
                }
                if (null != location) {
                    CellKit.setCellValue(CellKit.getOrCreateCell(row, location), cell.getValue(), this.styleSet, false, this.cellEditor);
                }
            }
        } else {
            writeRow(aliasTable.values());
        }
        return this;
    }

    /**
     * 写出一行数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeRow(final Iterable<?> rowData) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        RowKit.writeRow(this.sheet.createRow(this.currentRow.getAndIncrement()), rowData, this.styleSet, false, this.cellEditor);
        return this;
    }

    /**
     * 从第1列开始按列写入数据(index 从0开始)
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param colMap           一列的数据
     * @param isWriteKeyAsHead 是否将Map的Key作为表头输出，如果为True第一行为表头，紧接着为values
     * @return this
     */
    public ExcelWriter writeCol(final Map<?, ? extends Iterable<?>> colMap, final boolean isWriteKeyAsHead) {
        return writeCol(colMap, 0, isWriteKeyAsHead);
    }

    /**
     * 从指定列开始按列写入数据(index 从0开始)
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param colMap           一列的数据
     * @param startColIndex    起始的列号，从0开始
     * @param isWriteKeyAsHead 是否将Map的Key作为表头输出，如果为True第一行为表头，紧接着为values
     * @return this
     */
    public ExcelWriter writeCol(final Map<?, ? extends Iterable<?>> colMap, int startColIndex, final boolean isWriteKeyAsHead) {
        for (final Object k : colMap.keySet()) {
            final Iterable<?> v = colMap.get(k);
            if (v != null) {
                writeCol(isWriteKeyAsHead ? k : null, startColIndex, v, startColIndex != colMap.size() - 1);
                startColIndex++;
            }
        }
        return this;
    }


    /**
     * 为第一列写入数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param headerVal       表头名称,如果为null则不写入
     * @param colData         需要写入的列数据
     * @param isResetRowIndex 如果为true，写入完毕后Row index 将会重置为写入之前的未知，如果为false，写入完毕后Row index将会在写完的数据下方
     * @return this
     */
    public ExcelWriter writeCol(final Object headerVal, final Iterable<?> colData, final boolean isResetRowIndex) {
        return writeCol(headerVal, 0, colData, isResetRowIndex);
    }

    /**
     * 为第指定列写入数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认样式，可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param headerVal       表头名称,如果为null则不写入
     * @param colIndex        列index
     * @param colData         需要写入的列数据
     * @param isResetRowIndex 如果为true，写入完毕后Row index 将会重置为写入之前的未知，如果为false，写入完毕后Row index将会在写完的数据下方
     * @return this
     */
    public ExcelWriter writeCol(final Object headerVal, final int colIndex, final Iterable<?> colData, final boolean isResetRowIndex) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        int currentRowIndex = currentRow.get();
        if (null != headerVal) {
            writeCellValue(colIndex, currentRowIndex, headerVal, true);
            currentRowIndex++;
        }
        for (final Object colDatum : colData) {
            writeCellValue(colIndex, currentRowIndex, colDatum);
            currentRowIndex++;
        }
        if (!isResetRowIndex) {
            currentRow.set(currentRowIndex);
        }
        return this;
    }

    /**
     * 给指定单元格赋值，使用默认单元格样式
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @param value       值
     * @return this
     */
    public ExcelWriter writeCellValue(final String locationRef, final Object value) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return writeCellValue(cellLocation.getX(), cellLocation.getY(), value);
    }

    /**
     * 给指定单元格赋值，使用默认单元格样式
     *
     * @param x     X坐标，从0计数，即列号
     * @param y     Y坐标，从0计数，即行号
     * @param value 值
     * @return this
     */
    public ExcelWriter writeCellValue(final int x, final int y, final Object value) {
        return writeCellValue(x, y, value, false);
    }

    /**
     * 给指定单元格赋值，使用默认单元格样式
     *
     * @param x        X坐标，从0计数，即列号
     * @param y        Y坐标，从0计数，即行号
     * @param isHeader 是否为Header
     * @param value    值
     * @return this
     */
    public ExcelWriter writeCellValue(final int x, final int y, final Object value, final boolean isHeader) {
        final Cell cell = getOrCreateCell(x, y);
        CellKit.setCellValue(cell, value, this.styleSet, isHeader, this.cellEditor);
        return this;
    }

    /**
     * 设置某个单元格的样式
     * 此方法用于多个单元格共享样式的情况
     * 可以调用{@link #getOrCreateCellStyle(int, int)} 方法创建或取得一个样式对象。
     *
     * <p>
     * 需要注意的是，共享样式会共享同一个{@link CellStyle}，一个单元格样式改变，全部改变。
     *
     * @param style       单元格样式
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return this
     */
    public ExcelWriter setStyle(final CellStyle style, final String locationRef) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return setStyle(style, cellLocation.getX(), cellLocation.getY());
    }

    /**
     * 设置某个单元格的样式
     * 此方法用于多个单元格共享样式的情况
     * 可以调用{@link #getOrCreateCellStyle(int, int)} 方法创建或取得一个样式对象。
     *
     * <p>
     * 需要注意的是，共享样式会共享同一个{@link CellStyle}，一个单元格样式改变，全部改变。
     *
     * @param style 单元格样式
     * @param x     X坐标，从0计数，即列号
     * @param y     Y坐标，从0计数，即行号
     * @return this
     */
    public ExcelWriter setStyle(final CellStyle style, final int x, final int y) {
        final Cell cell = getOrCreateCell(x, y);
        cell.setCellStyle(style);
        return this;
    }

    /**
     * 设置行样式
     *
     * @param y     Y坐标，从0计数，即行号
     * @param style 样式
     * @return this
     * @see Row#setRowStyle(CellStyle)
     */
    public ExcelWriter setRowStyle(final int y, final CellStyle style) {
        getOrCreateRow(y).setRowStyle(style);
        return this;
    }

    /**
     * 对数据行整行加自定义样式 仅对数据单元格设置 write后调用
     * <p>
     * {@link ExcelWriter#setRowStyle(int, org.apache.poi.ss.usermodel.CellStyle)}
     * 这个方法加的样式会使整行没有数据的单元格也有样式
     * 特别是加背景色时很不美观 且有数据的单元格样式会被StyleSet中的样式覆盖掉
     *
     * @param y     行坐标
     * @param style 自定义的样式
     * @return this
     */
    public ExcelWriter setRowStyleIfHasData(final int y, final CellStyle style) {
        if (y < 0) {
            throw new IllegalArgumentException("Invalid row number (" + y + Symbol.PARENTHESE_RIGHT);
        }
        final int columnCount = this.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            this.setStyle(style, i, y);
        }
        return this;
    }

    /**
     * 设置列的默认样式
     *
     * @param x     列号，从0开始
     * @param style 样式
     * @return this
     */
    public ExcelWriter setColumnStyle(final int x, final CellStyle style) {
        this.sheet.setDefaultColumnStyle(x, style);
        return this;
    }

    /**
     * 设置整个列的样式 仅对数据单元格设置 write后调用
     * <p>
     * {@link ExcelWriter#setColumnStyle(int, org.apache.poi.ss.usermodel.CellStyle)}
     * 这个方法加的样式会使整列没有数据的单元格也有样式
     * 特别是加背景色时很不美观 且有数据的单元格样式会被StyleSet中的样式覆盖掉
     *
     * @param x     列的索引
     * @param y     起始行
     * @param style 样式
     * @return this
     */
    public ExcelWriter setColumnStyleIfHasData(final int x, final int y, final CellStyle style) {
        if (x < 0) {
            throw new IllegalArgumentException("Invalid column number (" + x + Symbol.PARENTHESE_RIGHT);
        }
        if (y < 0) {
            throw new IllegalArgumentException("Invalid row number (" + y + Symbol.PARENTHESE_RIGHT);
        }
        final int rowCount = this.getRowCount();
        for (int i = y; i < rowCount; i++) {
            this.setStyle(style, x, i);
        }
        return this;
    }

    /**
     * 将Excel Workbook刷出到预定义的文件
     * 如果用户未自定义输出的文件，将抛出{@link NullPointerException}
     * 预定义文件可以通过{@link #setDestFile(File)} 方法预定义，或者通过构造定义
     *
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush() throws InternalException {
        return flush(this.destFile);
    }

    /**
     * 将Excel Workbook刷出到文件
     * 如果用户未自定义输出的文件，将抛出{@link NullPointerException}
     *
     * @param destFile 写出到的文件
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(final File destFile) throws InternalException {
        Assert.notNull(destFile, "[destFile] is null, and you must call setDestFile(File) first or call flush(OutputStream).");
        return flush(FileKit.getOutputStream(destFile), true);
    }

    /**
     * 将Excel Workbook刷出到输出流
     *
     * @param out 输出流
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(final OutputStream out) throws InternalException {
        return flush(out, false);
    }

    /**
     * 将Excel Workbook刷出到输出流
     *
     * @param out        输出流
     * @param isCloseOut 是否关闭输出流
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        try {
            this.workbook.write(out);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.closeQuietly(out);
            }
        }
        return this;
    }

    /**
     * 关闭工作簿
     * 如果用户设定了目标文件，先写出目标文件后给关闭工作簿
     */
    @Override
    public void close() {
        if (null != this.destFile) {
            flush();
        }
        closeWithoutFlush();
    }

    /**
     * 关闭工作簿但是不写出
     */
    protected void closeWithoutFlush() {
        super.close();
        this.currentRow.set(0);

        // 清空对象
        this.styleSet = null;
    }

    /**
     * 为指定的key列表添加标题别名，如果没有定义key的别名，在onlyAlias为false时使用原key
     * key为别名，value为字段值
     *
     * @param rowMap 一行数据
     * @return 别名列表
     */
    private Table<?, ?, ?> aliasTable(final Map<?, ?> rowMap) {
        final Table<Object, Object, Object> filteredTable = new RowKeyTable<>(new LinkedHashMap<>(), TableMap::new);
        if (MapKit.isEmpty(this.headerAlias)) {
            rowMap.forEach((key, value) -> filteredTable.put(key, key, value));
        } else {
            rowMap.forEach((key, value) -> {
                final String aliasName = this.headerAlias.get(StringKit.toString(key));
                if (null != aliasName) {
                    // 别名键值对加入
                    filteredTable.put(key, aliasName, value);
                } else if (!this.onlyAlias) {
                    // 保留无别名设置的键值对
                    filteredTable.put(key, key, value);
                }
            });
        }

        return filteredTable;
    }

    /**
     * 获取单例的别名比较器，比较器的顺序为别名加入的顺序
     *
     * @return Comparator
     */
    private Comparator<String> getCachedAliasComparator() {
        if (MapKit.isEmpty(this.headerAlias)) {
            return null;
        }
        Comparator<String> aliasComparator = this.aliasComparator;
        if (null == aliasComparator) {
            final Set<String> keySet = this.headerAlias.keySet();
            aliasComparator = new IndexedCompare<>(keySet.toArray(new String[0]));
            this.aliasComparator = aliasComparator;
        }
        return aliasComparator;
    }

}
