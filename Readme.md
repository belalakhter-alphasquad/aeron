# Simple Barebone aeron cluster client

Use command `./gradlew client`

This will do following:

- Use SBE All package to generate codecs which are in sbe folder in `app/`
- Build the jar file app-1.0.0.jar in `app/build/libs` which use main class in app as entry point
- Connect to cluster and send a message to it and listen to response from cluster

This client is aligned with configuration for endpoints and protocol Codecs in:

https://github.com/alphaTaha/tm-playground/tree/main/aeron-echo-broadcast-cluster
