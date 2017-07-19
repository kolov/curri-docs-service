trap 'docker-compose stop' SIGINT

docker-compose run --rm --publish 9000:9000 app sbt $1
