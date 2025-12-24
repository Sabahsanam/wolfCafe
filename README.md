# WolfCafe

## Extra Credit
- UI Enhancements
- Security Audit
- Badges
- Github Projects

## Important Links
- [Google Doc with requirements and system test plan](https://docs.google.com/document/d/1M6mDdD9VnNUQyfr8piIxzJASb9yFBv6IGZbskWhhArM/edit?usp=sharing)
- [Developers Guide Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Developers'-Guide)
- [Human Flourishing Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Human-Flourishing)
- [Privacy Policy Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Privacy-Policy)
- [Project Management Plan Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Project-Management-Plan)
- [Github Projects Reflection Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Reflection-on-using-Github-Projects)
- [Requirements Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Requirements)
- [System Test Plan Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/System-Test-Plan)
- [Users' Guide Wiki](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/wiki/Users'-Guide)
- [Security Audit](https://github.com/ncstate-csc-coursework/csc326-2025-fall-tp-204-5/blob/9e583df1520db087835277b162ddcc2cce632220/wolf-cafe-backend/security_audit/SecurityAudit.pdf)

### Badges
![PR Review](https://img.shields.io/badge/reviews-required-blue)
![Line Coverage](https://img.shields.io/badge/line%20coverage-%3E85%25-brightgreen)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)

## Install Lombok
Lombok is a library that lets us use annotations to automatically generate getters, setters, and constructors.  For Lombok to work in Eclipse (and other IDEs like IntelliJ or VS Code), you need to set up Lombok with the IDE in addition to including in the pom.xml file.

Follow the [instructions for setting up Lombok in Eclipse](https://projectlombok.org/setup/eclipse).  Make sure you download the laste version of Lombok from [Maven Repository](https://mvnrepository.com/artifact/org.projectlombok/lombok) as a jar file.

## Configuration

Update `application.properties` in `src/main/resources/` and `src/test/resources/`.

  * Set `spring.datasource.password` to your local MySQL password`
  * Set `app.jwt-secret` as described below.
  * Set `app.admin-user-password` to a plain text string that you will use as the admin password.
  
### Set `app.jwt-secret`

We will create a secret key that will be used for JWT authentication.  Think of a secret key phrase.  You'll want to encrypt it using SHA256 encryption.  You can use a tool like:  https://emn178.github.io/online-tools/sha256.html to generate the encrypted text.  Copy that into your `application.properties` file.

## Setup
The rest of the setup for WolfCafe is the same as for [CoffeeMaker](https://pages.github.ncsu.edu/engr-csc326-staff/326-course-page/onboarding/setup).


## User Roles

User roles are defined and initialized in `config.Roles`.  The `ADMIN` role is a constant.  All other roles are listed in the `UserRoles` enumeration. You can add new roles by adding the role name to the enumeration.

### Initializing the Roles/Admin user in the DB

`config.SetupDataLoader` initializes the DB with roles and creates a default user with the `ADMIN` role.  This class is automatically run when the application starts.  

The admin user has the user name of "admin" and an email address of "admin@admin.edu".  You will specify the password for the admin user in the `application.properties` file.  The password is read in from the `application.properties` and then encrypted using the password encoder.  

## Testing User Authentication in Postman

The following provides examples of how to work with user authentication in Postman.

### Create a New User

Endpoint: `POST http://localhost:8080/api/auth/register`

Body:

```
{
    "name": "Sarah Heckman",
    "username": "sheckman",
    "email": "sheckman@ncsu.edu",
    "password": "sarah"
}
```

Response: 201 Created

```
User registered successfully.
```

### Login with User

Endpoint: `POST http://localhost:8080/api/auth/login`

Body: 

```
{
    "usernameOrEmail": "sheckman",
    "password": "sarah"
}
```

Response: 200 OK

```
{
    "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzaGVja21hbiIsImlhdCI6MTcyOTEyNjg1MiwiZXhwIjoxNzI5NzMxNjUyfQ.WiPROZAMhNbiB8H3fhNJdiC-XX5RJEcHXzmGPEH7aMEFvsjbsvk2m1ZcAKi-lTdt",
    "tokenType": "Bearer",
    "role": "ROLE_CUSTOMER"
}
```

Note the accessToken will vary with each login.  You'll want to save this for testing endpoints that require authentication.

### Get Items

Roles: STAFF, CUSTOMER

Authorization:
  * Bearer
  * Token - copy from the response of an authenticated User
  
Response: 200 OK

```
JSON list of items
```

