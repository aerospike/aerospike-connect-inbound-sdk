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
                        sh "mvn -f examples/jms clean install"
                        sh "mvn -f examples/kafka clean install"
                        sh "mvn -f examples/pulsar clean install"
                    }
                }

                stage("Vulnerability scanning") {
                    steps {
                       echo "Running snyk scan.."
                       sh "./gradlew --no-daemon snyk-test"
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
