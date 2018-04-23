package com.activiti.controller;

import com.activiti.api.ResultEntity;
import com.activiti.entity.Definittion;
import com.activiti.entity.LeaveForm;
import com.activiti.entity.TaskEntity;
import com.activiti.util.FiledUtil;
import com.activiti.util.UploadFlies;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 *
 * activiti 流程部署文件上传
 */
@Api("文件上传部署")
@RestController
@RequestMapping("/activiti")
public class ActivitiFileController {
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
    /**
     * 流程实例的KEY
     */
    private static final String LEAVKEY = "myfirst_1";

    @ApiOperation(value = "文件上传",  produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/upload")
    public ResultEntity fileUpload(@RequestParam("file") MultipartFile file,
                                   HttpServletRequest request){
        String path = "processes/";
        //file.getName() 不带格式
        //全名称
        String fileName = file.getOriginalFilename();
        try {
            UploadFlies.uploadFile(file.getBytes(),path,fileName);
            System.out.println(file.getOriginalFilename());
            System.out.println(file.getName());

        } catch (Exception e) {
            return ResultEntity.error(e.getMessage());
        }
        //
        return ResultEntity.success("success",null);
    }

    /**
     * 开始请假流程
     * @return
     */
    @ApiOperation(value = "请假申请!!!")
    @PostMapping("/leave")
    public ResultEntity leave(@RequestBody LeaveForm form){
        if (form.getName()== null || form.getName().equals("")){
            return ResultEntity.error("请输入有效值!!!");
        }
        //设置认证
        identityService.setAuthenticatedUserId(form.getName());
        //拿到流程实例
        //使用key实例流程 （每次拿到最新的流程）
        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(LEAVKEY);
        System.out.println(pi.getId());
        //查询任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //声明
        taskService.claim(task.getId(),form.getName());
        Map<String,Object> map = new HashMap<>();
        map.put("days",form.getDays());
        map.put("yy",form.getYy());
        taskService.complete(task.getId(),map);
        return ResultEntity.success("success","申请成功");
    }

    /**
     * 审批
     * @param taskId
     * @param name
     * @param message
     * @return
     */
    @PostMapping("/passAudit")
    public ResultEntity passAudit(String taskId,String name,String message){
        if (taskId == null|| taskId.equals("") || name.equals("") || name == null || message.equals("") || message == null){
            return ResultEntity.error("审批失败!!");
        }

        //设置流程变量
        Map<String,Object> map = new HashMap<>();
        map.put("message",message);
        map.put("auditor",name);
        map.put("auditTime",new Date());
        //声明
        taskService.claim(taskId,name);
        //审批
        taskService.complete(taskId,map);
        return ResultEntity.success("审批成功!!",null);
    }
    /**
     * 查询我的申请
     * @param name
     * @return
     */
    @GetMapping("/selectMyLeav")
    public ResultEntity selectMyLeaving(String name){
        if (name == null || name.equals("")){
            return ResultEntity.error("查询失败!!");
        }
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .startedBy(name)
                .list();
        List<LeaveForm> resultForm = new ArrayList<LeaveForm>();
        instances.forEach(instance -> {
            Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
            System.out.println("id: " + task.getId() + "  name: " + task.getName());
            LeaveForm form = getVal(instance);
            resultForm.add(form);
        });
        return ResultEntity.success("查询成功!!!",resultForm);
    }

    /**
     * 销假查询 查询量少
     * @param name
     * @return
     */
    @GetMapping("/endLeaveByInstance")
    public ResultEntity endLeaveByInstance(String name){
        if (name.equals("")||name == null){
            return ResultEntity.error("查询失败!!!");
        }

        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .startedBy(name)
                .list();
        List<LeaveForm> resultForm = new ArrayList<LeaveForm>();
        instances.forEach(instance -> {
            Task task = taskService.createTaskQuery().processInstanceId(instance.getId())
                    .singleResult();
            if ("销假".equals(task.getName())){
                LeaveForm form = getVal(instance);
                form.setId(task.getId());
                setManageVal(form,instance);
                resultForm.add(form);
            }
        });
        return ResultEntity.success("查询成功!!!",resultForm);
    }
    /**
     * 销假查询
     * @param name
     * @return
     */
    @GetMapping("/endLeave")
    public ResultEntity endLeave(String name){
        // 1 .先任务查询
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(name)
                .orderByTaskCreateTime().desc().list();
       //定义返回集
        List<LeaveForm> resultForm = new ArrayList<LeaveForm>();

        tasks.forEach(task -> {
            //获得流程实例id
            String instanceId = task.getProcessInstanceId();
            //获得流程对象
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            String message = runtimeService.getVariable(instance.getId(),"message",String.class);
            if (name.equals(instance.getStartUserId())){
                LeaveForm form = getVal(instance);
                form.setId(task.getId());
                setManageVal(form,instance);
                resultForm.add(form);
            }

        });
        return ResultEntity.success("查询成功!!!",resultForm);
    }

    /**
     * 销假执行
     * @param taskId
     * @param name
     * @return
     */
    @PostMapping("/doEndLeave")
    public ResultEntity doEndLeave(String taskId,String name){

        if (taskId.equals("")|| taskId == null|| name.equals("")||name == null){
            return ResultEntity.error("名字或者任务id不为空!!!");
        }
        Map<String,Object> map = new HashMap<>();
        //销假时间
        map.put("endLeaveTime",new Date());
        //声明
        taskService.claim(taskId,name);
        //审批
        taskService.complete(taskId,map);
        return ResultEntity.success("销假成功!!!",null);
    }
    //获得经理审批的参数
    public void setManageVal(LeaveForm form,ProcessInstance instance){
        String message = runtimeService.getVariable(instance.getId(),"message",String.class);
        String auditor = runtimeService.getVariable(instance.getId(),"auditor",String.class);
        form.setAuditor(auditor);
        form.setMessage(message);
        form.setAuditTime(runtimeService.getVariable(instance.getId(),"auditTime",Date.class));
        form.setStatus("申请成功");
    }
    //获得申请是的参数
    public LeaveForm getVal(ProcessInstance instance){
        Integer days = runtimeService.getVariable(instance.getId(),"days",Integer.class);
        String reason = runtimeService.getVariable(instance.getId(),"yy",String.class);
        Date creatTime = instance.getStartTime();
        String status = instance.isEnded()? "申请结束":"等待审批";

        LeaveForm form = new LeaveForm();
        System.out.println(instance.getStartUserId());
        form.setName(instance.getStartUserId());
        form.setDays(days);
        form.setYy(reason);
        form.setDate(creatTime);
        form.setStatus(status);
        return form;
    }
    //所属任务查询
    @ApiOperation(value = "所属任务查询!!!")
    @GetMapping("/selectTask")
    public ResultEntity selectTask(String name){
       System.out.println(name);
        //任务查询
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(name)
                .orderByTaskCreateTime().desc().list();
        List<TaskEntity> resultTask = new ArrayList<TaskEntity>();
        tasks.forEach(task -> {
            TaskEntity taskEntity = new TaskEntity();
            String id = task.getId();
            String taskName = task.getName();
            Date creatDate = task.getCreateTime();
            taskEntity.setId(id);
            taskEntity.setName(taskName);
            taskEntity.setCreatDate(creatDate);
            String instanceId = task.getProcessInstanceId();
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            taskEntity.setForm(getVal(instance));
            resultTask.add(taskEntity);
        });
        return ResultEntity.success("success",resultTask);
    }

    /**
     * 历史的申请记录查询
     * @param name
     * @return
     */
    @GetMapping("/hisLeave")
    public ResultEntity hisLeave(String name){
        //通过key查历史记录
        List<HistoricProcessInstance> histors = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(LEAVKEY)
                .startedBy(name).finished().orderByProcessInstanceEndTime().desc().list();
        List<LeaveForm> formList = new ArrayList<LeaveForm>();
        getLeaveForm(formList,histors);
        return ResultEntity.success("查询成功!!!",formList);
    }

    /**
     *审批记录查询
     *
     * @return
     */
    @GetMapping("/hisAuditList")
    public ResultEntity hisAuditList(String name){
        List<HistoricProcessInstance> histors = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(LEAVKEY)
                .involvedUser(name).finished().orderByProcessInstanceEndTime().desc().list();
        List<LeaveForm> formList = new ArrayList<LeaveForm>();
        getLeaveForm(formList,histors);
        return ResultEntity.success("查询成功!!!",formList);
    }
    //拿到历史流程参数
    public void getLeaveForm(List<LeaveForm> formList, List<HistoricProcessInstance> histors){
        for (HistoricProcessInstance historicProcessInstance : histors){
            LeaveForm leaveForm = new LeaveForm();
            leaveForm.setName(historicProcessInstance.getStartUserId());
            leaveForm.setStatus("申请结束!!!");
            leaveForm.setDate(historicProcessInstance.getStartTime());
            //获得历史流程变量
            List<HistoricVariableInstance> varInstanceList = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicProcessInstance.getId()).list();
            FiledUtil.setVarls(leaveForm,varInstanceList);
            formList.add(leaveForm);
        }
    }

    //部署流程相应的流程文件
    @PostMapping("/activitiBs")
    public ResultEntity activitiBs(String path){
        //部署流程相应的流程文件
        // org/activiti/db/create/activiti.mysql.create.history.sql 数据库文件存放地
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/first.xml").deploy();
        System.out.println(deployment);
        return ResultEntity.success("success",deployment);

    }
    //部署zip流程文件
    @PostMapping("/addZip")
    public ResultEntity addZip(MultipartFile file){
        if (file == null){
            return ResultEntity.error("请上传文件!!!");
        }
        //部署
        Deployment deployment = null;
        //创建存储服务
        FileInputStream fileInputStream = null;
        try {
//            fileInputStream = new FileInputStream(new File("processes/leave.zip"));
            ZipInputStream zip = new ZipInputStream(file.getInputStream());
            //部署
            deployment = repositoryService.createDeployment()
                    .addZipInputStream(zip).deploy();
            System.out.println(deployment);
        }catch (Exception e){
            return ResultEntity.error(e.getMessage());
        }
        return ResultEntity.success("success",deployment);
    }

    //流程流程定义
    @PostMapping("/lcdy")
    public ResultEntity selectActiviti(){
        //
//        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
//                .deploymentId("1").singleResult();
//        System.out.println(def.getKey());
        //使用key实例流程 （每次拿到最新的流程）
        String processDefinitionKey = "myfirst_1";
        //流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        System.out.println(pi);
        //执行流
//        Execution exe = runtimeService.createExecutionQuery().
//                processInstanceId(pi.getId()).onlyChildExecutions().singleResult();
        //流程任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(task.getName());
        return ResultEntity.success("success",task);
    }
    @ApiOperation(value = "查询所有流程" )
    @GetMapping("selectAllpdf")
    public ResultEntity selectAllpdk(){
        //使用key实例流程 （每次拿到最新的流程）
//        String processDefinitionKey = "myfirst_1";
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().desc().list();
        //流程放置的容器
        List<Definittion> definittionList = new ArrayList<Definittion>();
        list.forEach(processDefinition -> {
            Definittion definittion = new Definittion();
            definittion.setDefId(processDefinition.getId());
            definittion.setDefName(processDefinition.getName());
            definittion.setDefKey(processDefinition.getKey());
            definittion.setDemId(processDefinition.getDeploymentId());
            definittion.setResourceName(processDefinition.getResourceName());
            definittion.setVersion(processDefinition.getVersion());
            definittionList.add(definittion);
            System.out.println("流程定义的ID：" + processDefinition.getId());
            System.out.println("流程定义的名称：" + processDefinition.getName());
            System.out.println("流程定义的Key：" + processDefinition.getKey());
            System.out.println("流程定义的部署ID：" + processDefinition.getDeploymentId());
            System.out.println("流程定义的资源名称：" + processDefinition.getResourceName());
            System.out.println("流程定义的版本：" + processDefinition.getVersion());
            System.out.println("########################################################");
        });
        return ResultEntity.success("success",definittionList);
    }
    //查看图像
    @GetMapping("/creatimg")
    public void creatImg(String id)throws Exception{
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
    }

}
