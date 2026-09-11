# Circular dependency

## Problem `$1`
- In the `simple` file, the code causes a stack overflow as it will get stuck in a circular dependency loop.

- Flow: A-OrderService B-PaymentService
  1. Create A
  2. Inject its dependency
  3. Create B
  4. Inject its dependency
- Can be solved using DI by Fields and setters. which will follow the reflection property and inject the incomplete dependency object in the current object and then later complete it using the reflection of the completed object.

- **Don't solve Circular Dependency, avoid it**

## @Bean `$2`

- when we create two objects from the `.getBean(...)`, both the objects are same
- because by default and when in `Singelton` one bean definition is created for one class and the same is used everywhere

### Bean Scopes: Singleton and Prototype `$3`

- Singleton: only one object **per bean definition** is created and that same one is reused across objects.
    - All beans are created at the build time itself, automatically.
    - Best used for Stateless works
    - Are always Eager initialization, **individual components can be changed to Lazy by using `@Lazy`**
- Prototype: a new object is created per call, on demand. 
    - On demand, only created when it is called/demanded somewhere in the code.
    - Best used for stateful work
    - Is always Lazy initialization (can never be made Eager)
- Request
- Session
- Application