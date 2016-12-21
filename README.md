# Java server benchmark with GUI

## Gradle subprojects

* `bench` - benchmark gui (main one)
* `client` - several client implementations, who play a client role during benchmarking
* `commons` - shard stuff among projects
* `server` - several server architectures for benchmarking
* `server-runner` - benchmark server, which runs servers and talks to `bench` remotely
* `test` - ...

## GUI (`bench`)

It is tabbed. First tab has controls for configuring bench. parameter. Other tabs 
become available after running benchmark (clicking on bench button), all these display
statistics somehow and you can save their data as txt/png files (depends on the tab).

So:

* Input parameter 
* Press bench button
* Go to, for example, results table tab and do right click on the table to save it