FROM kolov/java8:latest
ENTRYPOINT ["scripts/docs-service-app"]

USER docker
USER root

WORKDIR /home/docker/app
ADD target/universal/curri-*.tgz /home/docker/app/
RUN ["chown", "-R", "docker:docker", "."]

USER docker

EXPOSE 9000
