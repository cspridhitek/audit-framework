package com.ridhitek.audit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import com.ridhitek.audit.audit.AuditInterceptor;

@Component
public class HibernateInterceptorPostProcessor implements BeanPostProcessor {

    private final AuditInterceptor auditInterceptor;

    @Autowired
    public HibernateInterceptorPostProcessor(AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            LocalContainerEntityManagerFactoryBean emFactoryBean = (LocalContainerEntityManagerFactoryBean) bean;
            emFactoryBean.getJpaPropertyMap().put("hibernate.session_factory.interceptor", auditInterceptor);
        }
        return bean;
    }
}
