FROM kolov/java8:latest
ENTRYPOINT ["curri-docs-1.0/bin/curri-docs"]

RUN mkdir /app
ADD target/universal/curri-docs-*.tgz /app
WORKDIR /app


EXPOSE 9000
