# Reproducer for jboss-vfs resolver

## How to reproduce

### Start a WildFly Server

Download a WildFly server and start it, like:

> wget -O wildfly.zip https://github.com/wildfly/wildfly/releases/download/26.0.0.Final/wildfly-26.0.0.Final.zip
> unzip -q wildfly.zip
> wildfly-26.0.0.Final/bin/standalone.sh

### Build and deploy the reproducer

In another terminal, run the followings:

> git clone https://github.com/gaol/tests
> cd tests/vfs-resolver
> mvn clean package wildfly:deploy

### Access the http end point

> curl http://localhost:8080/helloworld/helloworld


## Expected behavior

Expected response is:
```
 == config.properties  == 

year = 2022

 == geo.json in geo/  == 

{
    "location": "BeiJing"
}

 == geo.json in geo/sh/  == 

{
    "location": "ShangHai"
}

```

## Actual behavior

Actual response is:
```
<html><head><title>Error</title></head><body>Invalid url protocol: vfs</body></html>
```

NOTE:

> This demostrates the failure of accessing resources bundled in the war deployment.
> This implies that some Vertx APIs based on file systems can not be used in the applications if they are deployed to wildfly server.
> like the [PropertyFileAuthentication](https://vertx.io/docs/vertx-auth-properties/java/) which may need to read authentication properties from the bundle.
