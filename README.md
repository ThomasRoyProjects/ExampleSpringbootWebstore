
# Webstore Project

  

## Overview

  

This is a Java-based webstore application built with **Spring Boot**, **Thymeleaf**, and **MySQL**. The project is containerized using **Docker** and can be easily deployed using **Docker Compose**.

  

## Prerequisites

  

Make sure you have the following installed:

  

- [Docker](https://www.docker.com/)

- [Docker Compose](https://docs.docker.com/compose/)

- [Java 17](https://openjdk.org/projects/jdk/17/) (required for Gradle build)

- [Gradle](https://gradle.org/install/) (or use the included Gradle Wrapper)

  

## Installation & Setup

  

### 1. Clone the Repository

  

```sh

git  clone  https://github.com/ThomasRoyProjects/ExampleSpringbootWebstore.git

cd  ExampleSpringbootWebstore

```

  

### 2. Launch a Temporary MySQL Container for Build

  

Start a temporary MySQL container to provide the database needed for the Gradle build (required for JPA tasks):

  

```sh

docker  run  -d  --name  mysql-db  -e  MYSQL_ROOT_PASSWORD=root  -e  MYSQL_DATABASE=amazingwebstore  -p  3306:3306  mysql:8.0

```

  

This command:

  

- Starts a MySQL container named `mysql-db`

- Sets the root password to `root`

- Creates a database named `amazingwebstore`

- Maps port 3306 to the host

  

Wait for the MySQL container to be ready (this may take a few seconds):

  

```sh

docker  logs  mysql-db 2>&1 | grep  "ready for connections"

```

  

Repeat the `docker logs` command until you see a message indicating the MySQL server is ready.

  

>  **Note**: Ensure port 3306 is free on your host (check with `netstat -tuln | grep 3306` or equivalent).

  

### 3. Build the Spring Boot Application

  

With the MySQL container running, build the JAR file:

  

```sh

./gradlew  build  # For Linux/macOS

# OR

gradlew.bat  build  # For Windows

```

  

After the build completes, stop and remove the temporary MySQL container:

  

```sh

docker  stop  mysql-db

docker  rm  mysql-db

```

  

### 4. Start the Application with Docker Compose

  

Ensure ports 8080 and 3306 are free (check with `netstat -tuln | grep 8080` or equivalent). Then, run the following command to build and start the application and MySQL services:

  

```sh

docker-compose  -f  docker-compose.yml  up  --build  -d

```

  

This will:

  

- Start a MySQL container (named `mysql-db`)

- Build and start the Spring Boot application (named `webstore`)

  

### 5. Verify Running Containers

  

Check that the containers are running:

  

```sh

docker  ps

```

  

You should see the MySQL and application containers (`mysql-db` and `webstore`).

  

### 6. Access the Webstore

  

- Open your browser and go to: [http://localhost:8080](http://localhost:8080)

- MySQL is accessible on port 3306

  

## Admin Panel

  

An administrative interface is available to manage your webstore's products.

  

- 📍 Visit: [http://localhost:8080/admin](http://localhost:8080/admin)

- 🔐 Credentials:

-  **Username**: `admin`

-  **Password**: `adminpass`

  

From the admin panel, you can:

  

- Add new products to your store

- Edit or update existing product details

- Manage inventory and pricing

  

> ⚠️ For security, change these default credentials before deploying to production.

  

## Environment Variables

  

The application uses the following database environment variables, defined in `docker-compose.yml`:

  

```yaml

SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/amazingwebstore?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

SPRING_DATASOURCE_USERNAME: root

SPRING_DATASOURCE_PASSWORD: root

```

  

## Stopping the Application

  

To stop all running containers:

  

```sh

docker-compose  -f  docker-compose.yml  down

```

  

## Troubleshooting

  

- Check logs for the application or MySQL containers:

  

```sh

docker  logs  webstore

docker  logs  mysql-db

```

  

- Ensure ports 8080 and 3306 are free before starting containers.

- If database connection issues occur, try restarting:

  

```sh

docker-compose  -f  docker-compose.yml  down && docker-compose  -f  docker-compose.yml  up  --build  -d

```

  

## License

  

This project is licensed under the **MIT License**.

  

## Author

  

Developed by **Thomas Roy**.
