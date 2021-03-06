# Akka HTTP - MongoDB microservice     [![Build Status](https://travis-ci.org/kolov/curri-docs-service.svg?branch=master)](https://travis-ci.org/kolov/curri-docs-service)

A service managing access to documents in a MongoDB database. This is one of the microservices used by
the curriculi project.

The implementation is based on akka-http and reactivemongo.
 
## API

This service is not intended to be client-facing, it will be only accessible by an edge service 
providing headers `x-curri-user` and `x-curri-group` based on the user authentication. The following resources are 
available:

* `GET /docs` - get all documents visible by user/group
* `GET /docs/{id}` - get a document by id, must be visible 

All `GET` verbs returning document accept parameter `fields`, indication which fileds to return

* `POST /docs` - save new document from body, returns id
* `PUT /docs/{id}` - update document

## References

This is one of my first Scala projects, I used these resources to get started:

* https://github.com/ArchDev/akka-http-rest
* http://www.lightbend.com/activator/template/akka-http-microservice
* http://www.smartjava.org/content/building-rest-service-scala-akka-http-akka-streams-and-reactive-mongo

After thet removed Play and addusedd circe for json.

For licensing info see LICENSE file in project's root directory.
