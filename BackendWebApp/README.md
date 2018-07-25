# Backend Web App
Bio-studies web application provides http services used by submission tool and web frontend. Main web app responsibilities are

1. Create/Delete/Update submissions including validation
1. Temporally data storing
1. Security login and access check

## Getting Started

### Dependencies
In order to build and execute the application in your local environment, it's necessary to have the following
dependencies installed:
* Java SDK (version 8 at least)
* Gradle 4.7
* Docker 

 >Notes: <br/>
 Oracle Java should be used instead of openJDK <br/>
 Docker needs to be configured to be used without super user privileges. Check
 [here](https://docs.docker.com/install/linux/linux-postinstall/#manage-docker-as-a-non-root-user) for more info.
 
 ### Application Setup
 Once the dependencies mentioned above are up and running, please follow these steps to configure the application:
 1. Clone the repository
 2. Go to the app directory
 3. Execute `gradle build`
 4. Move to `BackendWebApp` folder
 5. Execute `gradle setUpDatabase`
 6. In a different location from the repository, create a folder which will be used as the **nfs** folder
 7. Go to `BackendWebApp/src/test/resources/nfs/templates` directory
 8. Copy the `config.properties` file into the **nfs** folder, created in step 6
 9. Replace `${BASE_PATH}` with the actual path for the **nfs** folder
 10. Open `BackendWebApp/src/main/resources/application.yml` file
 11. Set the `baseDir` property to the path for the **nfs** folder
 12. Go back to root directory
 13. Execute `gradle bootRun`
 14. The application should now be up and running at
 [http://localhost:8586/biostd/test/testLogin.html](http://localhost:8586/biostd/test/testLogin.html)

### Session Key
There are two mechanisms to get a session key to be used for the requests to the backend:

* **Command Line:**
  1. Inside `BackendWebApp` folder execute `gradle getAuthToken`
  2. Copy the value after **Session Key:**
  
* **UI:**
  1. Go to [http://localhost:8586/biostd/test/testLogin.html](http://localhost:8586/biostd/test/testLogin.html)
  2. Type the username `admin_user@ebi.ac.uk`
  3. Type the password `123456` (yes, we know, remember this is a local instance :grimacing:)
  4. Leave the PassHash field empty
  5. Click `Login`
  6. Copy the value after **Session Key:**
  
### Send A Submission
Execute a request with this information:
* **Endpoint:** http://localhost:8586/biostd/submit/createupdate?BIOSTDSESS=${SESSION_KEY}
* **Request Type:** POST
* **content/type:** application/json
* **body:** You can find an example of a submision in
[src/test/resources/intput/S-ACC-TEST.json](src/test/resources/intput/S-ACC-TEST.json)

> Note: Replace ${SESSION_KEY} with the value obtained from [Session Key](#session-key) section.

## How to generate artifacts
Both war and jar deployment type is supported only execute build command

    gradle bootJar
    gradle bootWar
