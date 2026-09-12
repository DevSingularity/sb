package org.example;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CartService /*implements DisposableBean*/ {

    HashMap<Integer, Integer> map;

    public CartService() {
        map = new HashMap<>();
        System.out.println("Initializing Cart Service");
    }

    // this init method is called from APpConfig when using the @Bean annotation
    public void start() {
        map.put(1,29);
        map.put(2, 10);
    }

    @PostConstruct //needs jakarta annotations lib
    public void start2() {
        map.put(1,23);
        map.put(2, 40);
    }

//    @Override
//    public void destroy() throws Exception {
//        map.remove(1);
//        System.out.println("Destroying Cart Service");
//    }

    @PreDestroy
    public void stop() {
        map.clear();
        System.out.println("Destroying Cart Service");
    }

    public void addToCart() {
        System.out.println("Adding to cart");
    }

    public int getVal(int key) {
        return map.get(key);
    }
}

/*
@Component
public class CartService implements InitializingBean {

    HashMap<Integer, Integer> map;

    public CartService() {
        map = new HashMap<>();
        System.out.println("Initializing Cart Service");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        map.put(1,29);
        map.put(2, 10);
    }

    public void addToCart() {
        System.out.println("Adding to cart");
    }

    public int getVal(int key) {
        return map.get(key);
    }
}
*/