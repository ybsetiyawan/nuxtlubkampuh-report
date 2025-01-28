pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = '/gsi-system/presensi-gsi-report'
        PATH_REPOSITORY = "/gsi-system/presensi-gsi-report.git"
        APPNAME = "presensi-gsi-report"
        TIMESTAMP = "${currentBuild.getTimeInMillis()}"
        IMAGE_TAG = "${TIMESTAMP}-${BUILD_ID}"
    }

     stages {
        stage('ENV DEV') {
            steps {
                script {
                    def envFilePath = '.env'  // Ganti dengan jalur yang sesuai
                    def envContents = readFile(envFilePath).trim()
                    def newEnvContents = envContents.replace("DB.HOST=${HOST_DB_PRESENSI_GSI_PROD}", "DB.HOST=${HOST_DB_PRESENSI_GSI_DEV}")
                    newEnvContents = newEnvContents.replace("DB.NAME=${NAME_DB_PRESENSI_GSI_PROD}", "DB.NAME=${NAME_DB_PRESENSI_GSI_DEV}")
                    
                    // Tulis kembali isi file .env
                    writeFile file: envFilePath, text: newEnvContents
                    sh "cat .env"
                }
            }
        }
        stage('Build Image Docker DEV') {
            steps {
                script {
                    // Bangun Docker image dengan versi yang ditentukan
                    sh "docker build -t ${REGISTRY_URL}${DOCKER_IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }
        }

        stage('Push Image Docker DEV') {
            steps {
                script {
                    // Push Docker image ke registry dengan versi yang ditentukan
                    sh "docker push ${REGISTRY_URL}${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Modify Image Tag DEV') {
            steps {
                script {
                    // Mengganti tag image
                    sh "sed -i 's|image:.*|image: ${REGISTRY_URL}${DOCKER_IMAGE_NAME}:${IMAGE_TAG}|g' ${PATH_KUSTOMIZE}/base/${APPNAME}/deployment.yaml"
                    sh "cat ${PATH_KUSTOMIZE}/base/${APPNAME}/deployment.yaml"
                }
            }
        }

     //   PRODUCTION
        stage('ENV PRODUCTION') {
            steps {
                script {
                    def envFilePath = '.env'
                    def envContents = readFile(envFilePath).trim()
                    def newEnvContents = envContents.replace("DB.HOST=${HOST_DB_PRESENSI_GSI_DEV}", "DB.HOST=${HOST_DB_PRESENSI_GSI_PROD}")
                    newEnvContents = newEnvContents.replace("DB.NAME=${NAME_DB_PRESENSI_GSI_DEV}", "DB.NAME=${NAME_DB_PRESENSI_GSI_PROD}")
                    
                    // Tulis kembali isi file .env
                    writeFile file: envFilePath, text: newEnvContents
                    sh "cat .env"
                }
            }
        }
        stage('Build Image Docker Production') {
            steps {
                script {
                    // Bangun Docker image dengan versi yang ditentukan
                    sh "docker build -t ${REGISTRY_URL}${DOCKER_IMAGE_NAME}:p-${IMAGE_TAG} ."
                }
            }
        }

        stage('Push Image Docker Production') {
            steps {
                script {
                    // Push Docker image ke registry dengan versi yang ditentukan
                    sh "docker push ${REGISTRY_URL}${DOCKER_IMAGE_NAME}:p-${IMAGE_TAG}"
                }
            }
        }

        stage('Modify Image Tag Production') {
            steps {
                script {
                    // Mengganti tag image
                    sh "sed -i 's|newName:.*|newName: ${REGISTRY_URL}${DOCKER_IMAGE_NAME}|g' ${PATH_KUSTOMIZE}/overlays/production/${APPNAME}/kustomization.yaml"
                    sh "sed -i 's|newTag:.*|newTag: p-${IMAGE_TAG}|g' ${PATH_KUSTOMIZE}/overlays/production/${APPNAME}/kustomization.yaml"
                    sh "cat ${PATH_KUSTOMIZE}/overlays/production/${APPNAME}/kustomization.yaml"
                }
            }
        }

        stage('Modify and Push') {
            steps {
                script {
                    // Ganti ke direktori lain
                    dir("${PATH_KUSTOMIZE}") {
                        // Melakukan perubahan dan menambahkan ke Git
                        sh "git config --global user.email ${GITLAB_USER_EMAIL}"
                        sh "git config --global user.name ${GITLAB_USER_NAME}"

                        sh 'git pull origin main'
                        sh 'git add .'
                        sh 'git commit -m "update Image"'
                        // Lakukan git push
                        sh 'git push origin main'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def status = currentBuild.currentResult.toString()
                def message

                if (status == 'SUCCESS') {
                    message = "Pipeline '${currentBuild.fullDisplayName}' has finished successfully Bro. :white_check_mark:"
                } else {
                    message = "Pipeline '${currentBuild.fullDisplayName}' has Failed Bro. :x:"
                }

                discordSend title: "Build And Push Docker Job $JOB_NAME",
                            link: "${REPOSITORY}${PATH_REPOSITORY}",
                            webhookURL: "$DISCORD_WEBHOOK",
                            result: currentBuild.currentResult, 
                            description: message,
                            footer: "${APPNAME}"
            }
        }
    }
}
