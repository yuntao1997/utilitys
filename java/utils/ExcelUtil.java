package com.qb.workstation.util;

import com.qb.workstation.export.DefaultPropertyReadHandler;
import com.qb.workstation.export.DefaultPropertyWriteWriteHandler;
import com.qb.workstation.export.PropertyReadHandler;
import com.qb.workstation.export.PropertyWriteHandler;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ExcelUtil {

    private final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    public static <T> List<T> readExcelWithTitle(InputStream inputStream, String sheetName, Class<T> cls, List<String> columns, PropertyReadHandler<T> readHandler) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {

        if (cls == null) {
            throw new IllegalArgumentException("Parameter[cls] must not null");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Parameter[inputStream] must not null");
        }
        if (StringUtils.isBlank(sheetName)) {
            throw new IllegalArgumentException("Parameter[sheetName] must not blank");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Parameter[columns] must not empty");
        }
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheet(sheetName);

            return readSheetWithTitle(sheet, cls, columns, readHandler);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }


    public static <T> List<T> readExcelWithTitle(InputStream inputStream, int sheetIndex, Class<T> cls, List<String> columns) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        return readExcelWithTitle(inputStream, sheetIndex, cls, columns);
    }
    public static <T> List<T> readExcelWithTitle(InputStream inputStream, int sheetIndex, Class<T> cls, List<String> columns, PropertyReadHandler<T> readHandler) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {

        if (cls == null) {
            throw new IllegalArgumentException("Parameter[cls] must not null");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Parameter[inputStream] must not null");
        }
        if (sheetIndex < 0) {
            throw new IllegalArgumentException("Parameter[SheetIndex] must greater than or equal to zero");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Parameter[columns] must not empty");
        }
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            return readSheetWithTitle(sheet, cls, columns, readHandler);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private static <T> List<T> readSheetWithTitle(Sheet sheet, Class<T> cls, List<String> columns, PropertyReadHandler<T> readHandler) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {


        int firstRowNum = sheet.getFirstRowNum();
        Row firstRow = sheet.getRow(firstRowNum);
        short firstCellNum = firstRow.getFirstCellNum();


        int lastRowNum = sheet.getLastRowNum();
        T bean = null;
        List<T> dataList = new ArrayList<>(lastRowNum);
        DefaultPropertyReadHandler<T> defaultPropertyReadHandler = new DefaultPropertyReadHandler<>();
        for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            //skip empty row
            if (row == null) {
                continue;
            }

            bean = cls.newInstance();
            for (int j = 0; j < columns.size(); j++) {
                String fieldName = columns.get(j);
                if (StringUtils.isBlank(fieldName)) {
                    continue;
                }
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                Object value = null;

                if (readHandler != null && readHandler.support(fieldName)) {
                    readHandler.handle(cell, bean, fieldName);
                } else {
                    defaultPropertyReadHandler.handle(cell, bean, fieldName);
                }
            }
            dataList.add(bean);
        }
        return dataList;

    }

    public static <T> void exportExcel(String title, LinkedHashMap<String, String> headersMap, Collection<T> dataCollection, OutputStream outputStream) {
        exportExcel(title, headersMap, dataCollection, null, outputStream);
    }

    /**
     * 导出EXCEL
     *
     * @param title              sheet页标题
     * @param headersMap         字段与表头映射。key为字段名，value为表头名
     * @param dataCollection     数据集合
     * @param propertyHandlerMap 字段属性处理器集合
     * @param outputStream       输出流
     */
    public static <T> void exportExcel(String title, LinkedHashMap<String, String> headersMap, Collection<T> dataCollection, Map<String, PropertyWriteHandler<T>> propertyHandlerMap, OutputStream outputStream) {
        title = StringUtils.defaultString(title, "Sheet1");
        if (headersMap == null || headersMap.isEmpty()) {
            logger.warn("未设置表头");
            return;
        }

        if (propertyHandlerMap == null) {
            propertyHandlerMap = new LinkedHashMap<>();
        }
        propertyHandlerMap.values().contains(null);

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet(title);

        HSSFDataFormat format = workbook.createDataFormat();

        HSSFCellStyle titleCellStyle = workbook.createCellStyle();
//        titleCellStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
//        titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCellStyle.setDataFormat(format.getFormat("@"));

        // 生成一个字体
        HSSFFont titleFont = workbook.createFont();
        titleFont.setColor(HSSFColor.VIOLET.index);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setFontName("宋体");
        titleFont.setBold(true);
        titleCellStyle.setFont(titleFont);


        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        style2.setDataFormat(format.getFormat("@"));

        // 生成另一个字体
        HSSFFont contentFont = workbook.createFont();
        contentFont.setFontHeightInPoints((short) 16);
        contentFont.setFontName("宋体");
        contentFont.setBold(false);
        // 把字体应用到当前的样式
        style2.setFont(contentFont);

        String[] properties = new String[headersMap.size()];
        String[] headers = new String[headersMap.size()];

        Iterator<Map.Entry<String, String>> iterator = headersMap.entrySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Map.Entry<String, String> next = iterator.next();
            properties[i] = next.getKey();
            headers[i] = next.getValue();
        }

        HSSFRow titleRow = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = titleRow.createCell(i);
            cell.setCellStyle(titleCellStyle);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }

        PropertyWriteHandler<T> defaultPropertyWriteHandler = new DefaultPropertyWriteWriteHandler<T>();

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataCollection.iterator();
        int index = 0;
        HSSFRow row = null;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);

            T t = (T) it.next();

            for (int i = 0; i < properties.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);


                String fieldName = properties[i];
                try {
                    Object value = PropertyUtils.getProperty(t, fieldName);

                    //如果存在属性处理器，通过属性处理器进行处理值，否则使用默认处理器
                    PropertyWriteHandler propertyWriteHandler = propertyHandlerMap.get(fieldName);
                    if (propertyWriteHandler != null) {

                        propertyWriteHandler.handle(cell, t, fieldName, value);
                    } else {
                        defaultPropertyWriteHandler.handle(cell, t, fieldName, value);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < headers.length; i++) {
            autoSizeColumn(sheet, i);
        }

        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 自动调整列的宽度
     *
     * @param sheet  sheet
     * @param column 列索引
     */
    public static void autoSizeColumn(Sheet sheet, int column) {

        if (sheet == null) {
            return;
        }

        int lastRowNum = sheet.getLastRowNum();

        int row = 0;
        int maxWidth = 1;
        while (row < lastRowNum) {
            Cell cell = sheet.getRow(row).getCell(column);

            int length = 0;
            if (cell.getCellType() == CellType.NUMERIC.getCode()) {
                double numericCellValue = cell.getNumericCellValue();
                length = String.valueOf(numericCellValue).getBytes().length * 2;
            } else {
                length = StringUtils.defaultString(cell.getStringCellValue()).getBytes().length;
            }


            if (length > maxWidth) {
                maxWidth = length;
            }
            row++;
        }
        //当前列中值的最大长度 * 256
        int width = maxWidth + 2;
        sheet.setColumnWidth(column, width * 256);

//        字符像素宽度 = 字体宽度 * 字符个数 + 边距
//        宋体 16号 字体宽度：20px，边距：10px
//        int width = (maxCharSize  + 10) * 256;
//        sheet.setColumnWidth(column, width);
    }

    /**
     * 是否是excel 2007及以上版本
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static boolean isXlsx(InputStream is) throws IOException {
        if (!is.markSupported()) {
            is = new PushbackInputStream(is, 8);
        }
        if (DocumentFactoryHelper.hasOOXMLHeader(is)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是excel 2003及以下版本
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static boolean isXls(InputStream is) throws IOException {
        if (!is.markSupported()) {
            is = new PushbackInputStream(is, 8);
        }
        if (POIFSFileSystem.hasPOIFSHeader(is)) {
            return true;
        }
        return false;
    }
}
