package org.carrot.scheduler.center.controller;

import org.carrot.scheduler.center.manager.JobManager;
import org.carrot.scheduler.center.vo.FrontTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class MainController {

    @Autowired
    private JobManager centerRegister;


    @RequestMapping("/login.html")
    public String login(){
        return "login";
    }

    @RequestMapping("/login")
    public String login_1(){
        return login();
    }

    @RequestMapping("/")
    public String login_2(){
        return login();
    }

    @RequestMapping("/index.html")
    public String index(ModelMap modelMap){
        List<FrontTask> taskList = centerRegister.getAll();
        if(taskList!=null && taskList.size()>0){
            for(FrontTask task : taskList){
                switch (task.getState()){
                    case NONE:
                        task.setStateDesc("未知");
                        break;
                    case NORMAL:
                        task.setStateDesc("正常");
                        break;
                    case PAUSED:
                        task.setStateDesc("暂停");
                        break;
                    case COMPLETE:
                        task.setStateDesc("完成");
                        break;
                    case ERROR:
                        task.setStateDesc("错误");
                        break;
                    case BLOCKED:
                        task.setStateDesc("运行中");
                        break;
                    default:
                        task.setStateDesc(task.getState().name());
                }
            }
        }
        modelMap.put("taskList", taskList);
        modelMap.put("frontTask", new FrontTask());
        return "index";
    }


    @RequestMapping("/forbid.html")
    public String forbid(FrontTask frontTask, ModelMap modelMap){
        String group = frontTask.getGroup();
        String jobName = frontTask.getJobName();
        centerRegister.stopJob(group, jobName);
        return "redirect:index.html";
    }

    @RequestMapping("/restart.html")
    public String restart(FrontTask frontTask, ModelMap modelMap){
        String group = frontTask.getGroup();
        String jobName = frontTask.getJobName();
        centerRegister.restartJob(group, jobName);
        return "redirect:index.html";
    }

    @RequestMapping("/delete.html")
    public String delete(FrontTask frontTask, ModelMap modelMap){
        String group = frontTask.getGroup();
        String jobName = frontTask.getJobName();
        centerRegister.deleteJob(group, jobName);
        return "redirect:index.html";
    }

    @RequestMapping("/modify.html")
    public String modify(FrontTask frontTask, ModelMap modelMap){
        String group = frontTask.getGroup();
        String jobName = frontTask.getJobName();
        String cronExpression = frontTask.getExpression();
        centerRegister.modifyJob(group, jobName, cronExpression);
        return "redirect:index.html";
    }

}
