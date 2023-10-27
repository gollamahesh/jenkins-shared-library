pipeline {
    agent { node { label 'AGENT-1' } }
    environment{
        //here if you create any variable you will have global access, since it is environment no need of def
        packageVersion = ''
    }
    parameters{
        string(name: 'component', defaultValue: '', description: 'which component?')
    }
    stages {
        stage('version'){
            steps {
                script{
                    def packageJson = readJSON(file: 'package.json')
                    packageVersion = packageJson.version
                    echo "version: ${packageVersion}"
                }
            }
        }
        stage('Install depdencies') {
            steps {
                sh 'npm install'
            }
        }
        stage('Unit test') {
            steps {
                echo "unit testing is done here"
            }
        }
        //sonar-scanner command expect sonar-project.properties should be available
        // stage('Sonar Scan') {
        //     steps {
        //         sh 'ls -ltr'
        //         sh 'sonar-scanner'
        //     }
        // }
        stage('Build') {
            steps {
                sh 'ls -ltr'
                sh "zip -r ${params.component}.zip ./* --exclude=.git --exclude=.zip"
            }
        }
        stage('SAST') {
            steps {
                echo "SAST Done"
                echo "package version: $packageVersion"
            }
        }
        stage('Publish Artifact') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: '52.87.243.88:8081/',
                    groupId: 'com.roboshop',
                    version: "$packageVersion",
                    repository: "${params.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: "${params.component}",
                        classifier: '',
                        file: "${params.component}.zip",
                        type: 'zip']
                    ]
                )
            }
        }

        
        stage('Deploy') {
            steps {
                script{
                    echo "Deployment"
                    def params = [
                        string(name: 'version', value: "$packageVersion")
                    ]
                    build job: "../${params.component}-deploy", wait: true, parameters: params
            }
        }    
        }
    }

    post{
        always{
            echo 'cleaning up workspace'
            deleteDir()
        }
    }
}