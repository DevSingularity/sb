# Spring IOC Container

In the previous module we learned about Dependency Injection and IoC

We implemented an example of DI.

In this module, we will do the same thing but using **Spring Context**

## 1. Steps: `$1`

- Install `spring-context` dependency from MVN library
- By putting `@Component` before the class initialization of any module, we are telling spring context to create Bean of that class. (every bean is a class, but every class is not a bean)
- Then in `Main.java` we initialize the spring context using the below snippet:
    ```java
      ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
      OrderService order = context.getBean(OrderService.class);
      order.placeOrder();  
    ```
  
- Here `AppConfig` is a file which determines the rules for Spring Context
- In `AppConfig`, `@Configuration` tells that this is a configuration class/file; `@ComponentScan("org.example")` tells to scan all the classes which have a `@Component` annotation in the `"org.example"` package (if no package given, it assumes for the classes of the directory it is in).
- Then when in any Service file, to initialize/inject an outside object (DI), we directly use the `@Autowired` with the method used to initialize and it takes care of initializing and DI of that class' object.

## 2. BTS of steps of IOC Container:

1. Spring starts the container.
2. Spring reads the AppConfig rules.
3. Spring processes `@ComponentScan`.
4. Spring finds `@Component` classes.
5. Spring creates Bean definitions.

### Bean Definition: 
**Metadata of the class for further processing of spring to become easier**

```java
PaymentService:

Bean name: paymentService,
Bean class
Scope
Dependency
```

6. Spring starts creating objects.

```java
PaymentService payment = new PaymentService();

OrderService order = new OrderService(payment);
```

7. Our application uses those beans.

## 3. Dealing with Interfaces (added for loose coupling) and multiple classes implementing it. `$2`

1. We converted all that previoud code to an Interface which is implemented by two services `CardPayment` and `UpiPayment` service.
2. Now when put `@Component` in one, it runs fine. But when put in both, we get an error saying that beans expected one class to create the object.
3. To solve this we use:
   - `@Primary` : whenever in confusion, this class is used as default. This will be the primary bean by default.
   - `@Qualifier` : put this at top of both the classes, and then in the constructor, when creating the object, define the type using `@Qualifier("cardPayment")` (name in the bracket is the name of the bean, i.e name of class but first char is small). Also we can give the bean a name when wiring the `@Qualifier` at the top by using it like `@Qualifier("cp")`; and then use it in the constructor with this new name. (Similar for all the 3 types of DI)

## 4. Dealing with cases when `@Component` tag fails. `#3` (Spring is not able to handle the class making by itself)

- Problems when:
    1. The constructor is complicated like the constructor in `User.java` constructor. (all the values are not objects of other class, instead they are primitive variables etc.)
    2. When using external packages/libraries: we cannot edit their code as they are in ByteCode. Eg. `ProductService`

- Solution:
    1. write rules in the `AppConfig` file through which we will tell the spring context of how to create custom beans.
        For that we will use the `@Bean` annotation in the `AppConfig` file.
    2. This is called creating custom beans. Thus now we have two methods to create beans: 1. by using the `@Component` annotation, 2. by using the `@Bean` annotation

- Ok. So again we face the problem of Interface that `PaymentService` is implemented by two services, so how will spring know which one to use?
- Solution $3.4

**When both `@Component` and `@Bean` is used for a single class, only a single bean is created following the `@Bean` annotation.. it overrides the component declaration.**