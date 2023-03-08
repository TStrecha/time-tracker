# Time Tracker
#### Table of contents
- [Product description](#product-description)
- [Technical description](#technical-description)
  - [Security](#security)
  - [Security filter chain](#security-filter-chain)
  - [User context](#user-context)
- [How to run](#how-to-run)
- [Development strategy](#development-strategy)
  - [Release 1](#release-1)
  - [Release 2](#release-2)
- [Contributing](#contributing)
  - [Code standards](#code-standards)
  - [Changing an entity](#changing-an-entity)
  - [Testing](#testing)
  - [Branch standards](#branch-standards)
  - [Development tracking](#development-tracking)

<a name="product-description"></a>
## Product description
This product focuses on tracking work time. Users can register and log in.
After confirming their email address they can create tasks that represent other applications' tasks. They can assign IDs from other applications
to make finding the task easy. They can describe them, set estimates, and track their status.

Once a user has some tasks, they can create a time report for this task. They can change what date is the report for, and add notes and comments. Users are also allowed to add an authorized user and edit their permissions. Once the authorized user is added, they can do limited actions using their account and selecting the user to which they have access.

For every module, there is a history provided, which allows users to see what and when has something changed and who made the change.

A notification and dashboard center allows users to see crucial information, and reports for the day, month estimate, and year overview. Users can set what they see in this dashboard.

Every month and every year an overall report is generated for every user telling them how they went, what did they work on, and how much time they spent working. This report is generated into PDF and Excel to make sharing this report possible.

<a name="technical-description"></a>
## Technical description
Time Tracker runs on Spring boot version 3.0 and requires Java 17.
Access to endpoints and API generation, including frontend client, is done by open API.
As a database, Postgres is needed and version 15 is recommended. Project uses liquibase for migrations.

Local database details are explained in the [How to run](#how-to-run) section below.

For object mapping, mapstruct is used with the component model parameter being in the pom with the addition of Jackson ObjectMapper.

<a name="security"></a>
### Security
Security is provided through the spring security context. Users can log in using the
`/auth/login` endpoint using the information provided in the registration.
Once a user is successfully authenticated, a JWT access token is created for that user. Application settings set properties of the JWT token -
the duration of this token and the secret key. Secret keys are different on each configuration profile to avoid tokens being easily mockable.

Tokens hold a user context, which holds detailed information about the currently logged user, its permissions, and which account is user logged as.

Users are divided into 2 roles - users and admins. Users are regular users and can do limited operations in contexts according to their permissions, whereas admins can log in as anyone and do anything to every account.
They also have some specific endpoints to handle, debug and fix some issues among the regular users on production.

<a name="security-filter-chain"></a>
### Security filter chain
Spring filter will decline every request, apart from open API, swagger ui, spring error pages, basic authorization - registration, and log-in.
Any other endpoint will fail when no or invalid authentication header is present in the request.
If the header check doesn't fail, it will proceed to this app's security filter, which will handle
its logic to authorize the user and set the right security context in spring.

<a name="user-context"></a>
### User context
User context defines everything that is needed about the currently logged user, including his permissions.
Users can log in as a different user, this is defined by `UserContext.loggedAs` and when the user changes context, a new token has to be generated for him.
More about this feature in the [Product description](#product-description) section.

Permissions in the context define permissions that have the currently logged user to the user who he is currently logged as.

Spring boot security context holds user context and can be retrieved at any point. If the context is needed in an endpoint,
custom annotation `@InjectUserContext` can be used.

To check users' permissions, the custom repeatable annotation `@PermissionCheck` is provided and is handled by spring's AspectJ.
The controller endpoint will fail with the response status code of `403: Forbidden` if no user context was found or the user context does not have the
required permissions defined in the value of this annotation.

<a name="how-to-run"></a>
## How to run
For running the application locally, first clone the repository using
```shell
git clone https://github.com/TStrecha/time-tracker.git
```

Run a local [postgres server](https://www.postgresql.org/) with a user `timetracker` having a password of `timetracker`. By default, the host of the Postgres server is set to `localhost:5432` in the local profile. Change it, if required, but machine-specific changes shouldn't be pushed onto git.

To run the application, either run it from IDE configuration or through maven using:
```shell
mvn springboot:run -Dspring.profiles.active=local
```

Or when testing the application, for unit tests run:
```shell
mvn test -Dtest=!*IT
```
And for integration tests run:
```shell
mvn test -Dtest=*IT
```
The local test profile is set automatically through the abstract integration test class using @BeforeEach. No need to worry about that.

<a name="development-strategy"></a>
## Development Strategy
The development strategy defines how the development is done for both backend and frontend applications and how the product is released to the end user. At first, the focus should be on making the minimum viable product, so that the foundation of the whole product is set and can be built upon.

The step of development is for this reason divided into releases - these releases define what is the end goal and what the product should look like after this release is done. A release should be defined before its beginning and shouldn't be changed during the progression.

<a name="contributing"></a>
## Contributing

<a name="code-standards"></a>
### Code standards
Java version 17 introduces a lot of new structures, features, and enhancements, so the code should use them as much as it can to make the code as clean as possible using the most recent technology provided:
- Type inference
- Records
- Stream API - including advanced methods
- Improved switch statements

Spring boot perspective:
- Constructor injection instead of single-field injection
- Returning response entity in controllers
- Having services implement their interfaces
- Commented code that logs important information using SLF4J
- Mappers without component model
- Avoiding expression in mapping by using named methods

<a name="changing-entity"></a>
### Changing an entity
Since the application uses migration, specifically liquibase, when changing an entity, it's required to also write a liquibase script. That can be done either by creating a script manually or running
```shell
mvn clean install -DskipTests liquibase:generateChangeLog
```

this will automatically generate a diff in the `src/main/resources/liquibase` folder. This file has to be checked before getting onto git.
New generated change log's name should start with 4 digits long unique string that matches this regex:
```regexp
\d{4}-([a-zA-Z-])+
```
Also has to contain the author's name and id of each change set and has to match the name of the change log file with some unique number suffix, in regex words:
```regexp
\d{4}-([a-zA-Z-])+-\d+
```

<a name="testing"></a>
### Testing
Testing endpoints is possible through IntelliJ Idea http client, although it is not a recommended option. A better way to test endpoints is using [Postman](https://www.postman.com/)

Each new logic should be tested manually through the options mentioned above, but also tested using automatic tests - either unit or integration tests to ensure that every time something changes, the pipeline tests all features, and in case something fails, we'll know about it. Automatic tests should test the most edge cases possible and ensure that the code works properly using these tests.

<a name="branch-standards"></a>
### Branch standards
The main branch is a protected branch that has the latest full release. Develop branch on the other hand should have features until they are fully released and merged into the main branch.

Every change to the code should be in a different branch and should only be merged into develop branch through a pull request. Pull requests are always onto develop branch, excluding release pull requests. Pull request has to be reviewed first and accepted before merging.

A naming strategy for a new branch is:
```text
[feature|bug]/[issue number]-[simple change description]
```

For example:
```text
feature/6-added-readme
```

Make sure to mark a pull request as a draft, if the changes are not complete. Also naming of the pull request should follow a naming strategy of branches.

Commits should follow a simple rule:
```text
#[issue id] - [commit message]
```

<a name="development-tracking"></a>
### Development tracking
At no point should anyone be working on a change that has no issue behind it. Every change should be part of an issue and every pull request should be linked to this issue. If no issue is provided, create one instead.
