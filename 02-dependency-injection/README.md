# Dependency Injection:

- A class should ask what it needs, not build everything itself

- IoC- inversion of control (Idea/Principle hai)

- Dependency inversion is a way/approach/technique to achieve IoC

- Spring framework has IOC Container, which creates, manages and connects objects together.

## Code implementation:

- Before the orderService was creating the Notification object and calling it, due to which it was breaking the OCP (as tightly coupled) and SRP (Business logic was mixed with object creation)
- After:
    - Create an Interface for NotificationService
    - Implement that interface in all childs.
    - And then initialize the Notification object in main and then pass/inject it in the OrderService.
    - Then create a constructor in OrderService to set the notif object to the value which was injected.
    - This makes our code losely coupled and also follows SOLID principles.