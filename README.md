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
_To be defined._ 

<a name="technical-description"></a>
## Technical description
Time Tracker runs on Spring boot version 3.0 and requires Java 17. 
Access to endpoints and api generation, including frontend client, is done by open api.
As a database, Postgres is needed and version 15 is recommended. Project uses liquibase for migrations.

Local database details are explained in the [How to run](#how-to-run) section below.

For object mapping, mapstruct is used with component model parameter being in the pom with the addition of jackson ObjectMapper.

<a name="security"></a>
### Security
Security is provided through spring security context. User can log in using the
`/auth/login` endpoint using their information provided in the registration.
Once user is successfully authenticated, a JWT access token is created for that user. Application settings set properties of the JWT token -
the duration of this token and the secret key. Secret keys are different on each configuration profile to avoid tokens being easily mock-able.

Tokens hold an user context, which holds detailed information about currently logged user, it's permissions and which account is user logged as.

Users are divided into 2 roles - users and admins. Users are regular users and can do limited operations in contexts according to their permissions, whereas admins can log as anyone and do anything to every account.
They also have some specific endpoints to handle, debug and fix some issues among the regular users on production.

<a name="security-filter-chain"></a>
### Security filter chain
Spring filter will decline every request, apart from open api, swagger ui, spring error pages and basic authorization - being registration and login.
Any other endpoint will fail when no or invalid authentication header is present in the request.
If header check doesn't fail, it will proceed to this app's own security filter, which will handle
own logic for the purpose of authorizing user and setting the right security context in spring.

<a name="user-context"></a>
### User context
User context defines everything that is needed about currently logged user, including his permissions. 
Users can log as different user, this is defined by `UserContext.loggedAs` and when user changes context, new token has to be generated for him.
More about this feature in the [Product description](#product-description) section.

Permissions in the context define permissions which has current logged user to the user which he is currently logged as.

Spring boot security context holds user context and can be retrieved at any point. If context is needed in an endpoint,
custom annotation `@InjectUserContext` can be used.

To check users permissions, custom repeatable annotation `@PermissionCheck` is provided and is handled by spring's AspectJ.
Controller endpoint will fail with the response status code of `403: Forbidden` if no user context was found or the user context does not have the
required permissions defined in the value of this annotation.

<a name="how-to-run"></a>
## How to run
For running the application locally, first clone the repository using
```shell
git clone https://github.com/TStrecha/time-tracker.git
```

Run a local [postgres server](https://www.postgresql.org/) is with an user `timetracker` having a password of `timetracker`. By default, host of the postgres server for set to `localhost:5432` in local profile. Change it, if required, but machine specific changes shouldn't be pushed onto git.

To run the application, either run it from IDE configuration, or through maven using:
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
Local test profile is set automatically through abstract integration test class using @BeforeEach. No need to worry about that.

<a name="development-strategy"></a>
## Development strategy
The development strategy defines how the development is done for both backend and frontend applications and how the product is released to the end user. At first the focus should be making the minimum viable product, so that the foundation of the whole product is set and can be build upon.

The step of develop is for this reason divided into releases - these releases define what is the end goal and what the product should look like after this release is done. Release should be defined before it's beginning and shouldn't be changed during the progression.

<a name="release-1"></a>
### Release 1

<a name="release-2"></a>
### Release 2
_Not yet defined._

<a name="contributing"></a>
## Contributing

<a name="code-standards"></a>
### Code standards
Java version 17 introduces a lot of new structures, features and enhancements, so the code should use them as much as it can to make the code as clean as possible using the most recent technology provided:
- Type inference
- Records
- Stream api - including advanced methods
- Improved switch statements

Spring boot perspective:
- Constructor injection instead of single field injection
- Returning response entity in controllers
- Having services implement their interfaces
- Commented code which logs important information using SLF4J
- Mappers without component model
- Avoiding expression in mapping by using named methods

<a name="changing-entity"></a>
### Changing an entity
Since the application uses migration, specifically liquibase, when changing an entity, it's required to also write a liquibase script. That can be done either by creating script manually, or running
```shell
mvn clean install -DskipTests liquibase:generateChangeLog
```

this will automatically generate diff in the `src/main/resources/liquibase` folder. This file has to be checked before getting onto git.
New generated change log's name should start with 4 digit long unique string match this regex:
```regexp
\d{4}-([a-zA-Z-])+
```
Also has to contain author's name and id of each change set has to match the name of the change log file with some unique number suffix, in regex words:
```regexp
\d{4}-([a-zA-Z-])+-\d+
```

<a name="testing"></a>
### Testing
Testing endpoints is possible through IntelliJ Idea http client, although it is not a recommended option. Better way to test endpoints is using [Postman](https://www.postman.com/)

Each new logic should be tested manually through options mentioned above, but also tested using automatic tests - either unit or integration tests to ensure that each and every time something changes, pipeline tests all features and in case something fails, we'll know about it. Automatic tests should test the most edge cases possible and ensure that the code works properly using these tests.

<a name="branch-standards"></a>
### Branch standards
Main branch is a protected branch that has the latest full release on. Develop branch on the other hand should have features until they are fully released and merged into the main branch.

Every change to the code should be in a different branch and should only be merged into develop branch through pull request. Pull requests are always onto develop branch, excluding release pull requests. Pull request has to be reviewed first and accepted before merging.

Naming strategies for branches are:
```text
[feature|bug]/[issue number]-[simple change description]
```

For example:
```text
feature/6-added-readme
```

Make sure to mark a pull request as a draft, if the changes are not complete. Also naming of the pull request should follow naming strategy of branches.

Commits should follow simple rule:
```text
#[issue id] - [commit message]
```

<a name="development-tracking"></a>
### Development tracking
At no point should anyone be working at a change that has no issue behind it. Every change should be part of an issue and every pull request should be linked to this issue. If no issue is provided, create one instead.