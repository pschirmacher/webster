webster
=======

[Webmachine](https://github.com/basho/webmachine/wiki)-inspired HTTP for Java.

Please be aware that this is just a proof-of-concept implementation. It's not ready for being used anywhere near
a production system.

Try out the [example](https://github.com/pschirmacher/webster-example)!

Rationale
=========

webster is a library to build HTTP servers using Java 8. It implements key HTTP features like content negotation and
conditional requests so you don't have to. The goal is to help developers build applications that adhere to the HTTP
spec without having to reimplement the same things over and over again. It also tries to keep things simple using just
plain Java without resorting to heavy use of annotations or reflection. Lastly, it's built for async IO using the
great [Netty](http://netty.io/) library.

Resource centric
----------------

The primary concept in webster are resources. Handling a request is done in two steps:

1.  The URL of the request is used to identify a matching resource. If no resource is found a 404 response is returned.
2.  If a matching resource is found, this resource is then responsible for handling the request. To accomplish this,
    it declares which HTTP methods are supported, which content types are available, when it expires, how etags are
    computed and so on. Based on this information webster will generate the HTTP response which is returned to the
    client.

To implement the second step, webster uses a similar approach as [webmachine](https://github.com/basho/webmachine/wiki). Each request is processed as described in 
this [diagram](https://github.com/basho/webmachine/wiki/Diagram) (or at least very similarly). This is the main thing
webster does for you (implemented [here](https://github.com/pschirmacher/webster/blob/master/webster-core/src/main/java/webster/decisions/DefaultFlow.java#L390)). 

As an application developer, you can focus on your application specific resources.

Async & containerless
-----------------------

webster does not block the thread that initially accepts a HTTP request. Instead, request processing is done
asynchronously using Java 8's CompletableFutures ([tutorial](http://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html)). You are also
not required to use a servlet container. webster does non-blocking IO using [Netty](http://netty.io/). Just package your entire application
including dependencies as a JAR and run it!

Simply Java
-----------

webster is built using plain Java (except for generating HTML which is done using [Scalate](http://scalate.fusesource.org/documentation/index.html)).
This makes it easy to get started using a familiar environment, great IDE support and a rock solid compiler that is
not only robust but also super fast. It should also provide a sound basis for using it from other JVM languages.

Setup
=====

Java 8 and Maven 3 are required. webster is not yet available from a public Maven repository so it has to be installed
locally. Just clone this repo and install with Maven:

    git clone https://github.com/pschirmacher/webster.git
    cd webster
    mvn install

This will install the JARs to your local Maven repository. Have a look at [webster-example](https://github.com/pschirmacher/webster-example)
for an example application.

Author and license
------------------

Copyright 2014 Philipp Schirmacher. Published under the Apache 2.0 license.