pipeline {
  agent {
    node {
      label 'osx'
    }
    
  }
  stages {
    stage('Check') {
      steps {
        sh './mvnw install dockerfile:build'
      }
    }
    stage('') {
      steps {
        archiveArtifacts 'target/surefire-reports/**/*.xml'
      }
    }
  }
}