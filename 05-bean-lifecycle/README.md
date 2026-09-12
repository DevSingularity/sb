# Bean Lifecycle:

### Responsibilities of Spring:
- Discover the bean
- Create the object
- inject dependencies
- call lifecycle methods
- keep the bean ready
- destroy it when required (garbage collection)

### Spring Bean Lifecycle Flow

<details>
  <summary><b>1. Spring Container starts</b></summary>

The application context is initialized (e.g., `AnnotationConfigApplicationContext`), triggering the startup of the Spring IoC container.

We are not passing an object of `AppConfig`, we are passing its **class metadata** instead.

Spring uses **reflection** to inspect that class.
</details>

<details>
  <summary><b>2. Reads configs / annotations</b></summary>

Spring scans your project to parse XML files, `@Configuration` classes, `@Component`, `@Bean`, and other annotations.
</details>

<details>
  <summary><b>3. Creates BeanDefinition</b></summary>

Spring creates a `BeanDefinition` object for each detected bean. This metadata contains configuration details like bean scope, constructor arguments, and lazy/eager initialization flags.

atp, the object is not necessarily created. Spring only registered info about the bean.

**Why does Spring need BeanDefinition first?**

Because spring is not creating just one object, it is building a complete object network

Spring first needs to know:
```txt
Which classes should I manage?
What is bean name?
What is the scope?
What dependencies does it need?
Is it lazy or eager?
Does it have init or destroy methods?
```
</details>

<details>
  <summary><b>4. Instantiates bean object</b></summary>

The container uses reflection to call the bean's constructor and physically creates the Java object instance in memory.

**Instantiation (Creation) and Initialization are different things**
</details>

<details>
  <summary><b>5. Injects dependencies</b></summary>

Spring looks at `@Autowired`, `@Value`, or setter methods to inject the required dependencies and properties into the newly created bean.
</details>

<details>
  <summary><b>6. Calls Aware interfaces</b></summary>

If the bean implements any `Aware` interfaces, Spring injects container-level infrastructure. For example:
* `BeanNameAware` gives the bean its own ID string.
* `BeanFactoryAware` gives it access to the current BeanFactory.
* `ApplicationContextAware` gives it access to the runtime environment.

Eg. `UserService.java`
**Important:**
```java
@Override
public void setBeanName(String name) { // this method runs automatically, i.e it is called by Spring itself, not be us.. so it is a callback method
// this is the default method which is for getting the name of the bean, from spring by default
System.out.println("Bean name is " + name);
} // the bean name does not change here, by using this method. It remains the same as it was in the IOC container...
```
</details>

<details>
  <summary><b>7. Runs initialization callbacks</b></summary>

Spring prepares the bean for usage by running initial setup logic in this exact sequence:
1. Methods annotated with `@PostConstruct`.
2. `afterPropertiesSet()` if the bean implements `InitializingBean`.
3. Custom `init-method` defined in your configuration.

Eg. `CartService.java`
</details>

<details>
  <summary><b>8. Bean is ready to use</b></summary>

The bean is now fully initialized, configured, and managed. It is cached in the container's singleton registry.
</details>

<details>
  <summary><b>9. Application uses the bean</b></summary>

Your application code fetches the bean (via `@Autowired` or `context.getBean()`) and executes its business logic.
</details>

<details>
  <summary><b>10. Runs destructive callbacks</b></summary>

When the container shuts down, Spring safely disposes of the bean by running cleanup logic in this order:
1. Methods annotated with `@PreDestroy`.
2. `destroy()` if the bean implements `DisposableBean`.
3. Custom `destroy-method` defined in your configuration.

Eg. `CartService.java`
</details>

<details>
  <summary><b>11. Bean is removed</b></summary>

All resources held by the bean are released, and the object is evicted from the container to be garbage collected.
</details>

Steps 10 and 11 cannot be used in `Scope("prototype")` as spring only does till Bean ready and then hands over the object to client i.e the user to deal with..

**To avoid memory leak**
### Bean Definition:

```txt
beanName: orderService
beanClass: orderService
scope: singleton
lazy: false
dependency paymentService
```

### Callback methods:
The methods called by spring, not by us.