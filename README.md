Twipser
=====================================

This is Twipser, a simple twitter clone created as a demo application for a play framework meetup at [IPSWAYS](http://www.ipsways.com/).
This application was inspired by the talk "[All Work and No Play Doesn't Scale](http://parleys.com/play/51c38b03e4b0ed877035686c/)" from [James Roper](https://github.com/jroper/all-work-no-play-doesnt-scale). 

In order to run the application you need to have a MongoDB up and running.

### API

##### Get all messages
```bash
curl --header "Content-type: application/json" --request GET http://localhost:9000/api/messages
```

##### Create new message
```bash
curl --header "Content-type: application/json" --request POST --data 'author": "Jan", "message": "CURL TEST"}' http://localhost:9000/api/message
```

### Resources
* [Play Framework](http://www.playframework.com/)
* [Play Framework documentation](http://www.playframework.com/documentation/2.2.x/Home)
* [Play Framework Usergroup](https://groups.google.com/forum/#!forum/play-framework)
* [Reactive Mongo](http://reactivemongo.org/)
* [Reactive Mongo Usergroup](https://groups.google.com/forum/#!forum/reactivemongo)
* [MongoDB](http://www.mongodb.org/)
* [Twitter bootstrap](http://getbootstrap.com/)
* [WebJars](http://www.webjars.org/)
* [Specs2](http://etorreborre.github.io/specs2/)
* [ScalaTest](http://www.scalatest.org/)
* [Akka](http://akka.io/)
* [Scala](http://www.scala-lang.org/)
* [Typesafe](http://typesafe.com/)
* [Typesafe Console](http://typesafe.com/platform/runtime/console)