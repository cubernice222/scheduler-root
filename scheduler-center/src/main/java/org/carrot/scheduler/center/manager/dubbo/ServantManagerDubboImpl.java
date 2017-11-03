package org.carrot.scheduler.center.manager.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.carrot.scheduler.vector.CenterTransmitter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServantManagerDubboImpl implements ApplicationContextAware,EnvironmentAware{

    private static ApplicationContext applicationContext;

    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public  void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static String getRegistryConfigProtocol() {
        return REGISTRY_CONFIG_PROTOCOL;
    }

    public static String getPROTOCOL() {
        return PROTOCOL;
    }

    public static ConcurrentHashMap<String, ReferenceBean<CenterTransmitter>> getServantHolder() {
        return servantHolder;
    }

    public static void setServantHolder(ConcurrentHashMap<String, ReferenceBean<CenterTransmitter>> servantHolder) {
        ServantManagerDubboImpl.servantHolder = servantHolder;
    }

    private static final String REGISTRY_CONFIG_PROTOCOL = "zookeeper";
    private static final String PROTOCOL = "dubbo";

    private static ConcurrentHashMap<String, ReferenceBean<CenterTransmitter>> servantHolder = new ConcurrentHashMap<>();
    public static CenterTransmitter getByAppName(String appName) {
        if(!servantHolder.containsKey(appName)){
            setReferenceBean(appName,false);
        }
        ReferenceConfig<CenterTransmitter> referenceConfig = servantHolder.get(appName);
        CenterTransmitter  centerTransmitter= referenceConfig.get();
        if(centerTransmitter == null){
            setReferenceBean(appName,true);
            referenceConfig = servantHolder.get(appName);
            centerTransmitter = referenceConfig.get();
        }
        return centerTransmitter;
    }

    private static void setReferenceBean(String appName, boolean isInstead){
        String connectString = environment.getProperty("zookeeper.address");
        ReferenceBean referenceBean = new ReferenceBean();
        referenceBean.setApplicationContext(applicationContext);
        referenceBean.setInterface(CenterTransmitter.class);
        referenceBean.setApplication(new ApplicationConfig(appName));
        referenceBean.setVersion(appName);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(connectString);
        registryConfig.setProtocol(REGISTRY_CONFIG_PROTOCOL);
        referenceBean.setRegistry(registryConfig);
        referenceBean.setProtocol(PROTOCOL);
        if(isInstead){
            servantHolder.put(appName, referenceBean);
        }else{
            servantHolder.putIfAbsent(appName, referenceBean);
        }
    }

}
