pipeline {
    agent any

    tools {
        maven 'maven-tool'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-creds')
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/affanbaig111/ms-docker-project']]
                )
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
                    echo "Packaging API Gateway JAR..."
                    sh 'mvn clean package -DskipTests'

                    echo "Building and pushing API Gateway Docker image..."
                    sh 'docker build -t affan341/api-gateway:latest .'
                    sh 'docker push affan341/api-gateway:latest'
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
                            echo "Building and pushing ${svc} with Jib..."
                            sh 'mvn compile com.google.cloud.tools:jib-maven-plugin:3.4.0:build'
                        }
                    }
                }
            }
        }

        stage('Clean Up Old Docker Resources') {
            steps {
                echo "Stopping and removing old containers/volumes..."
                sh 'docker-compose -f docker-compose.yml down -v --remove-orphans || true'
                sh 'docker system prune -af || true'
                sh 'docker volume prune -f || true'
            }
        }

        stage('Run Docker Compose') {
            steps {
                echo "Starting up services with Docker Compose..."
                sh 'docker-compose -f docker-compose.yml up -d'
            }
        }

        stage('Run Newman Tests') {
            steps {
                echo "Running Postman tests using Newman..."
                sh '''
                    docker run --rm \
                        -v $(pwd)/postman:/etc/newman \
                        postman/newman:alpine \
                        run /etc/newman/testing.postman_collection.json
                '''
            }
        }


        stage('Check Logs for notification Service') {
            steps {
                echo "Fetching logs for notification-service container..."
                sh 'docker logs notification-service --tail 100'
            }
        }
    }


}