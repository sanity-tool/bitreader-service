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
    stage('Publish') {
      steps {
        junit(allowEmptyResults: true, testResults: 'target/surefire-reports/**/*.xml')
      }
    }
  }
}