package com.activiti.util;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 反射类
 * 将历史流程数据放到实体类
 */
public class FiledUtil {

    public static <T> void setVarls(T entity, List<HistoricVariableInstance> processInstances) {
        //拿到class
        Class<?> tclass = entity.getClass();

        for (HistoricVariableInstance variableInstance : processInstances) {
            try {
                Field field = tclass.getDeclaredField(variableInstance.getVariableName());
                if (field == null) {
                    continue;
                }
                //设置可以改变私有的属性
                field.setAccessible(true);
                field.set(entity, variableInstance.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
