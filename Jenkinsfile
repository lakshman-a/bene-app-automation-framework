// ============================================================================
// PIPELINE: Playwright + Cucumber UI Tests (Windows)
// Repo: https://github.com/sivasakthi-ravi/playwright-java-framework-v3-main.git
// ============================================================================
// UI-specific:
//   - Playwright runs HEADLESS (no display needed in CI)
//   - No Selenium Grid required — Playwright bundles its own browsers
//   - Screenshots on failure captured by framework Hooks.java
//   - Screenshots included in Allure & Cucumber reports
// ============================================================================

pipeline {
    agent any

    triggers {
        parameterizedCron('''
            0 8 * * * %BRANCH=main;ENVIRONMENT=qa;TEST_TAGS=@smoke;BROWSER=chromium
            0 20 * * * %BRANCH=main;ENVIRONMENT=qa;TEST_TAGS=@regression;BROWSER=chromium
        ''')
    }

    parameters {
        string(  name: 'BRANCH',      defaultValue: 'main',      description: 'Git branch')
        choice(  name: 'ENVIRONMENT', choices: ['qa', 'dev'],    description: 'Target environment')
        choice(  name: 'TEST_TAGS',   choices: ['@smoke', '@regression', '@all', '@qa', '@login', '@products', '@cart'], description: 'Cucumber tags')
        choice(  name: 'BROWSER',     choices: ['chromium', 'firefox', 'webkit'], description: 'Browser for UI tests')
        string(  name: 'CUSTOM_TAGS', defaultValue: '',          description: 'Custom tags (overrides dropdown)')
    }

    environment {
        EFFECTIVE_TAGS = "${params.CUSTOM_TAGS ? params.CUSTOM_TAGS : (params.TEST_TAGS ?: '@smoke')}"
        REPO_URL       = 'https://github.com/sivasakthi-ravi/playwright-java-framework-v3-main.git'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        stage('Setup') {
            steps {
                bat 'git config --global core.longpaths true'
                cleanWs()
                echo """
                ========================================
                   Playwright UI Test Pipeline v3
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
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Install Browser') {
            steps {
                bat """mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install ${params.BROWSER ?: 'chromium'} --with-deps" || echo "Browser install attempted" """
            }
        }

        stage('Run UI Tests') {
            steps {
                bat """mvn test -Denv=${params.ENVIRONMENT ?: 'qa'} -Dbrowser=${params.BROWSER ?: 'chromium'} -Dheadless=true \"-Dcucumber.filter.tags=${EFFECTIVE_TAGS}\" -Dmaven.test.failure.ignore=true"""
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
            emailext(
                subject: "${currentBuild.currentResult == 'SUCCESS' ? 'PASSED' : 'FAILED'}: UI Tests v3 #${env.BUILD_NUMBER} [${params.BROWSER ?: 'chromium'}] [${EFFECTIVE_TAGS}]",
                body: "<p>Build <b>${currentBuild.currentResult}</b>. See <a href='${env.BUILD_URL}allure/'>Allure Report</a>.</p>",
                to: 'a.lakshman1991@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}
