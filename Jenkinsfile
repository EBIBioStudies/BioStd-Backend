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
        sh 'cp ${WORKSPACE}/BackendWebApp/build/libs/biostudy-$(date +'%Y%m%d').jar /home/jhoan/EBI/deployments/BackendWebApp'
      }
    }
  }
}
