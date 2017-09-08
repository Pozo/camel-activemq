## What is it?
An example fail safe point-to-point message delivery setup. It uses Apache Camel and Apache ActiveMQ.

Send messages to a remote destination without worrying about the connection state between the client and server. If there is no active connection, the client will persist the messages as long as it manage to create a connection.

If you're looking for a production ready error tracking solution try out [Sentry](sentry.io "Sentry")
## Installation
    git clone git@gitlab.com:Pozo/tracking-client-remotely.git && cd tracking-client-remotely
    mvn clean install
## Start server
    cd tracking-server
    mvn compile exec:java
## Start client
    cd tracking-client
    mvn compile exec:java
### Schematic workflow
```
                                                                      +------------+
                                                                  +---+ try again  |
                                                                  |   |            |
                                                                  |   +-----+------+
                                                                  |         ^
                                                                  v         | NO
                                         +---------------+      +-----------+-----------+
                                         | local message |      |  reconection attempt  |
                                         |    store      +----> |  was successful ?     |
                                         |               |      |                       |
                                         +-------+-------+      +-----------+-----------+
                                                 ^                          | YES
                                                 | NO                       v
                 +---------------+       +-------+-------+      +-----------------------+
                 |               |       | remote broker | YES  |                       |
message  +-----> |  vm:localhost | +---> | is available ?+----> |    deliver messages   |
                 |               |       |               |      |                       |
                 +---------------+       +---------------+      +-----------------------+

```
## Banana for scale

![alt text](https://www.dailydot.com/wp-content/uploads/c23/15/39ebf638f97319f404e3fd2faa6101cd.jpg "Logo Title Text 1")
## Licensing

Please see the file called LICENSE.

## Contact

  Zoltan Polgar - pozo@gmx.com
  
  Please do not hesitate to contact me if you have any further questions. 