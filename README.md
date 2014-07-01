[![Build Status](https://travis-ci.org/pschirmacher/webster.svg?branch=master)](https://travis-ci.org/pschirmacher/webster)

Webster
=======

[Webmachine](https://github.com/basho/webmachine/wiki)-inspired HTTP for the JVM.

Please be aware that this is just a proof-of-concept implementation. It's not ready for being used anywhere near
a production system.

Try out the [example](https://github.com/pschirmacher/webster-example)!

Rationale
=========

Webster is a library to build HTTP servers using Java 8. It implements key HTTP features like content negotation and
conditional requests so you don't have to. The goal is to help developers build applications that adhere to the HTTP
spec without having to reimplement the same things over and over again. It also tries to keep things simple by using
plain Java without resorting to heavy use of annotations or reflection. Lastly, it's built for async IO using the
great [Netty](http://netty.io/) library.

Resource centric
----------------

The primary concept in Webster are [resources](https://github.com/pschirmacher/webster/blob/master/webster-core/src/main/java/webster/resource/Resource.java#L10).
Handling a request is done in two steps:

1.  The URL of the request is used to identify a matching resource. If no resource is found a 404 response is returned.
2.  If a matching resource is found, this resource is then responsible for handling the request. To accomplish this,
    it declares which HTTP methods are supported, which content types are available, when it expires, how etags are
    computed and so on. Based on this information Webster will generate the HTTP response which is returned to the
    client.

To implement the second step, Webster uses an approach which is similar to the one pioneered by [Webmachine](https://github.com/basho/webmachine/wiki).
Each request is processed according to this [diagram](https://github.com/basho/webmachine/wiki/Diagram) (or at least very similarly).
This is the main thing Webster does for you (implemented [here](https://github.com/pschirmacher/webster/blob/master/webster-core/src/main/java/webster/decisions/DefaultFlow.java#L390)).

As an application developer, you can focus on your application specific resources.

This approach makes explicit which status code is returned and which headers are set in which situation. You don't have
to make these decisions again and again. Instead, its handled consistently across resources. Focusing on resources also
allows a much more complete implementation of the HTTP feature set. E.g. conditional requests are not supported properly
by many traditional HTTP libraries/frameworks.

But the approach chosen by Webster is also more restrictive. You cannot just return whatever response you want in any
given situation (well, [you can](https://github.com/pschirmacher/webster/blob/master/webster-core/src/main/java/webster/resource/Resource.java#L12),
but it's not encouraged). The purpose of this prototypical implementation largely lies in finding the right balance
between being restrictive and allowing flexibility.

Async & containerless
-----------------------

Webster does not block the thread that initially accepts a HTTP request. Instead, request processing is done
asynchronously using Java 8's CompletableFutures ([tutorial](http://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html)).
You are also not required to use a servlet container. Webster does non-blocking IO using [Netty](http://netty.io/).
Just package your entire application as a JAR including dependencies and you're set!

Simply Java
-----------

Webster is built using plain Java (except for generating HTML which is done using [Scalate](http://scalate.fusesource.org/documentation/index.html)).
This makes it easy to get started using a familiar environment, great IDE support and very robust & fast compiler.
It should also provide a sound basis for using it from other JVM languages.

Getting started
===============
Setup
-----

Java 8 and Maven 3 are required. Webster is not yet available from a public Maven repository so it has to be installed
locally. Just clone this repo and install with Maven:

    git clone https://github.com/pschirmacher/webster.git
    cd webster
    mvn install

This will install the JARs to your local Maven repository.

Example application
-------------------

Have a look at [webster-example](https://github.com/pschirmacher/webster-example) for an example application. Just
cloning the example app, running it locally and diving into the code is the easiest way to get started.

Hello world
-----------

The following will start a server which is listening for requests on [localhost:8080/hello](http://localhost:8080/hello).

    import webster.netty.Server;
    import webster.requestresponse.Request;
    import webster.resource.Resource;
    import webster.routing.RoutingTable;

    import java.util.Optional;
    import java.util.concurrent.CompletableFuture;

    import static webster.routing.RoutingBuilder.from;
    import static webster.routing.RoutingBuilder.routingTable;

    public class App {

        public static void main(String[] args) {

            RoutingTable routingTable = routingTable()
                    .withRoute(from("/hello").toResource(new HelloWorld()))
                    .build();

            new Server.Builder().withPort(8080).build().run(routingTable);
        }

        public static class HelloWorld implements Resource {

            @Override
            public CompletableFuture<Boolean> doesRequestedResourceExist(Request request) {
                return CompletableFuture.completedFuture(true);
            }

            @Override
            public CompletableFuture<Object> entity(Request request) {
                return CompletableFuture.completedFuture("<h1>Hello from Webster!</h1>");
            }

            @Override
            public CompletableFuture<Optional<String>> etag(Request request) {
                return CompletableFuture.completedFuture(Optional.of("1"));
            }
        }
    }

Author and license
------------------

Copyright 2014 Philipp Schirmacher. Published under the Apache 2.0 license.
