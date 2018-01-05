package com.qb.workstation.export;

import org.apache.poi.ss.usermodel.Cell;

import java.lang.reflect.InvocationTargetException;

public interface PropertyWriteHandler<T> {



    void handle(Cell cell, T bean, String fieldName, Object value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

}
