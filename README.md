# Bio Studies Backend

## Modules
### Backend Web App
Java application that support bio studies data submission thought http web services.

### Core Model
Contains model/database representation classes of Bio-Studies backend application.

### Commons library
Contains utility methods and classes.

### Spreadsheet Readers library
Contains utility methods and classes used to read submissions input files.

### Submit Tools
Help perform submissions thought terminal

### Convert tool
Helps to converts between different submissions formats

## Coding standards
Project follow google [java coding standards](https://google.github.io/styleguide/javaguide.html)

- [Eclipse formatter](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)
- [Idea formatter](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)


## Development task

### Importing project

import as gradle project in your favorite IDE


### Generating artifact

Run gradle build command either using local distribution or wrapper
 
 ```
  gradlew.bat build (Windows)
  ./gradlew build (Linux)
 ```

Generated war will be available under `BackendWebApp/builds/libs` folder. 

