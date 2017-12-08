<h1 align="center"> Project Title </h1> <br>

<p align="center">
  Sample microservice description.
</p>


## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Quick Start](#quickstart)




## Introduction

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

TODO: Replace with introduction

## Features
TODO: Description of features

* Include a list of
* all the many beautiful
* web server features


## Requirements
The following software is required to run the application. It can be run locally or in a docker container.

### EGO
A running instance of [EGO](https://github.com/overture-stack/ego/) is required to generate the Authorization tokens and to provide the verification key.

[EGO](https://github.com/overture-stack/ego/) can be cloned and run locally if no public server is available. 


### Local
* [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/download.cgi)


### Docker
* [Docker](https://www.docker.com/get-docker)


## Quick Start
Make sure the JWT Verification Key is configured, then you can run the server in a docker container or on your local machine.

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


## Test
TODO: Additional instructions for testing the application.


## API
TODO: API Reference with examples, or a link to a wiki or other documentation source.

## Acknowledgments
TODO: Show folks some love.