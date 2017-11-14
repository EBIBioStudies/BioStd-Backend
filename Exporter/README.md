# Submission Exporter

Application in charge of generate submission files json file. Exporter process submissions 


![Flow - Diagram](docs/Flow.png)

## Submission Forker Job
Query the full list of submissions and distribute between workers processing queues.

## Worker Job
Take basic submission information, query submissions sections, links and attributes and generate json representation. Submit to Join queue after complete the process.

## Join worker
Get the full list of submissions json representation and merge into single out file.


# Execution example

compile application using gradle 

```
  ./gradlew clean build
```

Run generated jar in ``build/libs`` folder 


```
java -jar exporter-0.0.1-SNAPSHOT.jar --application.queryModified=" limit 1"
```
