package com.qb.workstation.export;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class DefaultPropertyWriteWriteHandler<T> implements PropertyWriteHandler<T> {

    @Override
    public void handle(Cell cell, T bean, String fieldName, Object value) {
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
