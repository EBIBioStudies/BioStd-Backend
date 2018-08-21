pipeline {
  agent any
  tools {
    java "Java 8"
    gradle "Gradle 4.7"
  }

  stages {
    stage('Build') {
      steps {
        sh 'gradle --version'
        sh 'gradle clean build'
      }
    }

    stage('Generate Artifact') {
      steps {
        sh 'gradle bootJar'
      }
    }
  }
}
