# Simple Barebone implementation of Order placement from API to Aeron client and Aeron Cluster Service

## Commands

Use command `./gradlew build`

Run the jar file in `build/libs`

This will do following:

- Run cluster service right now single node
- Cluster client instance with getter
- Gateway with one api handle right now

## API Endpoints

- localhost:3000/placeOrder
  {
  "OrderId": 123456,
  "Symbol": "BTC",
  "Quantity": 10
  }

## Note

- Some features not completely handled like snapshoting and pending messages feature
- Due to all services run in same machine there is no proper mechanisim for multi threading implemented

#### This is only free time practice playground to understand things better
