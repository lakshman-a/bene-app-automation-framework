// ============================================================================
// PIPELINE: Playwright + Cucumber UI/API Tests (local Jenkins, Docker Linux agent)
// Repo: https://github.com/lakshman-a/bene-app-automation-framework.git
// ============================================================================
//   - Runs on the Jenkins controller's built-in Linux node (Docker container)
//   - Playwright runs HEADLESS, browsers installed fresh on the "Install Browser" stage
//   - Allure results published via the Allure Jenkins plugin
// ============================================================================

pipeline {
    agent any

    parameters {
        string(  name: 'BRANCH',      defaultValue: 'main',      description: 'Git branch to build')
        choice(  name: 'ENVIRONMENT', choices: ['qa', 'dev', 'uat'], description: 'Target environment (-Denv=)')
        choice(  name: 'TEST_TAGS',   choices: ['@smoke', '@regression', '@bene', '@login', '@products', '@cart'], description: 'Cucumber tags to run')
        choice(  name: 'BROWSER',     choices: ['chromium', 'firefox', 'webkit'], description: 'Browser for UI tests')
        string(  name: 'CUSTOM_TAGS', defaultValue: '',          description: 'Custom Cucumber tag expression (overrides TEST_TAGS if set)')
    }

    environment {
        EFFECTIVE_TAGS = "${params.CUSTOM_TAGS?.trim() ? params.CUSTOM_TAGS : (params.TEST_TAGS ?: '@smoke')}"
        REPO_URL       = 'https://github.com/lakshman-a/bene-app-automation-framework.git'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        stage('Setup') {
            steps {
                cleanWs()
                echo """
                ========================================
                   bene-app Automation Pipeline
                ========================================
                  Branch:      ${params.BRANCH ?: 'main'}
                  Environment: ${params.ENVIRONMENT ?: 'qa'}
                  Browser:     ${params.BROWSER ?: 'chromium'}
                  Tags:        ${EFFECTIVE_TAGS}
                  Build:       #${env.BUILD_NUMBER}
                ========================================
                """
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${params.BRANCH ?: 'main'}",
                    url: "${REPO_URL}",
                    credentialsId: 'github-pat'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn -B clean compile -DskipTests'
            }
        }

        stage('Install Browser') {
            steps {
                sh """mvn -B exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install ${params.BROWSER ?: 'chromium'} --with-deps" """
            }
        }

        stage('Run Tests') {
            steps {
                sh """mvn -B test -Denv=${params.ENVIRONMENT ?: 'qa'} -Dbrowser=${params.BROWSER ?: 'chromium'} -Dheadless=true "-Dcucumber.filter.tags=${EFFECTIVE_TAGS}" -Dmaven.test.failure.ignore=true"""
            }
        }

        stage('Collect Results') {
            steps {
                junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
            }
        }

        stage('Allure Report') {
            steps {
                allure(includeProperties: false, jdk: '', results: [[path: 'target/allure-results']])
            }
        }
    }

    post {
        always {
            archiveArtifacts(
                artifacts: 'target/allure-results/**,target/allure-report/**,target/logs/**',
                allowEmptyArchive: true
            )
            echo "Build ${currentBuild.currentResult} — #${env.BUILD_NUMBER} [${params.BROWSER ?: 'chromium'}] [${EFFECTIVE_TAGS}]. Allure report: ${env.BUILD_URL}allure/"
        }
    }
}
