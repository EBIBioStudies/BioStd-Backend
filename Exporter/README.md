# Submission Exporter

Application in charge of execute export task for different purposes 

## Jobs
### 1. Full export
General .json file by processing the full list of submissions

![Flow - Diagram](docs/Flow.png)

#### Submission Forker Job
Query the full list of submissions and distribute between workers processing queues.

#### Worker Job
Take basic submission information, query submissions sections, links and attributes and generate json representation. Submit to Join queue after complete the process.

#### Join worker
Get the full list of submissions json representation and merge into single out file.


### 2. Partial export
Identify new submissions create json file whit their content and notify configured url.

### 3. Stats export
Generate stats about each single submission in a CSV file. Stats are calculated as following:

#### Page tab file size
NFS is explorer to extract and calculate page tab file size

#### Submission files (number and size)
Database FileRef table is query for each submission to calculated the total number of files and their size.

