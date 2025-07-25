pipeline {
    agent { label 'jenkins' }

    environment {
        PROJECT_DIR = "/home/kim/production/gomdolbook"
        CLONE_DIR = "${PROJECT_DIR}/app"
        GIT_REPO = "https://github.com/lazerfit/gomdolbook-back.git"
    }

    stages {
        stage('Clone & Deploy (on same host)') {
            steps {
                script {
                    try {
                        withCredentials([
                            usernamePassword(credentialsId: 'kc_admin', usernameVariable: 'KC_ADMIN_USER', passwordVariable: 'KC_ADMIN_PASSWORD'),
                            usernamePassword(credentialsId: 'mariadb-root', usernameVariable: 'MARIADB_ROOT_USER', passwordVariable: 'MARIADB_ROOT_PASSWORD'),
                            usernamePassword(credentialsId: 'mariadb-gomdol', usernameVariable: 'MARIADB_USER', passwordVariable: 'MARIADB_PASSWORD'),
                            usernamePassword(credentialsId: 'jwt-token', usernameVariable: 'JWT_USER', passwordVariable: 'JWT_SECRET'),
                            usernamePassword(credentialsId: 'aladin_ttbkey', usernameVariable: 'ALADIN_USER', passwordVariable: 'ALADIN_TTBKEY'),
                            usernamePassword(credentialsId: 'management_admin', usernameVariable: 'USER_NAME', passwordVariable: 'APP_SECRET_PASSWORD')
                        ]) {
                            sh """#!/bin/bash
                                set -e

                                rm -rf ${CLONE_DIR}
                                git clone ${GIT_REPO} ${CLONE_DIR}
                                cd ${CLONE_DIR}
                                chmod +x ./gradlew
                                ./gradlew clean build

                                cp build/libs/gomdolbook.jar ${PROJECT_DIR}/app/app.jar

                                cd ${PROJECT_DIR}
                                echo "KC_BOOTSTRAP_ADMIN_USERNAME=${KC_ADMIN_USER}" > .env
                                echo "KC_BOOTSTRAP_ADMIN_PASSWORD=${KC_ADMIN_PASSWORD}" >> .env
                                echo "MARIADB_ROOT_PASSWORD=${MARIADB_ROOT_PASSWORD}" >> .env
                                echo "MARIADB_USER=${MARIADB_USER}" >> .env
                                echo "MARIADB_PASSWORD=${MARIADB_PASSWORD}" >> .env
                                echo "SPRING_JWT_SECRET=${JWT_SECRET}" >> .env
                                echo "ALADIN_TTBKEY=${ALADIN_TTBKEY}" >> .env
                                echo "SPRING_SECURITY_USER_NAME=${USER_NAME}" >> .env
                                echo "SPRING_SECURITY_USER_PASSWORD=${APP_SECRET_PASSWORD}" >> .env
                                chmod 600 .env

                                echo "==== .env 내용 확인 ===="
                                cat .env
                                echo "========================="

                                docker compose --env-file .env build --no-cache
                                docker compose --env-file .env up -d
                            """

                            slackSend(
                                channel: "#playground",
                                color: "good",
                                message: "✅ *gomdolbook 배포 성공!* (<${env.BUILD_URL}|Build #${env.BUILD_NUMBER}>)"
                            )
                        }
                    } catch (err) {
                        slackSend(
                            channel: "#playground",
                            color: "danger",
                            message: "❌ *gomdolbook 배포 실패!* (<${env.BUILD_URL}|Build #${env.BUILD_NUMBER}>)"
                        )
                        throw err
                    } finally {
                        sh """#!/bin/bash
                            cd ${PROJECT_DIR}
                            rm -f .env
                        """
                    }
                }
            }
        }
    }
}
