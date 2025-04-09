pipeline {
    agent any

    tools {
        maven 'maven-tool'
    }

    environment {
        GIT_COMMIT = ''  // Will store the short commit hash
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/affanbaig111/ms-docker-project']]
                )
                script {
                    GIT_COMMIT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Build API Gateway with Dockerfile') {
            steps {
                dir('api-gateway') {
                    script {
                        def tag = "affan341/api-gateway:${GIT_COMMIT}"
                        echo "Building and pushing API Gateway with tag ${tag}"
                        sh "docker build -t ${tag} ."
                        sh "docker push ${tag}"
                    }
                }
            }
        }

        stage('Build Other Services with Jib') {
            steps {
                script {
                    def jibServices = [
                        'discovery-server',
                        'inventory-service',
                        'notification-service',
                        'order-service',
                        'product-service'
                    ]

                    for (svc in jibServices) {
                        dir(svc) {
                            echo "Building ${svc} with Jib..."
                            sh "mvn compile com.google.cloud.tools:jib-maven-plugin:3.4.0:build -Djib.to.image=affan341/${svc}:${GIT_COMMIT}"
                        }
                    }
                }
            }
        }
    }
}