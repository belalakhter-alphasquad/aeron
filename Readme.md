# Tasks defined

## Generate Codecs

Sbe protocol file which contains messages is preset in:
`app/src/main/java/playground/app/sbe`

along with the validation file for messages.xml

in root Project Dir run this command to generate messages

`./gradlew generateCodecs`

Generated messages will be in `app/build/generated/src/main/java/io/aeron/samples/simple`
