# Akka HTTP - MongoDB microservice 

Microservice managing access to documents in a MongoDB database. This is one of the mocroservices used by
https://github.com/kolov/curriculi.

The implementation is based on akka-http, reactivemongo.
 
## API

This service is not intended to be client-facing, it will be only accessible by an edge service 
providing headers `x-curri-user` and `x-curri-group` based on the user authentication. The following resources are 
availabel:

* `GET /docs` - get all documents
* `GET /docs/{id}` - get a document by id 

All `GET` verbs accept parameter `ields`, indication which fileds to return

* `POST /docs` - save new document
* `PUT /docs/{id}` - update document

#### References

I have used the following resources to get started:

* https://github.com/ArchDev/akka-http-rest
* http://www.lightbend.com/activator/template/akka-http-microservice
* http://www.smartjava.org/content/building-rest-service-scala-akka-http-akka-streams-and-reactive-mongo

For licensing info see LICENSE file in project's root directory.
