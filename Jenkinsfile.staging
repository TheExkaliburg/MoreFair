pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr:'5'))
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
    }

    tools {
        // Install the Maven version configured as "Maven" and add it to the path.
        maven "Maven"
    }

    stages {
        stage('Build') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore=true clean package"

                sh "docker build -t kaliburg/fairgame ."
            }
        }

        stage('Login') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }

        stage('Push') {
            steps {
                sh 'docker push kaliburg/fairgame:staging'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
    }
}
