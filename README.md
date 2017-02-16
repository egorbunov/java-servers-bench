# Java server benchmark

## Gradle subprojects

* `bench` - benchmark gui (main one)
* `client` - several client implementations, who play a client role during benchmarking
* `commons` - shard stuff among projects
* `server` - several server architectures for benchmarking
* `server-runner` - benchmark server, which runs servers and talks to `bench` remotely
* `test` - ...

## Benchmarking servers architectures

* TCP Server, which creates separate thread for serving each client
* TCP Server, which uses Cached Thread Pool for each client connection
* Asynchronous TCP Server
* Single threaded TCP server
* TCP server, which uses NIO (Selectors)
* UDP Server, which uses separate threads for each received request processing
* UDP Server with fixed thread pool for serving requests

Each request contains array, which must be sorted on server and sent back.

## Build and run

```bash
$ cd java-servers-bench
$ ./gradlew build
```

* Successful build will generate `build/jars` and fill it with JARS!

Example usage:

```bash
# TERMINAL 1
$ java -server -jar -Dlogback.configurationFile=logback-info.xml build/jars/server-runner-0.1.jar 6666

# TERMINAL 2
$ java -jar -Dlogback.configurationFile=logback-info.xml build/jars/bench-0.1.jar
```

We pass log config. due to zipped in jar config will produce debug logs also =)

## GUI (`bench`)

It is tabbed. First tab has controls for configuring bench. parameter. Other tabs 
become available after running benchmark (clicking on bench button), all these display
statistics somehow and you can save their data as txt/png files (depends on the tab).

So:

* Input parameter 
* Press bench button
* Go to, for example, results table tab and do right click on the table to save it

## Results

See `results` directory
