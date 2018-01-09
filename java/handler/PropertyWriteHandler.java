package com.qb.workstation.export;

import org.apache.poi.ss.usermodel.Cell;

import java.lang.reflect.InvocationTargetException;

public interface PropertyWriteHandler<T> {

    default public boolean support(String fieldName){
        return true;
    }

    void handle(Cell cell, T bean, String fieldName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

}
