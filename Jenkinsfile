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

    stage('Deploy') {
      steps {
//        sh 'gradle bootJar --stacktrace'
//        sh "cp ${workspace}/BackendWebApp/build/libs/biostudy-\$(date +'%Y%m%d').jar /home/jhoan/EBI/deployments/BackendWebApp"
        sh "echo ${workspace}"
        sh "pgrep -lf 'biostudy-[0-9]+.jar'"
      }
    }

//    stage('Run') {
//      steps {
//        sh 'sudo pkill -f biostudy'
//        withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
//          sh "nohup ${env.JAVA_HOME}/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n -jar /home/jhoan/EBI/deployments/BackendWebApp/biostudy-\$(date +'%Y%m%d').jar --biostudy.baseDir=\"/var/lib/jenkins/nfs\" --biostudy.environment=DEV --eutoxrisk-file-validator.enabled=true --spring.mail.host=\"smtp.ebi.ac.uk\" >> /var/lib/jenkins/nfs/BioStd-Backend.log &"
//        }
//      }
//    }
  }
}
