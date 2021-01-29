# Chat app using akka http

#### Chat app which can send and receive messages to and from registered user. Actor is used to send messages using POST and to get messages using GET request.
##### Users can have one to one chats ans well as group chat and get all the names of group user has been added to.

**Scala version : 2.12.2 <br>
Sbt version : 1.3.8**

`Steps to run project:` <br>
`1) sbt run `

`Steps to run test files:` <br>
`1) sbt test `

To generate coverage report:
` sbt coverageReport`

###Dependencies used:
1) Akka actors - 2.5.20
2) Akka testkit - 2.5.20
3) Akka stream - 2.5.20
4) Akka http - 10.1.7
5) Akka http-testkit - 10.1.7
6) playjson - 2.6.7
7) akka-http-play-json - 1.20.0
8) mongo-scala driver - 2.7.0
9) courier - 3.0.0-M2
