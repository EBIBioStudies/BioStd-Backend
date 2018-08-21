pipeline {
  agent any
  tools {
    gradle "Gradle 4.7"
  }

  stages {
    stage('Build') {
      steps {
        sh 'gradle clean build --stacktrace'
      }
    }

    stage('Generate Artifact') {
      steps {
        sh 'gradle bootJar --stacktrace'
      }
    }
  }
}
