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

## Setup
In order to start the application, please follow these steps
1. Open the configuration file: `src/main/resources/application.yml`
2. Configure the local database connection by providing:
  * jdbc-url
  * username
  * password
3. Execyte `gradle bootRun`  
  
### Stats Setup
In order to configure the local stats job, please follow these steps:
1. Open the configuration file: `src/main/resources/application.yml`
2. Look for the **stats** section in the local profile
3. Provide the proper values for:
   * `${BASE_PATH}` is the path were the submissions are stored (the **nfs** folder)
   * `${OUTPUT_PATH}` is the path for a folder that will be used to store the stats report
