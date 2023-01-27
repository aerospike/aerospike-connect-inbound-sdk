pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    }

    stages {
        stage("Pipeline" ) {
            stages {
                stage("Checkout") {
                    steps {
                        checkout([
                            $class: 'GitSCM',
                            branches: scm.branches,
                            extensions: scm.extensions + [[$class: 'CleanBeforeCheckout']],
                            userRemoteConfigs: scm.userRemoteConfigs
                        ])
                    }
                }

                stage("Build") {
                    steps {
                        echo "Building.."
                        sh "./gradlew --no-daemon clean build"
                        sh "mvn -f examples/jms clean install -Dsnyk.skip"
                        sh "mvn -f examples/kafka clean install -Dsnyk.skip"
                        sh "mvn -f examples/pulsar clean install -Dsnyk.skip"
                    }
                }

                stage("Vulnerability scanning") {
                    steps {
                       echo "Running snyk scan.."
                       sh "./gradlew --no-daemon snyk-test"
                       sh "mvn -f examples/jms test"
                       sh "mvn -f examples/kafka test"
                       sh "mvn -f examples/pulsar test"
                    }
                }
            }
        }
    }

    post {
        cleanup {
            cleanWs()
        }
    }
}
