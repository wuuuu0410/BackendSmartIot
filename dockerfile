FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/SmartIot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java","-jar","app.jar"]


# WARNING :

# docker network    
# docker default port is 0.0.0.0 , 127.0.0.1 is container itself.
# so if a container connect to 127.0.0.1, it would be like connect to itself.
# we use 0.0.0.0 to accepted request from the outside.

# in the formal situation, docker use bridge vitual network, it let us to use -p to quick start the container
# bridge is behind the firewall, host is connect to web interface directly.



# docker volume
# the container should be immutable and ephemeral.
# according SoC, we should use Volume or Bind Mount to save the data of database.

