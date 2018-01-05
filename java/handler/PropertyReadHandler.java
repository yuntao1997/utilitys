package com.qb.workstation.export;

import org.apache.poi.ss.usermodel.Cell;

import java.lang.reflect.InvocationTargetException;

public interface PropertyReadHandler<T>  {

    default public boolean support(String fieldName){
        return true;
    }

    public void handle(Cell cell, T bean, String fieldName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException ;
}
