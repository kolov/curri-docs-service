FROM kolov/java8:latest
ENTRYPOINT ["curri-akka-1.0/bin/docs-service-app"]

#RUN useradd docker
#USER root

WORKDIR /home/docker/app
ADD target/universal/curri-*.tgz /home/docker/app/
#RUN ["chown", "-R", "docker", "."]

#USER docker

EXPOSE 9000
