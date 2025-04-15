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
              try {
                sh """
                  rm -rf ${CLONE_DIR} && \
                  git clone ${GIT_REPO} ${CLONE_DIR} && \
                  cd ${CLONE_DIR} && \
                  chmod +x ./gradlew && \
                  ./gradlew clean build && \
                  cp build/libs/gomdolbook.jar ${PROJECT_DIR}/app/app.jar && \
                  cd ${PROJECT_DIR} && \
                  docker compose build --no-cache && \
                  docker compose up -d
                  """
                  slackSend(
                    channel: "#playground",
                    color: "good",
                    message: "✅ *gomdolbook 배포 성공!* (<${env.BUILD_URL}|Build #${env.BUILD_NUMBER}>)"
                  )
              }
              catch (err) {
                slackSend(
                  channel: "#playground",
                  color: "danger",
                  message: "❌ *gomdolbook 배포 실패!* (<${env.BUILD_URL}|Build #${env.BUILD_NUMBER}>)"
                )
                throw err
              }
            }
        }
    }
}
