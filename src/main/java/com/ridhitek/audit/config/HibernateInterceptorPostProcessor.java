package com.ridhitek.audit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import com.ridhitek.audit.audit.AuditInterceptor;

@Component
public class HibernateInterceptorPostProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext; // Fetch lazily to prevent circular dependencies

    @Autowired
    public HibernateInterceptorPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            LocalContainerEntityManagerFactoryBean emFactoryBean = (LocalContainerEntityManagerFactoryBean) bean;

            // Fetch AuditInterceptor lazily to avoid circular dependencies
            AuditInterceptor auditInterceptor = applicationContext.getBean(AuditInterceptor.class);

            // Apply interceptor dynamically to all entity managers
            emFactoryBean.getJpaPropertyMap().put("hibernate.session_factory.interceptor", auditInterceptor);
        }
        return bean;
    }
}
