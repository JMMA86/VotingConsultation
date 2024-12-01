# VotingConsultation

## Colaborators

- Cristian Eduardo Botina (A00395008)
- Juan Manuel Marín (A00382037)
- Óscar Andrés Gómez (A00394142)

## Content

This is an implementation of a mock voting system, using Gradle and Zero Ice.

## Ice Grid Compilation

1. Run the IceGrid Registry and Nodes:
    - `icegridregistry --Ice.Config=config/registry.config`
    - For each node X: `icegridnode --Ice.Config=config/NodeX.config`
2. Enter to the IceGrid Admin: `icegridadmin`, select the locator if necessary (note the endpoint port)
    - You can use any username and an empty password
    - Enter the command `application add application.xml`. If it already exists, then run `application update application.xml` 
    - Additionally, you can see the servers using `server list` and the server status `server describe <name>`
3. Execute the clients jar as usual `java -jar client/build/libs/client.jar`