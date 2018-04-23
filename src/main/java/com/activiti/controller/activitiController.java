package com.activiti.controller;


import com.activiti.entity.UserEntity;
import com.activiti.util.ActivitiUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
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
@Api(value = "activiti测试文档")
@RestController
public class activitiController {
    /**
     * 拿到Response
     */
    @Autowired
    HttpServletResponse resp;
    /**
     * 存储服务
     * 加载流程
     * rs.createDeployment().addClasspathResource("processes/first.xml").deploy();
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
     * 历史任务服务
     */
    @Autowired
    HistoryService historyService;

    @ApiOperation(value = "查看流程图片",  produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * 查看流程图片
     * @return
     */
    @RequestMapping("/creatimg")
    public String creatImg()throws Exception{
        //部署流程相应的流程文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/first.xml").deploy();
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
        FileInputStream fileInputStream = null;
        try {

            fileInputStream = new FileInputStream(new File("newDemo.rar"));
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            //加入流
            builder.addZipInputStream(zip);
            //存储
            builder.deploy();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
//        //持有查询
////        taskService.createTaskQuery().taskOwner(u.getId()).list();
//        //设置代理人
////        taskService.claim(task.getId(),u.getId());
////        taskService.createTaskQuery().taskAssignee(u.getId());
    }

    /**
     * 用户虚拟化
     * @return
     */
    @RequestMapping("/userSer")
    public String userSerializable(){
        //真实项目下不允许
        Task task = taskService.newTask(UUID.randomUUID().toString().replaceAll("-",""));
        task.setName("测试任务1");
        taskService.saveTask(task);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("测试用户");

        //用户虚拟化
        taskService.setVariable(task.getId(),"user1",userEntity);
        //拿到用户的值
        UserEntity entity = taskService.getVariable(task.getId(),"user1",UserEntity.class);
        System.out.println(entity);
        return "success";
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
     * 并行网关流程测试
     * @return
     */
    @RequestMapping("/binxin")
    public String binXin(){
        //流程部署
        Deployment dep = repositoryService.createDeployment().addClasspathResource("processes/tree.bpmn").deploy();
        //流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();

        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());
        //会在 execution表 中存在一个key值 方便查询
//        ProcessInstance pi1 = runtimeService.startProcessInstanceById(pd.getId(),"abc");
        System.out.println("流程实例id: " + pi.getId());

        /**查询当前人的个人任务**/
        List<Task> tasks = taskService.createTaskQuery().//创建任务查询对象
                taskAssignee("").//指定个人任务查询 指定办理人
                list();
        tasks.forEach(task -> {
            System.out.println("任务id: "  + task.getId());
            System.out.println("任务名称： " + task.getName());
            System.out.println("任务的创建时间：" + task.getCreateTime());
            System.out.println("任务的办理人： " + task.getAssignee());
            System.out.println("流程实例ID: " + task.getProcessInstanceId());
            System.out.println("执行对象ID: " + task.getExecutionId());
            System.out.println("流程定义ID: " + task.getProcessDefinitionId());
        });
        return "";
    }
    /**查询当前人的个人任务**/
    public String findTask(){
        /**查询当前人的个人任务**/
        List<Task> tasks = taskService.createTaskQuery().//创建任务查询对象
                taskAssignee("")//指定个人任务查询 指定办理人
                .taskCandidateOrAssigned("") //组任务的办理人查询
                .processDefinitionId("")//使用流程定义的id 查询
                .processInstanceId("")//使用流程实例id 查询
                .executionId("")//使用执行对象id查询
                .orderByTaskAssignee()//排序
                .list();


        tasks.forEach(task -> {
            System.out.println("任务id: "  + task.getId());
            System.out.println("任务名称： " + task.getName());
            System.out.println("任务的创建时间：" + task.getCreateTime());
            System.out.println("任务的办理人： " + task.getAssignee());
            System.out.println("流程实例ID: " + task.getProcessInstanceId());
            System.out.println("执行对象ID: " + task.getExecutionId());
            System.out.println("流程定义ID: " + task.getProcessDefinitionId());
        });
        return "";
    }

    /**
     * 查询流程状态
     */
    public void isProcessEnd(){
        String processInstanceId = "";
        ProcessInstance pi = runtimeService.createProcessInstanceQuery() //processInstance 继承 Execution
//                .createExecutionQuery() //效果一样
        .processInstanceId(processInstanceId) // 使用流程定义id查询
        .singleResult();//返回一个

        if (pi == null){
            System.out.println("流程已经结束");
        }else{
            System.out.println("流程没有结束");
        }
    }

    /**
     * 历史任务查询
     * 根据任务人名称
     */
    public  void findHistoryTask(){
        String taskAssignee = ""; //名称
        List<HistoricTaskInstance> lists = historyService
                .createHistoricTaskInstanceQuery() //创建历史任务查询
                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .list();
        lists.forEach(historicTaskInstance -> {
            System.out.println(historicTaskInstance.getId() + "   " + historicTaskInstance.getName());
        });
    }

    /**
     * 查询历史流程实例
     * 根据id 查询
     */
    public void findHistoryPInstance(){
        HistoricProcessInstance taskInstance  = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId("")
                .singleResult();
    }
    /**
     * 删除流程定义
     */
    public void deleteProcessDefinition(){
       repositoryService.deleteDeployment("1");//通过流程定义id 删除流程 只能删除没有启动的流程 否则会抛异常
       repositoryService.deleteDeployment("1",true); //通过流程定义id 删除流程 级联删除
    }

    /**
     * 图片生成
     *
     * @return
     */
    @RequestMapping("/viewPic")
    public String viewPic() throws IOException {
        String deployMentId = "57515";

        /**获取图片资源名称**/
        List<String> list = repositoryService.getDeploymentResourceNames(deployMentId);

        //资源名称
        String resource = "";

        //遍历
        for (String name : list ){
            if (name.indexOf(".png") >= 0){
                resource = name;
            }
        }
        /**
         * 获取图片的输入流
         */
        InputStream in = repositoryService.getResourceAsStream(deployMentId,resource);
        //使用io工具 生成文件
        File f = new File("F:\\" + resource);
        //保存文件
        FileUtils.copyInputStreamToFile(in,f);
        return "success";
    }
    @RequestMapping("viewImg")
    public String viewImage(String id){
        String deployMentId = "57515";

        /**获取图片资源名称**/
        List<String> list = repositoryService.getDeploymentResourceNames(id);

        //资源名称
        String resource = "";

        //遍历
        for (String name : list ){
            if (name.indexOf(".png") >= 0){
                resource = name;
            }
        }
        //此处方法实际项目应该放在service里面
        InputStream in = repositoryService.getResourceAsStream(id,resource);

        try {
            OutputStream out = resp.getOutputStream();
            // 把图片的输入流程写入resp的输出流中
            byte[] b = new byte[1024];
            for (int len = -1; (len= in.read(b))!=-1; ) {
                out.write(b, 0, len);
            }
            // 关闭流
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置流程变量
     */
    public void setVariables(){
        /** 任务流程（正在执行的）**/
        String taskId = "";
        taskService.setVariable(taskId,"请假天数",3);
        //使用map对象
//        taskService.setVariables(taskId,new HashMap<>()); //设置多个
        /** 流程实例 （任务）还可以是 实例id**/
//        runtimeService.setVariable(taskId,"请假天数",3);
//        runtimeService.setVariables(taskId,new HashMap<>());//设置多个
    }
    /**
     * 获得流程变量
     * **/
    public void getVariables(){
        //任务实例
//        taskService.getVariable();
//        taskService.getVariables();
//        taskService.getVariableLocal();
//流程实例
//        runtimeService.getVariable();
//        runtimeService.getVariables();
//        runtimeService.getVariableLocal();
    }

    /**
     * 查询流程变量的历史表
     */
    public void findHistoryVar(){
        List<HistoricVariableInstance> list = historyService.
                createHistoricVariableInstanceQuery()//创建历史的流程变量查询
                .variableName("") //查询条件
                .list();

    }
    /**
     * 并行网关流程参数作用域测试
     * @return
     */
    @RequestMapping("/args")
    public String args(){
        //流程部署
        Deployment dep = repositoryService.createDeployment().addClasspathResource("processes/var_.bpmn").deploy();
        //流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
        /** 存储服务**/
        repositoryService.createProcessDefinitionQuery();//创建一个流程定义
//        .deploymentId(deploymentId)//使用部署对象id查询
//        .processDefinitionId(processDefinitionId)//使用流程定义id 查询
//        .processDefinitionKey(processDefinitionKey) //使用流程定义的key查询
//          .processDefinitionNameLike(processDefinitionNameLike) //使用流程定义的名称模糊查询
        //.orderByProcessDefinitionVersion().asc() //排序
//        .count() //返回结果集数量
//        .listPage(i,il)//分页查询
//        .list()//返回集合列表
//        .singleResult() //返回唯一结果


        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());
        //会在 execution表 中存在一个key值 方便查询
//        ProcessInstance pi1 = runtimeService.startProcessInstanceById(pd.getId(),"abc");

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        tasks.forEach(task -> {
            System.out.println(task.getName());
            //查询执行流
            Execution ex = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
            if (task.getName().equals("taskA")){
                //设置本地参数 当前执行流生效
                runtimeService.setVariableLocal(ex.getId(),"taskVarA","varA");
            }else {
                //设置全局参数 整个流程生效
                runtimeService.setVariable(ex.getId(),"taskVarB","varB");
            }
        });
        //执行下一步
        tasks.forEach(task ->taskService.complete(task.getId()));
        System.out.println(runtimeService.getVariable(pi.getId(),"taskVarA"));
        System.out.println(runtimeService.getVariable(pi.getId(),"taskVarB"));
        System.out.println("流程实例id: " + pi.getId());
        return "";
    }

    /**
     * 流程定义
     * @return
     */
    @RequestMapping("/receive")
    public String receiveTask(){
        //流程部署
        Deployment dep = repositoryService.createDeployment().addClasspathResource("processes/receiveTask.bpmn").deploy();
        //流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();

        //开启流程
        // 流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());

        //只查一个子执行流
        Execution exe = runtimeService.createExecutionQuery().
                processInstanceId(pi.getId()).onlyChildExecutions().singleResult();

        //流程任务
//        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(pi.getId() + "---- 当前节点--：" + exe.getActivityId());

        //向前走
        runtimeService.trigger(exe.getId());

        exe = runtimeService.createExecutionQuery().
                processInstanceId(pi.getId()).onlyChildExecutions().singleResult();

        System.out.println(pi.getId() + "---- 当前节点--：" + exe.getActivityId());

        return "success";
    }
    /**
     * 查询附件
     *
     */
    @RequestMapping("/findImg")
    public String findImg() throws Exception{
        //流程部署
        Deployment dep = repositoryService.createDeployment().addClasspathResource("processes/var_loacl.bpmn").deploy();
        //流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();

        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());

        //流程任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

        //设置任务附件
        Attachment att1 = taskService.createAttachment("web url",task.getId(),pi.getId(),"Attachment1",
                "163 web page","http://www.163.com");
        //创建图片输出流
        File file = new File("F:\\img");
        if (!file.exists()){
//            创建文件夹
            file.mkdirs();
        }
        File f = new File(file,"result.png");
        if (!f.exists()){
            f.createNewFile();
        }
        InputStream is = new FileInputStream(f);
        //设置输出流为任务附件
        Attachment att2 = taskService.createAttachment("web url",task.getId(),pi.getId(),"Attachment2",
                "image InputStream",is);

        //根据流程实例id 查询附件
        List<Attachment> attas1 = taskService.getProcessInstanceAttachments(pi.getId());
        System.out.println("流程附件数量： " + attas1.size());

        //根据任务id 查询附件
        List<Attachment> attas2 = taskService.getTaskAttachments(task.getId());
        System.out.println("任务附件数量： " + attas1.size());

        Attachment attResult = taskService.getAttachment(att1.getId());
        System.out.println("附件一名称：  " + attResult.getName());

         InputStream stream1 = taskService.getAttachmentContent(att1.getId());
        System.out.println("附件1的输入了流： "  + stream1);

        InputStream stream2 = taskService.getAttachmentContent(att2.getId());
        System.out.println("附件2的输入流: " + stream2);
        return "";
    }

    /**
     * 设置本地任务参数 只在本地有效
     * @return
     */
    @RequestMapping("/local")
    public String localBpnm(){
        //流程部署
        Deployment dep = repositoryService.createDeployment().addClasspathResource("processes/var_loacl.bpmn").deploy();
        //流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());
        //流程任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(task.getName());
        //本地有效 执行 taskService.complete(task.getId()) 就会消失
        taskService.setVariableLocal(task.getId(),"days",3);
        System.out.println("当前任务： " + task.getName() + " , days参数：" + taskService.getVariableLocal(task.getId(),"days"));
        //执行下一步
        taskService.complete(task.getId());
        //流程任务定义
        task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println("当前任务： " + task.getName() + " , days参数：" + taskService.getVariableLocal(task.getId(),"days"));
        //
        return "";
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


        rs.createDeployment().addClasspathResource("processes/first.xml").deploy();

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
