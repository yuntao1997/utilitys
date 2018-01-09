package com.qb.workstation.export;


import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.lang.reflect.InvocationTargetException;

public class DefaultPropertyWriteWriteHandler<T> implements PropertyWriteHandler<T> {

    @Override
    public void handle(Cell cell, T bean, String fieldName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Object value = PropertyUtils.getProperty(bean, fieldName);
        if (value instanceof Boolean) {
            cell.setCellType(CellType.BOOLEAN);
        }
        if (value instanceof Integer || value instanceof Double || value instanceof Float) {
            cell.setCellType(CellType.NUMERIC);
        }
        if (value instanceof String) {
            cell.setCellType(CellType.STRING);
        }
        String strValue = "";
        if (value != null) {
            strValue = value.toString();
        }

        cell.setCellValue(strValue);
    }
}
