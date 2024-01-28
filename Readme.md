# Simple Barebone implementation of Order placement from API to Aeron client and Aeron Cluster Service

## Commands

Use command `./gradlew run`

Read the Terminal Logs can quickly give idea of this app.

This will do following:

- Generate Codec against messages in sbe Dir
- Run a single node cluster service and cluster client instance
- Http server gateway that pass Api request to aeron client and passes to cluster service

## Note

- Some features not completely handled like snapshoting and pending messages feature
- Due to all services run in same machine there is no proper mechanisim for multi threading implemented

#### This is only free time practice playground to understand things better
