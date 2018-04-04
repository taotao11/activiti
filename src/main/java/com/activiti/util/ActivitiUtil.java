package com.activiti.util;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;

public class ActivitiUtil {
    //动态创建流程
    public static BpmnModel creatbpmnModel(){
        //创建流程对象
        BpmnModel bpmnModel = new BpmnModel();
        //创建流程定义
        Process process = new Process();
        bpmnModel.addProcess(process);

        process.setId("myprocess");
        process.setName("my process");

        //开始事件
        StartEvent startEvent = new StartEvent();
        startEvent.setId("starEvent");
        process.addFlowElement(startEvent);
        //身份任务
        UserTask userTask = new UserTask();
        userTask.setName("User Task");
        userTask.setId("userTask");
        process.addFlowElement(userTask);

        //身份任务2
        UserTask user = new UserTask();
        userTask.setName("User");
        userTask.setId("user");
        process.addFlowElement(user);

        //结束事件
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        process.addFlowElement(endEvent);

        //执行顺序 连线
        process.addFlowElement(new SequenceFlow("starEvent","userTask"));
        process.addFlowElement(new SequenceFlow("userTask","user"));
        process.addFlowElement(new SequenceFlow("user","endEvent"));
        return bpmnModel;
    }
}
