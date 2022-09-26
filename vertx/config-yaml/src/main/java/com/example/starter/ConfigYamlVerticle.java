/*
 *  Copyright (c) 2022 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of Apache License v2.0 which
 *  accompanies this distribution.
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.example.starter;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;

/**
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
public class ConfigYamlVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("yaml")
      .setConfig(new JsonObject()
        .put("path", "config.yaml")
      );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
    retriever.getConfig().onComplete(js -> {
      if (js.succeeded()) {
        String value = js.result().getString("configKey");
        Assert.assertEquals("configValue", value);
        System.out.println("All GOOD");
      } else {
        js.cause().printStackTrace();
      }
      retriever.close();
    });
  }

}
