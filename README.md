# Crypto Market Service


Crypto Market Service is an application that fetches and processes real-time cryptocurrency market data from the CoinGecko API. It uses a clean hexagonal architecture to separate domain logic, application rules, and infrastructure concerns. The service continuously retrieves market data, chart data, and detailed information while dispatching events to downstream systems.

  

## Table of Contents

  

- [Features](#features)

- [Architecture](#architecture)

- [Getting Started](#getting-started)

- [Building the Project](#building-the-project)

- [Running the Application](#running-the-application)

- [Endpoints](#endpoints)

- [Testing](#testing)

- [Docker](#docker)

- [CI/CD](#cicd)

- [License](#license)

  

## Features

  

-  **Market Data Retrieval:**

Fetches current market data, chart data, and detailed information for various cryptocurrencies.

-  **Event Dispatching:**

Publishes events with updated market information to an external event dispatcher.

-  **Fetch Process Management:**

Manages periodic fetching using concurrent jobs to continuously update data.

-  **Hexagonal Architecture:**

Clearly separates domain models, application logic, and external adapters.

  

## Architecture

  

Crypto Market Service follows a hexagonal architecture:

  

-  **Domain Layer:**

Contains business models such as [Crypto](./app/src/main/kotlin/it/unibo/domain/Crypto.kt), [CryptoChartData](./app/src/main/kotlin/it/unibo/domain/CryptoChartData.kt), [CryptoDetails](./app/src/main/kotlin/it/unibo/domain/CryptoDetails.kt), and [Currency](./app/src/main/kotlin/it/unibo/domain/Currency.kt).

  

-  **Application Layer:**

Implements business rules via services such as [FetchCoinMarketDataService](./app/src/main/kotlin/it/unibo/application/FetchCoinMarketDataService.kt) and [FetchProcessManager](./app/src/main/kotlin/it/unibo/application/FetchProcessManager.kt).

  

-  **Infrastructure Adapter:**

Provides adapters for external systems including:

- HTTP client and API communication via [CryptoRepositoryImpl](./app/src/main/kotlin/it/unibo/infrastructure/adapter/CryptoRepositoryImpl.kt)

- Event dispatching via [EventDispatcherAdapter](./app/src/main/kotlin/it/unibo/infrastructure/adapter/EventDispatcherAdapter.kt)

- Web server endpoints via [WebServer](./app/src/main/kotlin/it/unibo/infrastructure/adapter/WebServer.kt)

  

## Getting Started

  

### Clone the Repository

```bash

git clone https://github.com/CryptoMonitorASW-SPE/crypto-market.git

cd  crypto-market

```
### Building the project
Use the Gradle wrapper to build the project. The build configuration is defined in `build.gradle.kts`.

``` ./gradlew build ```

This command compiles the code, runs tests, and creates a fat JAR in the `build/libs` directory.

### Running the application

The application starts a Ktor web server with the main entry point in Main.kt. To run the application:

``` ./gradlew run ```

The server listens on port `8080` by default.

## Endpoints

The web server exposes the following endpoints:

-   **POST `/start`**\
    Starts the data fetching process for a specified currency (e.g., USD or EUR).

    -   **Query Parameter:** `currency` (defaults to `USD`)
    -   **Response:**
        -   If the process is already running:
            -   Returns a status indicating whether data was sent to the event dispatcher or that no data is available.
        -   If not running:
            -   Starts the process and returns a "started" status.
-   **POST `/stop`**\
    Stops the data fetching process for the specified currency.

    -   **Query Parameter:** `currency` (defaults to `USD`)
    -   **Response:** JSON with the status set to "stopped" and the currency code.
-   **GET `/status`**\
    Returns the running status for all supported currencies as a JSON map.

-   **GET `/data`**\
    Retrieves the latest fetched market data for the given currency.

    -   Responds with HTTP status `204 No Content` if no data is available.
-   **GET `/health`**\
    A simple health-check endpoint returning the server's status.

-   **GET `/chart/{coinId}/{currency}/{days}`**\
    Retrieves chart data for a specific cryptocurrency.

    -   **Path Parameters:**
        -   `coinId`: Identifier of the cryptocurrency.
        -   `currency`: The currency code.
        -   `days`: Number of days for chart data.
-   **GET `/details/{coinId}`**\
    Retrieves detailed information for the specified cryptocurrency.

    -   **Path Parameter:**
        -   `coinId`: Identifier of the cryptocurrency. 

## Testing

The project includes unit and integration tests with JUnit5.

To run tests:

`./gradlew test`


## Docker


A Dockerfile is provided to build a Docker image of the application.

To build and run the Docker container:

`docker build -t cryptomarket .\`
`docker run -p 8080:8080 cryptomarket`


## CI/CD

### Automatic Releases

When a push is made to the main branch, an automatic release process is triggered. If a new release is detected, a new Docker package is built and published to [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).


### CI/CD for Docker

The CI/CD pipeline ensures that every release includes an updated Docker image. When a new release is created:

-   A Docker image is built.

-   The image is pushed to GHCR.

-   The latest tag is updated.

### Code Quality and Testing

For each pull request, the following checks are performed:

-   **Code Quality and Smell Analysis**: The code is analyzed using [Detekt](https://detekt.dev/) to ensure it adheres to Kotlin best practices and to check for code smells.

-   **Test Validation**: The test suite is executed to verify that all tests pass before merging.

### KtLint

The project uses [KtLint](https://github.com/pinterest/ktlint) for Kotlin code style enforcement.\
It is integrated into the commit process using Husky, meaning you cannot commit code that violates style rules.

### Commitlint

[Commitlint](https://commitlint.js.org/) is configured to enforce commit message conventions.\
It ensures that all commits follow a standardized format, improving readability and maintainability.

License
-------

This project is licensed under the MIT License.