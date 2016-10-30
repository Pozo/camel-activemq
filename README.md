## What is it?
A fail safe point-to-point message delivery solution. It uses Apache Camel and Apache ActiveMQ.

Send messages to a remote destination without worrying about the connection state between the client and server. If there is no active connection, the client will persist the messages as long as it manage to create a connection.
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

![alt text](http://cdn0.dailydot.com/cache/f9/50/f950e4c4ffb624d260ec08f30d7266bd.jpg "Logo Title Text 1")
## Licensing

Please see the file called LICENSE.

## Contact

  Zoltan Polgar - pozo@gmx.com
  
  Please do not hesitate to contact me if you have any further questions. 