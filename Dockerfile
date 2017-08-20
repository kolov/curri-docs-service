FROM kolov/java8:latest
ENTRYPOINT ["curri-akka-1.0/bin/curri-akka"]

RUN mkdir /app
WORKDIR /app
ADD target/universal/curri-*.tgz /app


EXPOSE 9000
