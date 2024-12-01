#!/bin/bash

mkdir -p deploy/node/db/node deploy/registry/db/registry deploy/client
mkdir -p deploy/node/logs deploy/registry/logs deploy/client/logs

gradle build

cp server/build/libs/server.jar deploy/node
cp client/build/libs/client.jar deploy/client
cp .env deploy/node


# ssh swarch@xhgrid3 "rm -r returners"
# ssh swarch@xhgrid4 "rm -r returners"
# ssh swarch@xhgrid7 "rm -r returners"
# ssh swarch@xhgrid6 "rm -r returners"

# cp config/registry.config deploy/registry
# cp config/Node1.config deploy/node/node.config
# cp application.xml deploy/registry

# scp -r deploy/registry swarch@xhgrid3:returners
# scp -r deploy/node swarch@xhgrid4:returners
# scp -r deploy/client swarch@xhgrid6:returners

# icegridregistry --Ice.Config=registry.config
# icegridnode --Ice.Config=node.config
