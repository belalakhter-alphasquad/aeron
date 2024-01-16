# Tasks defined

## Generate Codecs

Sbe protocol file which contains messages is preset in:
`app/src/main/java/playground/app/sbe`

along with the validation file for messages.xml

in root Project Dir run this command to generate messages

`./gradlew generateCodecs`

Generated messages will be in `app/build/generated/src/main/java/io/aeron/samples/simple`

## Connect cluster

Run the build jar file it will connect to cluster and a new instance will be given by cluster client

## Sending messages

Use the message codecs to define message sending method in send messages file
