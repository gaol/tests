# Reproducer for FileResolveImpl Cache issue

When resolving resources from bundle URL, the cache is not used at all.

## Environment to reproduce the issue

### Java version: `11.0.11`

```shell
[🎩 lgao@lins-p1 resolver-cache]$ java -version
java version "11.0.11" 2021-04-20 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.11+9-LTS-194)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.11+9-LTS-194, mixed mode)

```

### OS: `Fedora release 34 (Thirty Four)`
```shell
[🎩 lgao@lins-p1 resolver-cache]$ cat /etc/redhat-release 
Fedora release 34 (Thirty Four)
```

### Vert.x Versions
* 4.2.7
* 4.3.2.SNAPSHOT

## How to reproduce

* Build this reproducer

> mvn clean install

* Start the application by

> java -jar target/resolver-cache-1.0.0-SNAPSHOT-fat.jar

* From another console, send http request

```shell
[🎩 lgao@lins-p1 resolver-cache]$ curl -I "http://localhost:8888?path=java/lang/Object.class"
HTTP/1.1 200 OK
```

* From the cache dir, something like: `/tmp/vertx-cache-af18a48d-7597-4a53-b35f-62260cdacfde/`, you will see the cached files are dumped there:

```shell
[🎩 lgao@lins-p1 resolver-cache]$ ls -l  /tmp/vertx-cache-af18a48d-7597-4a53-b35f-62260cdacfde/java.base/java/lang/
total 4
-rw-rw-r--. 1 lgao lgao 1944 May 31 16:25 Object.class
```

* Now send the http request again

```shell
[🎩 lgao@lins-p1 resolver-cache]$ curl -I "http://localhost:8888?path=java/lang/Object.class"
HTTP/1.1 200 OK
```

If you add a line to [FileCache.cacheFile](https://github.com/eclipse-vertx/vert.x/blob/4.2.7/src/main/java/io/vertx/core/file/impl/FileCache.java#L203):

```java
ignore.printStackTrace();
```

You will see the following exception on each http request except for the first one:

```java
java.nio.file.FileAlreadyExistsException: /tmp/vertx-cache-af18a48d-7597-4a53-b35f-62260cdacfde/java.base/java/lang/Object.class
	at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:94)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:116)
	at java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(UnixFileSystemProvider.java:219)
	at java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(FileSystemProvider.java:478)
	at java.base/java.nio.file.Files.newOutputStream(Files.java:220)
	at java.base/java.nio.file.Files.copy(Files.java:3067)
	at io.vertx.core.file.impl.FileCache.cacheFile(FileCache.java:203)
	at io.vertx.core.file.impl.FileResolverImpl.unpackFromBundleURL(FileResolverImpl.java:358)
	at io.vertx.core.file.impl.FileResolverImpl.unpackUrlResource(FileResolverImpl.java:242)
	at io.vertx.core.file.impl.FileResolverImpl.resolveFile(FileResolverImpl.java:170)
	at io.vertx.core.impl.VertxImpl.resolveFile(VertxImpl.java:770)
	at io.vertx.core.file.impl.FileSystemImpl$16.perform(FileSystemImpl.java:1066)
	at io.vertx.core.file.impl.FileSystemImpl$16.perform(FileSystemImpl.java:1063)
	at io.vertx.core.file.impl.FileSystemImpl$BlockingAction.handle(FileSystemImpl.java:1175)
	at io.vertx.core.file.impl.FileSystemImpl$BlockingAction.handle(FileSystemImpl.java:1157)
	at io.vertx.core.impl.ContextImpl.lambda$executeBlocking$0(ContextImpl.java:159)
	at io.vertx.core.impl.AbstractContext.dispatch(AbstractContext.java:100)
	at io.vertx.core.impl.ContextImpl.lambda$executeBlocking$1(ContextImpl.java:157)
	at io.vertx.core.impl.TaskQueue.run(TaskQueue.java:76)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.base/java.lang.Thread.run(Thread.java:834)

```

This indicates that there are unpacking operations on each subsequence http requests.


## Expected behavior

The cached file should be used, and no more unpack calls on subsequence http requests.

## Analysis

In the [FileResolverImpl.unpackFromBundleURL](https://github.com/eclipse-vertx/vert.x/blob/4.2.7/src/main/java/io/vertx/core/file/impl/FileResolverImpl.java#L350) method, the `String file = url.getHost() + File.separator + url.getFile();` is used as the fileName to cache to the cache directory, but it should be the `fileName` from the `FileResolverImpl.unpackUrlResource` method.

In this case, the request to resolve path of `java/lang/Object.class` reveals the URL: `jrt:/java.base/java/lang/Object.class`. This case is just for the reproducer purpose.

The issue also applies to URL protocol: `vfs`, I believe the same for others as well.

## Thoughts on FileResolverImpl

`FileResolverImpl` encapsulates most of the resource resolving functionalities together with the cache capability and which ClassLoader is used. And [VertxBuilder has the option](https://github.com/eclipse-vertx/vert.x/blob/4.2.7/src/main/java/io/vertx/core/impl/VertxBuilder.java#L181) to specify a different implementation of `FileResolver`. Write a fresh new implementation would be cumbersome to match how the current implementation(`FileResolverImpl`) works.

So it would be great if `FileResolverImpl` can be easy to extend.
