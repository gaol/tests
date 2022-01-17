/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.helloworld;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Servlet used to use Vertx filesystem API to access files in the deployed archive.
 *
 */
@SuppressWarnings("serial")
@WebServlet(value = "/helloworld", asyncSupported = true)
public class HelloWorldServlet extends HttpServlet {

    private Vertx vertx;

    @Override
    public void init() throws ServletException {
        vertx = Vertx.vertx();
    }

    @Override
    public void destroy() {
        vertx.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final AsyncContext asyncContext = req.startAsync();
        final StringBuilder sb = new StringBuilder();

        vertx.fileSystem().readFile("config.properties").flatMap(cf -> {
            sb.append("\n == config.properties  == \n");
            sb.append("\n" + cf.toString() +  "\n");
            return vertx.fileSystem().readFile("geo/geo.json");
        }).flatMap(geobj -> {
            sb.append("\n == geo.json in geo/  == \n");
            sb.append("\n" + geobj.toString() + "\n");
            return vertx.fileSystem().readFile("geo/sh/geo.json");
        }).flatMap(geosh -> {
            sb.append("\n == geo.json in geo/sh/  == \n");
            sb.append("\n" + geosh.toString() + "\n");
            return Future.succeededFuture(sb.toString());
        }).onComplete(result -> {
            try {
                if (result.succeeded()) {
                    PrintWriter writer = asyncContext.getResponse().getWriter();
                    writer.println(result.result());
                } else {
                    result.cause().printStackTrace();
                    resp.sendError(500, result.cause().getMessage());
                }
                asyncContext.complete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
