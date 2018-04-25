<h1 align="center"> Kids First ETL Task Runner </h1> <br>

<p align="center">
  Microservice to execute Dockerized Tasks as requested by the Kids First Release Coordinator
</p>


## Table of Contents

- [Introduction](#introduction)
- [Process](#process)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Testing](#testing)
- [API](#requirements)
- [Acknowledgements](#acknowledgements)




## Introduction

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e91606af4a364076a7058c5ea1c006a8)](https://www.codacy.com/app/joneubank/kf-portal-etl-coordinator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=overture-stack/microservice-template-java&amp;utm_campaign=Badge_Grade)

This application will initiate tasks as requested by the Kids First Release Coordinator. Tasks are defined processes run in docker containers, created when the task is started. This allows multiple tasks to be run simultaneously in distinct containers.

Specifically, this will be used to execute the Kids First ETL tasks. However, this is built with a configuration model that makes it easy to adopt any other dockerized task that the release controller wants to manage.

Authorization for requests made to the Task Runner is performed via JWTs from an [EGO](https://github.com/overture-stack/ego) server. A valid token with permissions for a recognized Task Coordinator application is required for any action to be taken on any request to this ETL Task Runner.

## Process
Each task is moved through the following state-transitions:

![State Transition Diagram for Tasks](state-diagram.png "State Transition Diagram for Tasks")

The task runner provides 2 services that can be called by the Release Coordinator: /status and /tasks

* Status - General health and version status of the ETL service. 
 Initialize, Status, Run, Publish. These steps match the requests outlined in the [Kids First Task Coordinator](https://github.com/kids-first/kf-api-release-coordinator#sequence-of-operations-success-case) documentation.

1. Initialize - This will check if the task runner is able to run a new task. Any checks that are needed ahead of starting a task are performed here. For any ETL tasks, this will include checking permissions to access the studies that will be requested in the ETL feed. On success this will return a unique task code to be used for this task.

2. Status - Given a recognized task ID, the task runner will return the current state of the given task. See the State Transition Diagram for the possible statuses.

3. Run - Start the task running and stage the results. Given ID for a task currently in PENDING status from the Intialize step, and any required variables for the task, a docker container will be created and the task run.

4. Publish - Given a task ID that is currently in STAGED status after completing the Run step, this will publish the results.

## Requirements
The application can be run locally or in a docker container, the requirements for each setup are listed below.


### EGO
A running instance of [EGO](https://github.com/overture-stack/ego/) is required to generate the Authorization tokens and to provide the verification key.

[EGO](https://github.com/overture-stack/ego/) can be cloned and run locally if no public server is available. 


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


## Testing
TODO: Additional instructions for testing the application.


## API


### Initiate Service


## Acknowledgements

Services provided to accomplish the task coordination process as defined in the [Kids First Task Coordinator](https://github.com/kids-first/kf-api-release-coordinator)

JWT Authentication model provided by [Overture](https://github.com/overture-stack)'s [Ego](https://github.com/overture-stack/ego) service. This microservice is built on a fork of the [Ego Microservice Template for Java](https://github.com/overture-stack/microservice-template-java)
