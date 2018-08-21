pipeline {
  agent any

  stages {
    stage('Build') {
      steps {
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
