# Spring Boot Core:

Spring Core - 1. Annotation based, 2.XML based

SpringBoot- Annotation based

`@SpringBootApplication` is an annotation that calls all of these combined- `@SpringBootConfiguration`, `@EnableAutoConfiguration` and `@ComponentScan`

- `@SpringBootConfiguration` = `@Configuration` is the main config file (just like AppConfig in prev)
- `@EnableAutoConfiguration`: we are telling SB, "Look at my project and create important beans"
- `@AutoConfiguration`: some classes whose beans are automatically created by SB (not us)... written inside SpringBoot by default.

So when called `@EnableAutoConfiguration`, we are telling springboot that it can create the `@AutoConfiguration` part by itself

## Application properties:

Some files which allows us not to hardcode values in code files, instead take values from outside.

Similar to .env file we use in node.js

Some example files may be:
- application.properties
- application.yaml
- Environment variables
- command line argument
- system properties

And we use this value in our app using `@Value("propertyName.something")`

Eg.

```java
public PaymentGateway(@Value("${paymentGateway.type}") String type, @Value("${paymentGateway.retryCount}") int retryCount) {...}
```

If value exists use that, if not use paytm
```java
@Value("${paymentGateway.type:paytm}")
private String type;
```

How to escape the complexity of writing `@Value` tag again and again for every variable?? using `@ConfigurationProperties`

eg. `PaymentProperties.java`


## How to run the app:

Using `@ApplicationRunner` annotation, eg. `DemoRunner.java`

By overriding `run()` method of `ApplicationRunner` and `CommandLineRunner`