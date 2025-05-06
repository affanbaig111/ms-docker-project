//   pipeline {
//     agent any
//
//     tools {
//     maven 'maven-tool'
//     }
//
//     environment {
//     DOCKERHUB_CREDENTIALS = credentials('docker-hub-creds')
//     }
//
//     stages {
//     stage('Checkout Code') {
//     steps {
//     checkout scm
//     }
//     }
//
//     stage('Clean Up Old Docker Resources') {
//     steps {
//     echo "Stopping and removing old containers/volumes..."
//     sh 'docker-compose -f docker-compose.yml down -v --remove-orphans || true'
//     sh 'docker system prune -af || true'
//     sh 'docker volume prune -f || true'
//     }
//     }
//
//     stage('Build Docker Images') {
//     steps {
//     echo 'Running build.sh to build Docker images locally...'
//     sh './build.sh'
//     }
//     }
//
//     stage('Run Docker Compose') {
//     steps {
//     echo "Starting up services with Docker Compose..."
//     sh 'docker-compose -f docker-compose.yml up -d --no-build --no-recreate'
//     }
//     }
//
//    stage('Run Newman Tests') {
//        steps {
//            echo "Running Postman tests using Newman..."
//
//            script {
//                def result = sh(script: 'newman run postman/testing.postman_collection.json -r cli', returnStatus: true)
//                if (result != 0) {
//                    error "❌ Newman tests failed. Failing the pipeline."
//                }
//            }
//        }
//    }
//     }


pipeline {
    agent any

    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-creds')
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
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

        stage('Build Docker Images') {
            steps {
                echo 'Running build.sh to build Docker images locally...'
                sh './build.sh'
            }
        }

        stage('Run Docker Compose') {
            steps {
                echo "Starting up services with Docker Compose..."
                sh 'docker-compose -f docker-compose.yml up -d --no-build --no-recreate'
            }
        }

        stage('Wait for Services to Be Ready') {
            steps {
                script {
                    def services = [
                        "mongodb": "mongodb:27017",
                        "zipkin": "http://zipkin:9411/actuator/health",
                        "product-service": "http://product-service:44585"  // You can add any other services you want to wait for here
                    ]

                    services.each { service, url ->
                        echo "Waiting for ${service} to be available..."
                        waitForService(url, service)
                    }
                }
            }
        }

        stage('Run Newman Tests') {
            steps {
                echo "Running Postman tests using Newman..."
                script {
                    def result = sh(script: 'newman run postman/testing.postman_collection.json -r cli', returnStatus: true)
                    if (result != 0) {
                        error "❌ Newman tests failed. Failing the pipeline."
                    }
                }
            }
        }
    }
}

def waitForService(url, serviceName) {
    def maxRetries = 30
    def retryCount = 0

    while (retryCount < maxRetries) {
        try {
            // Try to reach the service
            sh "curl --silent --max-time 5 ${url} > /dev/null"
            echo "${serviceName} is ready!"
            break
        } catch (Exception e) {
            echo "${serviceName} not available yet. Retrying..."
            retryCount++
            sleep 5
        }
    }

    if (retryCount == maxRetries) {
        error "Service ${serviceName} did not become available in time."
    }
}
