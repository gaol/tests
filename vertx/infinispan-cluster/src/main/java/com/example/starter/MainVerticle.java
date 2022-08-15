package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  private static final String ADDRESS = "inbox";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    boolean startHttp = Boolean.getBoolean("http");
    if (startHttp) {
      System.out.println("\n======== START HTTP ==========\n");
      vertx.createHttpServer()
        .requestHandler(req -> vertx.eventBus().<String>request(ADDRESS, "Hello from Vertx. !").onComplete(r -> {
          String msg = "";
          if (r.failed()) {
            msg = r.cause().getMessage();
          } else {
            msg = r.result().body();
          }
          req.response()
            .putHeader("content-type", "text/plain")
            .end(msg + "\n");
        }))
        .listen(8888, http -> {
          if (http.succeeded()) {
            startPromise.complete();
            System.out.println("HTTP server started on port 8888");
          } else {
            startPromise.fail(http.cause());
          }
        });
    } else {
      System.out.println("\n======== SETUP CONSUMER ==========\n");
      vertx.eventBus()
        .<String>consumer(ADDRESS)
        .handler(m -> m.reply("Got your message: " + m.body()))
        .completionHandler(startPromise);
    }

  }
}
