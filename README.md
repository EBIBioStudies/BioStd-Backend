# Bio Studies Backend

## Modules
### Backend Web App
Java application that supports the bio studies data submission thought http web services.

### Core Model
Contains model/database representation classes of Bio-Studies backend application.

### Commons library
Contains utility methods and classes.

### Spreadsheet Readers library
Contains utility methods and classes used to read submissions input files.

### Submit Tools
Helps to perform submissions through terminal

### Convert tool
Helps to convert between different submissions formats

## Coding standards
The project follows the google [java coding standards](https://google.github.io/styleguide/javaguide.html)

- [Eclipse formatter](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)
- [Idea formatter](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)


## Development task

### Importing project

Import as gradle project in your favorite IDE


### Generating artifact

Run gradle build command either using local distribution or wrapper
 
 ```
  gradlew.bat build (Windows)
  ./gradlew build (Linux)
 ```

Generated war will be available under `BackendWebApp/builds/libs` folder. 

## Getting Started

### Dependencies
In order to build and execute the application in your local environment it's necessary to have the following
dependencies installed
* Java SDK (at least version 8)
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
There are two mechanisms to get an session key to be used for the requests to the backend:

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
