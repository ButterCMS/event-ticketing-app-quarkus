# event-ticketing-app-quarkus

Building an Event Ticketing App with Quarkus &amp; ButterCMS

This is a demo project that integrates [ButterCMS](https://buttercms.com) with [Quarkus](https://quarkus.io).
It mimics a ticketing platform on which clients can book tickets for events added by editor through Butter.

The building process is documented in the [related blog post](https://buttercms.com/blog/quarkus-tutorial-event-app/).

## Important Notice
This project was created as an example use case of ButterCMS in conjunction with a blog article, [Building an Event Ticketing App with Quarkus & ButterCMS](https://buttercms.com/blog/quarkus-tutorial-event-app/), and will not be actively maintained. 

If you’re interested in exploring the best, most up-to-date way to integrate Butter into Java frameworks like Quarkus, you can check out the following resources:

### Starter Projects

The following turn-key starters are fully integrated with dynamic sample content from your ButterCMS account, including main menu, pages, blog posts, categories, and tags, all with a beautiful, custom theme with already-implemented search functionality. All of the included sample content is automatically created in your account dashboard when you sign up for a free trial of ButterCMS.
- [Java Starter](https://buttercms.com/starters/java-starter-project/)
- [Angular Starter](https://buttercms.com/starters/angular-starter-project/)
- [React Starter](https://buttercms.com/starters/react-starter-project/)
- [Vue.js Starter](https://buttercms.com/starters/vuejs-starter-project/)
- Or see a list of all our [currently-maintained starters](https://buttercms.com/starters/). (Over a dozen and counting!)

### Other Resources
- Check out the [official ButterCMS Docs](https://buttercms.com/docs/)
- Check out the [official ButterCMS API docs](https://buttercms.com/docs/api/)


# Architecture

The system is organised around Butter and the service that allows clients to book tickets:

![The system architecture that shows that Butter communicates with the ticket-service through webhook. It also exposes that the service has a database and that it communicates with Butter through REST API.](./architecture.svg)

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ticket-service-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.
