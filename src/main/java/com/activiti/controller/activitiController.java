package com.activiti.controller;


import com.activiti.util.ActivitiUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;

/**
 *
 * activiti 简单测试
 */
@RestController
public class activitiController {
    /**
     * 存储服务
     * 加载流程
     * rs.createDeployment().addClasspathResource("processes/first.bpmn").deploy();
     * activiti 启动时自动加载 processes
     * 暂且不用
     */
    @Autowired
    RepositoryService repositoryService;

    /**
     * 运行服务
     * 拿到流程实例
     * ProcessInstance pi = runService.startProcessInstanceByKey("myProcess_1")
     * myprocess_1 为流程id
     */
    @Autowired
    RuntimeService runtimeService;

    /**
     * 任务服务
     * taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult()
     * 拿到流程为单例模式
     * taskService.complete(task.getId()); //审核
     */
    @Autowired
    TaskService taskService;
    /**
     * 用户组 身份服务
     */
    @Autowired
   IdentityService identityService;

    /**
     * 查看流程图片
     * @return
     */
    @RequestMapping("/creatimg")
    public String creatImg()throws Exception{
        //部署流程相应的流程文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/first.bpmn").deploy();
        //查询流程定义
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        //查看图像
        InputStream in = repositoryService.getProcessDiagram(def.getId());
        while (in.read() != 0){
            System.out.println(in.read());
        }
        FileOutputStream out = null;
        try {
            BufferedImage image = ImageIO.read(in);
            File file = new File("img");
            if (file == null){
                file.mkdirs();
            }
            File f = new File(file,"first.png");
            if (!f.exists()){
                f.createNewFile();
            }
            out = new FileOutputStream(file);
            ImageIO.write(image,"png",out);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "success";
    }
    /**
     * 动态添加流程
     *
     * @return
     */
    @RequestMapping("/builder")
    public String budlerModel(){

       BpmnModel model =  ActivitiUtil.creatbpmnModel();
       //获得存储服务
        DeploymentBuilder builder =  repositoryService.createDeployment();
        builder.addBpmnModel("my process",model);
        //
        builder.deploy();
        return "success";
    }
    /**
     * 添加存储服务
     * @return
     */
    @RequestMapping("/addZip")
    public String addZip(){
        //创建存储服务
        DeploymentBuilder builder = repositoryService.createDeployment();
        try {

            FileInputStream fileInputStream = new FileInputStream(new File("newDemo.rar"));
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            //加入流
            builder.addZipInputStream(zip);
            //存储
            builder.deploy();
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

       return "success";
    }
    /**
     * 用户添加
     * @return
     */
    @RequestMapping("/group")
    public String identify(){

        for (int i = 0; i <= 9; i++){
            Group group = identityService.newGroup(String.valueOf(i));
            group.setName("Group_" + i);
            group.setType("TPYE_" + i);
            identityService.saveGroup(group);
        }

        return "success";
    }

    /**
     * 查询用户
     * @return
     */
    @RequestMapping("/select")
    public String select(){
        //查询所有 按照组件升序返回结果
        List<Group> list = identityService.createGroupQuery().list();
        list.forEach(group -> System.out.println("id:  " + group.getId() + "   name:  " + group.getName() + "   type: " + group.getType()));
        return "success";
    }

    /**
     * 分页查询
     * @return
     */
    @RequestMapping("/page")
    public Map listPage(){
        //分页查询 每个字段都有相应的 分组方法 desc 降序 都要先调用createGroupQuery()
        List<Group> listPage = identityService.createGroupQuery().orderByGroupName().desc().listPage(0,5);
        listPage.forEach(group -> System.out.println("id:  " + group.getId() + "   name:  " + group.getName() + "   type: " + group.getType()));
        //原生查询
        List<Group> list = identityService.createNativeGroupQuery()
                .sql("select * from ACT_ID_GROUP where NAME_ = #{name}")
                .parameter("name","Group_5")
                .list();
        System.out.println("---------原生查询----------");
        list.forEach(group -> System.out.println("id:  " + group.getId() + "   name:  " + group.getName() + "   type: " + group.getType()));
        //计数
        Long count = identityService.createGroupQuery().count();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("count",count);
        map.put("groups",listPage);
        return map;
    }

    /**
     * 流程测试
     * @return
     */
    @RequestMapping("/taskTest")
    public String taskTest(){
        //真实项目下不允许
        Task task = taskService.newTask(UUID.randomUUID().toString().replaceAll("-",""));
        task.setName("测试任务");
        taskService.saveTask(task);

//        Group group = identityService.newGroup(UUID.randomUUID().toString().replaceAll("-",""));
//        group.setName("测试用户组");
//        identityService.saveGroup(group);
        User u = identityService.newUser(UUID.randomUUID().toString().replaceAll("-",""));
            u.setFirstName("测试用户");
        identityService.saveUser(u);
        //添加执行任务用户组
        taskService.addCandidateUser(task.getId(),u.getId());
        //查询任务
        List<Task> taskList = taskService.createTaskQuery().taskCandidateUser(u.getId()).list();
        taskList.forEach(task1 -> System.out.println("name:  " + task.getName() + " id:  " + task.getId()));
        return "success";
        //设置持有人
//        taskService.setOwner(task.getId(),u.getId());
        //持有查询
//        taskService.createTaskQuery().taskOwner(u.getId()).list();
        //设置代理人
//        taskService.claim(task.getId(),u.getId());
//        taskService.createTaskQuery().taskAssignee(u.getId());
    }
    /**
     * 设置用户
     * @return
     */
    @RequestMapping("/setting")
    public List<ProcessDefinition> SettingUser(){
        //设置用户
        User user = identityService.newUser(UUID.randomUUID().toString().replaceAll("-",""));
        user.setFirstName("小红");
        identityService.saveUser(user);
        //部署流程
        DeploymentBuilder builder = repositoryService.createDeployment();
        builder.addClasspathResource("processes/sccend.bpmn");
        Deployment dep = builder.deploy();
        System.out.println(dep.getId() + "-----------" + dep.getName());

        // 拿到流程
        ProcessDefinition p = repositoryService.createProcessDefinitionQuery()
                .deploymentId(dep.getId()).singleResult();
        System.out.println(p.getId() + "-----------" + p.getName());

        //设置小红为流程候选用户
        repositoryService.addCandidateStarterUser(p.getId(),user.getId());

        //根据用户来查询可用流程
        List<ProcessDefinition> desf = repositoryService.createProcessDefinitionQuery()
                .startableByUser(user.getId()).list();
        System.out.println("----查看可启动流程---");
        desf.forEach(processDefinition -> System.out.println(processDefinition.getId() + "  name: " + processDefinition.getName()));
        return desf;
    }
    /**
     * 拿到流程
     *
     * @return
     */
    @RequestMapping("/runTest")
    public String runTest(){
        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("myProcess_1");//xml 文件的 process 的节点中 在数据库的act_run_task中
       // 终止流程 激活时会报错 repositoryService.suspendProcessDefinitionById("");
        // 启动流程 runtimeService.activateProcessInstanceById("");
        //查询流程
        //员工完成请假
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println("当前流程节点： " + task.getName() + "   processInstanceID:  " + pi.getId() );
        return "success";
    }
    @RequestMapping("/test")
    public String test(){
        //
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        engine.getIdentityService();
        //拿到服务
        RepositoryService rs = engine.getRepositoryService();
        //运行时服务
        RuntimeService runService = engine.getRuntimeService();
        //任务服务
        TaskService taskService = engine.getTaskService();


        rs.createDeployment().addClasspathResource("processes/first.bpmn").deploy();

        //流程实例
        ProcessInstance pi = runService.startProcessInstanceByKey("myProcess_1");//xml 文件的 process 的节点中

        //查询流程
        //员工完成请假
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println("当前流程节点： " + task.getName());
        taskService.complete(task.getId()); //审核

        //经理审核任务
        task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println("当前流程节点： " + task.getName());
        taskService.complete(task.getId()); //审核

        //查看流程详情
        task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println("当前流程节结束： " + task);

        //关闭
        engine.close();
        return "hehe";
    }
}
