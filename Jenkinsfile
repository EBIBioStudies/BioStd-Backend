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
        //def workspace = pwd()
        sh 'gradle bootJar --stacktrace'
        echo "${workspace}"
        sh "cp ${workspace}/BackendWebApp/build/libs/biostudy-\$(date +'%Y%m%d').jar /home/jhoan/EBI/deployments/BackendWebApp"
      }
    }
  }
}
