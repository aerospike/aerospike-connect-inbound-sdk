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
                        sh "mvn -B -Dstyle.color=never -f examples/jms -U clean install"
                        sh "mvn -B -Dstyle.color=never -f examples/kafka clean install"
                        sh "mvn -B -Dstyle.color=never -f examples/pulsar clean install"
                    }
                }

                stage("Checks") {
                    parallel {
                        stage("Vulnerability scanning") {
                            steps {
                               echo "Running snyk scan.."
                               sh "./gradlew --no-daemon snyk-test"
                            }
                        }
                        stage("Tests") {
                            steps {
                                script {
                                    echo "Running tests.."
                                    sh "./gradlew --no-daemon test"
                                }
                            }
                        }
                    }
                }

                stage("Upload") {
                    steps {
                        echo "Uploading archives.."
                        sh "./gradlew --no-daemon publish"
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
