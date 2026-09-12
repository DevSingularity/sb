package org.example;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("userBean")
public class UserService implements BeanNameAware, ApplicationContextAware {
    public UserService() {
        System.out.println("UserService constructor called");
    }

    @Override
    public void setBeanName(String name) { // this method runs automatically, i.e it is called by Spring itself, not be us.. so it is a callback method
        // this is the default method which is for getting the name of the bean, from spring by default
        System.out.println("Bean name is " + name);
    } // the bean name does not change here, by using this method. It remains the same as it was in the IOC container...


    // this is when we explicitly, as users want to get name
    public String getBean() {
        return "userBean";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("Application name is " + applicationContext.getClass());
    }
}
