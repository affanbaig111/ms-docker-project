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

//         stage('Login to Docker Hub') {
//             steps {
//                 withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
//                     sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
//                 }
//             }
//         }


         stage('Build Docker Images') {
                    steps {
                        echo 'Running build.sh to build Docker images locally...'
                        sh './build.sh' // Ensure build.sh is executable (chmod +x build.sh)
                    }
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
                sh 'newman run postman/testing.postman_collection.json -r cli'
            }
        }
    }

//   post {
//         always {
//             script {
//                 def userChoice = input(
//                     id: 'StopConfirm',
//                     message: 'Pipeline finished. Do you want to stop and remove Docker services?',
//                     parameters: [
//                         choice(
//                             choices: ['Yes', 'No'],
//                             description: 'Choose an action',
//                             name: 'Stop'
//                         )
//                     ]
//                 )
//
//                 if (userChoice == 'Yes') {
//                     echo "Stopping Docker services..."
//                     sh 'docker-compose -f docker-compose.yml down -v --remove-orphans || true'
//                 } else {
//                     echo "Leaving Docker services running..."
//                 }
//             }
//         }
//     }
}