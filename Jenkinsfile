pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr:'10', daysToKeepStr:'28', artifactNumToKeepStr:'5', artifactDaysToKeepStr:'14'))
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
        ENV_FILE = credentials('fairgame-env')
    }

    tools {
        // Install the Maven version configured as "Maven" and add it to the path.
        maven "Maven"
    }

    stages {
        stage('Build/Test') {
            steps {
                // git branch: 'main', credentialsId: 'github', url: 'https://github.com/TheExkaliburg/MoreFair.git'
                sh 'mvn -Dmaven.test.failure.ignore=true clean package'
            }

            post {
                success {
                    junit allowEmptyResults: true, skipOldReports: true, testResults: 'backend/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Docker') {
            steps {
                sh 'docker build -t kaliburg/fairgame:latest .'
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kaliburg/fairgame:latest'
            }
            post {
                always {
                    sh 'docker logout'
                }
            }
        }

        stage('Compose/Startup') {
            steps {
                sh 'docker compose stop'
                sh 'docker compose --env-file $ENV_FILE up --pull always --force-recreate --detach'
            }
        }
    }
}
