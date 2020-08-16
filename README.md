# Java-Service-API

This app is used to start a docker container, in which the java code is compiled and executed.

To build the app run:

```
mvn clean package
```

To run the app go to target directory and run:

```
java -jar java-service-api-0.0.1-SNAPSHOT.jar
```

To change the location where the app saves the generated java files you will have to modify the 
property ```CLASS_DIRECTORY``` from ```Constants``` class.

## How to configure a new jdk version:

First you will have to create a ```Dockerfile``` in the project ```java-runner-config```

To configure a new ```Dockerfile``` in ```java-runner-config``` create a new ```Dockerfiel``` with the following name format:

```jdk_version.Dockerfie```

Example: ```openjdk_10.Dockerfile```

The content of the new file should be: 

```
FROM jdk-image:version

ENV GOTTY_TAG_VER v1.0.1

RUN apt-get -y update && \
   apt-get install -y curl && \
   curl -sLk https://github.com/yudai/gotty/releases/download/${GOTTY_TAG_VER}/gotty_linux_amd64.tar.gz \
   | tar xzC /usr/local/bin && \
   apt-get purge --auto-remove -y curl && \
   apt-get clean && \
   rm -rf /var/lib/apt/lists*

COPY ./gotty_config/gotty /gotty
```

For previous example the following code will be valid:

```
FROM openjdk:10.0.1-10-jdk-slim

ENV GOTTY_TAG_VER v1.0.1

RUN apt-get -y update && \
    apt-get install -y curl && \
    curl -sLk https://github.com/yudai/gotty/releases/download/${GOTTY_TAG_VER}/gotty_linux_amd64.tar.gz \
    | tar xzC /usr/local/bin && \
    apt-get purge --auto-remove -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists*

COPY ./gotty_config/gotty /gotty
```

After the ```Docker``` file have been created you will have to build it:

```docker build -t repository:tag -f Dockerfile_name .```

For previous example the following command is valid:

```docker build -t x-openjdk:10 -f openjdk_10.Dockerfile .```

After you have built the image you will need to create a new ```Dockerfile``` and put it
in the directory ```resources/dockerfile``` from this project. The name of the file can be the same as the
previous file ```jdk_version.Dockerfile```. 

This ```Dockerfile``` will have as a base image the image that has been built at a previous step.

Example of content for the new file:

```
FROM x-openjdk:10

ARG COPY_PATH

COPY ${COPY_PATH} /classes
WORKDIR /classes

CMD ["sh","-c", "javac Main.java && java Main"]
ENTRYPOINT ["/usr/local/bin/gotty","--permit-write", "--once", "--config", "/gotty"]

```
