# Vehicle Routing with Timefold Solver

A Java-based vehicle routing optimization application using **Timefold Solver**, **Quarkus**, and **Maven**. This project demonstrates how to solve vehicle routing problems with time windows and capacity planning constraints.

![Vehicle Routing Screenshot](vehicle-routing-screenshot.png)

## 🚀 Features

- **Vehicle Route Optimization**: Find the most efficient routes for a fleet of vehicles
- **Time Windows**: Support for delivery time constraints  
- **Capacity Planning**: Vehicle capacity and load optimization
- **Real-time Solving**: Live optimization with immediate visual feedback
- **Web Interface**: Interactive UI for configuring and visualizing routes
- **REST API**: Programmatic access to optimization functions

## 📋 Prerequisites

1. **Java 17 or later** - Install with [SDKMAN](https://sdkman.io/):
   ```bash
   sdk install java
   ```

2. **Maven 3.6+** - Install with SDKMAN:
   ```bash
   sdk install maven
   ```

## 🛠️ Getting Started

### Clone and Run

1. **Clone this repository**:
   ```bash
   git clone https://github.com/caioapp/vehicle-routing-timefold.git
   cd vehicle-routing-timefold
   ```

2. **Start the application**:
   ```bash
   mvn quarkus:dev
   ```

3. **Open your browser** and visit: http://localhost:8080

4. **Click the "Solve" button** to see the optimization in action!

### Live Development

Quarkus supports live coding - make changes to your source code and refresh the browser (F5) to see them immediately applied without restarting the application.

## 🏢 Enterprise Edition (Optional)

For high-scalability production use cases, you can switch to **Timefold Solver Enterprise Edition**:

1. **Configure Maven settings** - Create `.m2/settings.xml` in your home directory:
   ```xml
   <settings>
     <servers>
       <server>
         <id>timefold-solver-enterprise</id>
         <username>your_username</username>
         <password>your_password</password>
       </server>
     </servers>
   </settings>
   ```
   > Contact [Timefold](https://timefold.ai/contact) to obtain enterprise credentials.

2. **Run with Enterprise Edition**:
   ```bash
   mvn clean quarkus:dev -Denterprise
   ```

## 📦 Production Deployment

### Package as JAR

1. **Build the application**:
   ```bash
   mvn package
   ```

2. **Run the packaged JAR**:
   ```bash
   java -jar ./target/quarkus-app/quarkus-run.jar
   ```

   To use a different port:
   ```bash
   java -jar ./target/quarkus-app/quarkus-run.jar -Dquarkus.http.port=8081
   ```

### Docker Container

1. **Build container image**:
   ```bash
   mvn package -Dcontainer
   ```

2. **Run container**:
   ```bash
   docker run -p 8080:8080 --rm $USER/vehicle-routing:1.0-SNAPSHOT
   ```

### Native Executable (GraalVM)

For serverless deployments with faster startup times:

1. **Install GraalVM** and the native-image tool
2. **Compile natively** (takes several minutes):
   ```bash
   mvn package -Dnative
   ```
3. **Run the native executable**:
   ```bash
   ./target/*-runner
   ```

## 🏗️ Project Structure

```
src/main/java/org/acme/vehiclerouting/
├── domain/
│   ├── dto/           # Data transfer objects
│   ├── geo/           # Geographic utilities  
│   ├── Location.java  # Location entity
│   ├── Vehicle.java   # Vehicle entity
│   ├── Visit.java     # Visit/customer entity
│   └── VehicleRoutePlan.java  # Solution class
├── rest/              # REST API endpoints
├── solver/            # Constraint definitions
└── ...
```

## 🔧 Key Technologies

- **[Timefold Solver](https://timefold.ai/)** - AI constraint solver for optimization
- **[Quarkus](https://quarkus.io/)** - Supersonic Java framework  
- **Maven** - Project management and build tool
- **RESTEasy** - JAX-RS implementation for REST APIs
- **Jackson** - JSON processing
- **Bootstrap & jQuery** - Frontend UI framework

## 📊 API Endpoints

- `GET /vehicle-routing` - Get current route plan
- `POST /vehicle-routing/solve` - Start optimization
- `POST /vehicle-routing/stop-solving` - Stop current optimization
- `GET /demo-data` - Load demonstration dataset

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)  
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📚 Resources

- [Timefold Solver Documentation](https://timefold.ai/docs)
- [Quarkus Guides](https://quarkus.io/guides/)
- [Vehicle Routing Problem Overview](https://en.wikipedia.org/wiki/Vehicle_routing_problem)

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

💡 **Tip**: Check out Timefold's [Field Service Routing](https://timefold.ai/docs/timefold-solver/latest/use-cases-and-examples/vehicle-routing/vehicle-routing#field-service-routing) for advanced features like priorities, skills, and fairness constraints.