# Sample Application for LinkedIn APIs

> Please take a 1-minute survey to help us help you, with more Sample Apps for LinkedIn APIs.
> Go to **www.slido.com** and use the code **SampleApp** to answer the survey

## Overview

Sample Application is a ready-to-use code example that enables you to try out RESTful calls to LinkedIn's APIs. The application provides scalable and customizable code for your requirements as you begin API development with LinkedIn.

The sample application contains the client and server component you can use to manage your requests to LinkedIn's APIs. The server creates and stores your access token and invokes APIs upon request from the client application. You can download or clone the OAuth sample application and try out these APIs.

> **Note**: For a detailed demo, please visit LinkedIn's public documentation page

The sample application uses the following development tools:

* Spring Boot 3.3.6: Used as web server framework [<https://spring.io/projects/spring-boot>]
* Kotlin 2.0.21: Modern JVM language with enhanced safety and conciseness
* LinkedIn OAuth 2.0: user authorization and API authentication
* Gradle 8.12.1: app building and management
* Amazon Corretto JDK 21.0.6: Required for development and runtime

## Prerequisites

* Ensure that you have an application registered in [LinkedIn Developer Portal](https://developer.linkedin.com/).
Once you have your application, note down the Client ID and Client Secret
* Add <http://localhost:8080/login> to the Authorized Redirect URLs under the **Authentication** section
* Java 17 or later installed on your system
* Gradle 8.12.1 is included in this project via the Gradle wrapper, so no separate installation is required

## Configure the application

**Configure the client app:**

 1. Navigate to the **application.properties** file. You can find this file under: **/client/src/main/resources/application.properties**
 1. To edit server link or port with custom values modify the following values:

    > server.port = <replace_with_required_port_no>

    > SERVER_URL = <replace_with_required_server_url>

 1. Save the changes.

**Configure the server app:**

 1. Navigate to the server directory and copy the **.env.example** file to **.env**:
    ```bash
    cd server
    cp .env.example .env
    ```
 2. Edit the .env file and replace the placeholder values with your actual LinkedIn app credentials:

    ```
    CLIENT_ID=<replace_with_client_id>
    CLIENT_SECRET=<replace_with_client_secret>
    REDIRECT_URI=<replace_with_redirect_url_set_in_developer_portal>
    SCOPE=<replace_with_api_scope>
    CLIENT_URL=<replace_with_client_url>
    ```

    Example:
    ```
    CLIENT_ID=77eyglizzofb4g
    CLIENT_SECRET=your_client_secret_here
    REDIRECT_URI=http://localhost:8080/login
    SCOPE=openid,profile,email,w_member_social
    CLIENT_URL=http://localhost:8989/
    ```

 3. Save the changes.

**Note:**
- The .env file is automatically ignored by git to keep your credentials secure. Never commit your actual credentials to version control.
- The application uses Spring Boot's native .env file support, so the .env file must be in the server directory.
- When running the server, it will automatically load the environment variables from the .env file.

## Start the application

To start the server:

1. Navigate to the server folder.
2. Open the terminal and run the following command to build and run the spring-boot server:
```
./gradlew bootRun
```

> **Note:** The server will be running on <http://localhost:8080/>

To start the client:

1. Navigate to the client folder.
2. Open the terminal and run the following command to build and run the spring-boot client:
```
./gradlew bootRun
```

> **Note**: The client will be running on <http://localhost:8989/>

Alternatively, you can build both modules from the root directory:

```
# Build both modules
./gradlew build

# Run the server
cd server
./gradlew bootRun

# In another terminal, run the client
cd client
./gradlew bootRun
```

## Gradle Migration

This project has been migrated from Maven to Gradle 8.12.1, upgraded to Spring Boot 3.3.6, and ported to Kotlin 2.0.21. The changes include:

1. Creation of Gradle build files for the root project and each module
2. Configuration of Spring Boot 3.3.6 plugin for both client and server modules
3. Setup of Gradle wrapper version 8.12.1
4. Upgrade of Java compatibility to Java 17 (required for Spring Boot 3)
5. Update of dependencies to versions compatible with Spring Boot 3.3.6
6. Conversion of Java classes to Kotlin 2.0.21
7. Addition of Kotlin-specific plugins and dependencies

To build the project with Gradle, use the provided Gradle wrapper:

```
./gradlew build
```

## List of dependencies

|Component Name |License |Linked |Modified |
|---------------|--------|--------|----------|
|[boot:spring-boot-starter-parent:2.5.2](<https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-parent/2.5.2>) |Apache 2.0 |Static |No |
|[boot:spring-boot-starter-parent:2.5.2](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-parent/2.5.2) |Apache 2.0 |Static |No |
|[org.springframework.boot:spring-boot-starter-thymeleaf:2.2.2.RELEASE](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-thymeleaf/2.2.2.RELEASE) |Apache 2.0 |Static |No |
|[org.springframework.boot:spring-boot-devtools:2.6.0](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools/2.6.0) |Apache 2.0 |Static |No |
|[com.fasterxml.jackson.core:jackson-databind:2.13.0](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.13.0)                                     |Apache 2.0 |Static |No |
|[com.fasterxml.jackson.core:jackson-core:2.13.0](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.13.0) |Apache 2.0 |Static |No |
|[org.springframework.boot:spring-boot-starter-web:2.5.2](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web/2.5.2) |Apache 2.0 |Static |No |
| [org.springframework.boot:spring-boot-starter-test:2.6.0](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test/2.6.0) |Apache 2.0 |Static |No |
|[org.springframework:spring-core:5.3.13](https://mvnrepository.com/artifact/org.springframework/spring-core/5.3.13) |Apache 2.0 |Static |No |
