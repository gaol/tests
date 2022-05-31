package io.github.gaol.vertx.tests;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;

public class MainVerticle extends AbstractVerticle {

    static String getHost() {
        return System.getProperty("vertx.http.host", "localhost");
    }

    static int getHttpPort() {
        return Integer.getInteger("vertx.http.port", 8888);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.createHttpServer().requestHandler(req -> {
            String resPath = req.getParam("path");
            if (resPath == null) {
                resPath = "file.json";
            }
            System.out.println("Read resource from: " + resPath);
            HttpServerResponse response = req.response();
            vertx.fileSystem().readFile(resPath).onComplete(result -> {
                    if (result.succeeded()) {
                        response.setStatusCode(200).end(result.result());
                    } else {
                        response.setStatusCode(500).end(result.cause().getMessage());
                    }
            });
        })
        .listen(getHttpPort(), getHost())
        .flatMap(server -> {
            System.out.println(getClass() + ":  Http Server is listening on: " + server.actualPort());
            return Future.succeededFuture((Void) null);
        })
        .onComplete(startPromise);
    }
}
