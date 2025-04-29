# 🎬 University Cinema Hall Reservation System – Architecture Overview

## 📐 Main Architecture Patterns

The **University Cinema Hall Reservation System** follows a layered and modular software architecture built primarily on the following patterns:

- **Model-View-Controller (MVC)**
- **Service Layer**
- **Factory Pattern**
- **DAO (Data Access Object) Pattern**
- **Singleton Pattern (implicit usage)**

---

## 🧩 MVC Pattern

The **MVC (Model-View-Controller)** pattern structures the application into three core components:

- **Model**  
  Located in `com.ucinema.model.entities`, this package includes business entities such as:
  - `Movie`
  - `Student`
  - `Reservation`
  - `Hall`

- **View**  
  Found in `com.ucinema.view`, this package includes JavaFX-based UI components such as:
  - `LoginScreen`
  - `StudentDashboard`
  - Tabbed panes for Movies, Reservations, and Profile

- **Controller (via Services)**  
  Service classes act as the controller layer:
  - Handle interactions between the view and the model
  - Enforce business rules and logic
  - Examples: `ReservationService`, `MovieService`, `HallService`

---

## 🧪 Service Layer Pattern

Business logic is encapsulated in a **Service Layer** that:

- Mediates between the UI and data access layers
- Provides clean APIs for operations like:
  - Searching movies
  - Reserving seats
  - Managing user profiles
- Delegates database interactions to the DAO layer

---

## 🏭 Factory Pattern

A clear implementation of the **Factory Pattern** is found in the **HallService** class:

- **Hall Creation**
  - `addHall()` creates `Hall` objects based on standardized layouts
  - Handles logic for mapping custom capacities to nearest standard sizes

- **Seat Factory**
  - `createSeatsForHall()` builds seat arrangements dynamically
  - Supports multiple seat types (standard, premium, accessible)
  - Applies rules for layout formatting (e.g., center aisles)

- **Layout Mapping**
  - Uses `STANDARD_LAYOUTS` as a configuration map
  - Ensures layout consistency and scalability

---

## 🗃 DAO Pattern

The **DAO (Data Access Object)** pattern is implemented via classes like:

- `MovieDAO`
- `ReservationDAO`
- `StudentDAO`

These classes abstract all database access logic, promoting:

- Loose coupling
- Maintainability
- Easy testing and mocking

---

## ♻️ Singleton Pattern (Implicit)

Service classes behave as **singletons**:

- Only one instance per service is expected at runtime
- Ensures consistency and shared state across the app
- Could be explicitly managed using dependency injection in future iterations

---

## 📊 Custom Data Structures

The system utilizes domain-specific data structures to enhance performance:

| Structure               | Purpose                                               |
|-------------------------|-------------------------------------------------------|
| `HallGraph`             | Models seat adjacency/relationships as a graph        |
| `ReservationLinkedList` | Dynamic reservation management via linked list        |
| `ScheduleBST`           | Efficient movie schedule lookup (binary search tree)  |
| `StudentHashTable`      | Fast access to student data via hashing               |

These choices reflect a focus on optimizing for the system’s specific performance needs.

---

## ✅ Summary

The system demonstrates strong software architecture through:

- Separation of concerns via MVC
- Well-defined service and data access layers
- Efficient object creation using the Factory Pattern
- Performance-oriented use of custom data structures

This architecture enables modularity, maintainability, and scalability — ideal for an academic or production-level cinema reservation system.

---
