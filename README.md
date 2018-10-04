# Java Microservice Quickstart Template
Spring-Boot application preconfigured for to use [EGO](https://github.com/overture-stack/ego/) generated JWTs for authorization.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


## Features
This template provides the following:

* Spring-Boot Application with Spring Security
* JWT Authorization
* JWT Asymmetric Verification - fetches public-key from web on start-up
* JWT Filter - User Role and Status requirements implemented by default
* Docker and Docker-Compose configuration 


## Template Guide
Here is a convenient list of steps to create a new application based on this template:

1. Fork this!
2. Update __pom.xml__:
    - `groupId`
    - `artifactId`
    - `name`
    - `description`
3. Update __application.yml__:
    - `auth.jwt.publicKeyUrl` - URL to fetch the JWT verification key 
4. Configure Codacy:
    - Go to [Codacy Project Wizard](https://www.codacy.com/wizard/projects) and add your new repository.
5. Configure CircleCI:
    - Go to [CircleCI Add Projects](https://circleci.com/add-projects/gh/overture-stack) and add your project.
    - Go to CircleCI project settings and modify environment variables
        - If not there, add new environment variable: `EGO_TEST_SERVER_KEY_URL` . This should store the URL used for `auth.jwt.publicKeyUrl` value in CircleCI tests. 
6. Update __README.md__: 
    - Replace current README with template - __README.template.md__
    - Remove template file
    - Update Project name and description in new README
    - Update Shields in Introduction section
        - Codacy - Badge Markdown code can be found on Codacy project's settings page
        - CircleCI - Build from example using github organization, project, and branch names
    
    


## Requirements
The application can be run locally or in a docker container, the requirements for each setup are listed below.


### EGO
A running instance of [EGO](https://github.com/overture-stack/ego/) is required to generate the Authorization tokens and to provide the verification key.

[EGO](https://github.com/overture-stack/ego/) can be cloned and run locally if a public instance is not setup. 


### Local
* [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/download.cgi)


### Docker
* [Docker](https://www.docker.com/get-docker)


## Quick Start
Make sure the JWT Verification Key URL is configured, then you can run the server in a docker container or on your local machine.


### Configure JWT Verification Key
Update __application.yml__. Set `auth.jwt.publicKeyUrl` to the URL to fetch the JWT verification key. The application will not start if it can't set the verification key for the JWTConverter.

The default value in the __application.yml__ file is set to connect to EGO running locally on its default port `8081`.

### Run Local
```bash
$ mvn spring-boot:run
```

Application will run by default on port `1234`

Configure the port by changing `server.port` in __application.yml__


### Run Docker

First build the image:
```bash
$ docker-compose build
```

When ready, run it:
```bash
$ docker-compose up
```

Application will run by default on port `1234`

Configure the port by changing `services.api.ports` in __docker-compose.yml__. Port 1234 was used by default so the value is easy to identify and change in the configuration file.

### Test Endpoint
The application has a single endpoint `/test` that will accept GET and POST requests with a valid token.

A JWT must be passed in a request header, following the Bearer token pattern. Below is a usable value to test with, it is valid vs. the example keystore given in the EGO repo.
 
 ```
 Authorization=Bearer eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE1MTI3NjIxODIsImV4cCI6MjE0NzQ4MzY0Nywic3ViIjoiNjA2IiwiaXNzIjoiZWdvIiwiYXVkIjpbXSwiY29udGV4dCI6eyJ1c2VyIjp7Im5hbWUiOiJEZW1vLlVzZXJAZXhhbXBsZS5jb20iLCJlbWFpbCI6IkRlbW8uVXNlckBleGFtcGxlLmNvbSIsInN0YXR1cyI6IkFwcHJvdmVkIiwiZmlyc3ROYW1lIjoiRGVtbyIsImxhc3ROYW1lIjoiVXNlciIsImNyZWF0ZWRBdCI6IjIwMTctMTEtMjIgMDM6MTA6NTUiLCJsYXN0TG9naW4iOiIyMDE3LTEyLTA4IDA3OjQzOjAyIiwicHJlZmVycmVkTGFuZ3VhZ2UiOm51bGwsInJvbGVzIjpbIlVTRVIiXX19LCJqdGkiOiI0OGE5NGIzNy1mMTJlLTQxNWQtYjM1Zi1kZDhmOThiMDQ4ZDcifQ.Cmgbd_xnUp8dPnIJvmUXmh5LYnHgHSk_n_0VzCn0k9r4WVNdsupb-MQqJvgOMg3K8si5mzhIjzLi9rZL5N_JwFXtpjKXKRVT7KF4mYfqF7bVNm6tkQg6CeAGhiuaMujhLhASS79LVBPKOv1tk79WuVu-VKHzyLS1h3yFQAsjLVQxA6_0MD7zKa1W3Nbhte6lHwgiNo1AlxuIJzP37-2saNb-aUy9DigmH3_C2oPqxpBu-YNnaekO5jNmbfucMinlpxCpEw-UvpvxI9Xk_9E73TNQE9acNQyyg_BxdnVbwDsR-kG5QXNrlEAxGm-1yY6w8Nvqxcp-3uoff6K0uKLUdQ
 ```
 
 Test cURL requests:
 ```bash
curl -X GET \
  http://localhost:1234/test \
  -H 'authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE1MTI3NjIxODIsImV4cCI6MjE0NzQ4MzY0Nywic3ViIjoiNjA2IiwiaXNzIjoiZWdvIiwiYXVkIjpbXSwiY29udGV4dCI6eyJ1c2VyIjp7Im5hbWUiOiJEZW1vLlVzZXJAZXhhbXBsZS5jb20iLCJlbWFpbCI6IkRlbW8uVXNlckBleGFtcGxlLmNvbSIsInN0YXR1cyI6IkFwcHJvdmVkIiwiZmlyc3ROYW1lIjoiRGVtbyIsImxhc3ROYW1lIjoiVXNlciIsImNyZWF0ZWRBdCI6IjIwMTctMTEtMjIgMDM6MTA6NTUiLCJsYXN0TG9naW4iOiIyMDE3LTEyLTA4IDA3OjQzOjAyIiwicHJlZmVycmVkTGFuZ3VhZ2UiOm51bGwsInJvbGVzIjpbIlVTRVIiXX19LCJqdGkiOiI0OGE5NGIzNy1mMTJlLTQxNWQtYjM1Zi1kZDhmOThiMDQ4ZDcifQ.Cmgbd_xnUp8dPnIJvmUXmh5LYnHgHSk_n_0VzCn0k9r4WVNdsupb-MQqJvgOMg3K8si5mzhIjzLi9rZL5N_JwFXtpjKXKRVT7KF4mYfqF7bVNm6tkQg6CeAGhiuaMujhLhASS79LVBPKOv1tk79WuVu-VKHzyLS1h3yFQAsjLVQxA6_0MD7zKa1W3Nbhte6lHwgiNo1AlxuIJzP37-2saNb-aUy9DigmH3_C2oPqxpBu-YNnaekO5jNmbfucMinlpxCpEw-UvpvxI9Xk_9E73TNQE9acNQyyg_BxdnVbwDsR-kG5QXNrlEAxGm-1yY6w8Nvqxcp-3uoff6K0uKLUdQ'
```

```bash
curl -X POST \
  http://localhost:1234/test \
  -H 'authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE1MTI3NjIxODIsImV4cCI6MjE0NzQ4MzY0Nywic3ViIjoiNjA2IiwiaXNzIjoiZWdvIiwiYXVkIjpbXSwiY29udGV4dCI6eyJ1c2VyIjp7Im5hbWUiOiJEZW1vLlVzZXJAZXhhbXBsZS5jb20iLCJlbWFpbCI6IkRlbW8uVXNlckBleGFtcGxlLmNvbSIsInN0YXR1cyI6IkFwcHJvdmVkIiwiZmlyc3ROYW1lIjoiRGVtbyIsImxhc3ROYW1lIjoiVXNlciIsImNyZWF0ZWRBdCI6IjIwMTctMTEtMjIgMDM6MTA6NTUiLCJsYXN0TG9naW4iOiIyMDE3LTEyLTA4IDA3OjQzOjAyIiwicHJlZmVycmVkTGFuZ3VhZ2UiOm51bGwsInJvbGVzIjpbIlVTRVIiXX19LCJqdGkiOiI0OGE5NGIzNy1mMTJlLTQxNWQtYjM1Zi1kZDhmOThiMDQ4ZDcifQ.Cmgbd_xnUp8dPnIJvmUXmh5LYnHgHSk_n_0VzCn0k9r4WVNdsupb-MQqJvgOMg3K8si5mzhIjzLi9rZL5N_JwFXtpjKXKRVT7KF4mYfqF7bVNm6tkQg6CeAGhiuaMujhLhASS79LVBPKOv1tk79WuVu-VKHzyLS1h3yFQAsjLVQxA6_0MD7zKa1W3Nbhte6lHwgiNo1AlxuIJzP37-2saNb-aUy9DigmH3_C2oPqxpBu-YNnaekO5jNmbfucMinlpxCpEw-UvpvxI9Xk_9E73TNQE9acNQyyg_BxdnVbwDsR-kG5QXNrlEAxGm-1yY6w8Nvqxcp-3uoff6K0uKLUdQ'
```

If everything is working as expected, the request should return a pleasant greeting. ;)
