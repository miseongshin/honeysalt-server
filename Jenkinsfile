/**
 * CI/CD — Jenkins 가 GitHub main 을 받아 이 순서대로 돈다 (Build Now)
 * CI: 테스트 · CD: 이미지 만들기 → 다시 띄우기 → health 확인
 * Jenkins 작업 honeysalt-server 가 쓴다 · 테스트만 돌리는 것은 Jenkinsfile.test (작업 honeysalt-test)
 * 비밀값은 여기 두지 않는다 — 설정(/opt/honeysalt/config)과 마스터 키(/opt/honeysalt/.env)는 서버 것을 그대로 쓴다
 *
 * @author shinm
 * @since 2026-11-07
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-07        shinm              최초 생성
 * 2026-11-10        shinm              앱 로그 크기 제한 (10MB × 5)
 */
pipeline {
    agent any

    options {
        disableConcurrentBuilds()                 // 두 번 눌러도 하나씩
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('테스트 (CI)') {
            steps {
                sh 'chmod +x gradlew'
                // DB 는 Testcontainers 가 PostgreSQL 15 를 잠깐 띄운다 (application-test.yml)
                sh './gradlew --no-daemon clean test -PtestProfile=test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('이미지 만들기 (CD)') {
            steps {
                sh 'docker build -t honeysalt-server .'
            }
        }

        stage('다시 띄우기 (CD)') {
            steps {
                // (앱 컨테이너를 지우고 기존이미지 연결을 끊고 새 이미지로 다시 띄운다) (지울 컨테이너가 없어도 멈추지 않게 || true)
                sh 'docker rm -f honeysalt-server || true'
                // 앱 로그(docker logs)는 10MB 씩 5개까지만 둔다 — 회원 번호가 찍히는 로그가 끝없이 쌓이지 않게
                sh 'docker run -d --name honeysalt-server --restart unless-stopped --network honeysalt-net -p 127.0.0.1:8080:8080 --log-opt max-size=10m --log-opt max-file=5 -v /opt/honeysalt/config:/app/config:ro --env-file /opt/honeysalt/.env -e SPRING_PROFILES_ACTIVE=prod honeysalt-server'
            }
        }

        stage('health 확인 (CD)') {
            steps {
                // 같은 honeysalt-net 안이라 컨테이너 이름으로 찾는다 · 뜨는 데 시간이 걸려 5초마다 최대 3분
                sh '''
                    for i in $(seq 1 36); do
                        if curl -fs http://honeysalt-server:8080/actuator/health | grep -q '"status":"UP"'; then
                            echo "health UP"
                            exit 0
                        fi
                        sleep 5
                    done
                    echo "health 가 UP 이 아니다 — 앱 로그 끝부분:"
                    docker logs --tail 80 honeysalt-server
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f --filter label=app=honeysalt-server || true'    // 연결되지 않은 예전 앱 이미지만 지우기 (디스크 · 다른 이미지는 두고)
        }
    }
}
