# Reproducer

This is the reproducer for the `Transfer-Encoding: gzip, chunked` issue in Netty.

## How to reproduce

### Start the http server

Run the following command to start the HTTP Server:

> mvn clean package exec:java

### Sent http request

```
tar -czvf data.gz README.md
curl http://localhost:8080 -H "Transfer-Encoding: gzip, chunked" -d @./data.gz -X POST -v
```

## Expected behavior

client can get the response: `I hope you can get my response!`

## Actual behavior

client cannot get the response, and there is exception in the server

## Linked Issue

https://github.com/eclipse-vertx/vert.x/issues/3353

