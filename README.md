# clj-cloud-playground

A Clojure demonstration project designed to enable you to rapidly spin up and experiment with a Clojure application using Docker, Containerization, and the like.

## Usage

#### Run Locally
To run locally, do one of:
* Launch a REPL and evaluate `(clj-cloud-playground.core/start)`
* lein run
* lein uberjar, java -jar target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar

#### Run with Docker
To run using Docker:
1. Build the standalone app with `lein uberjar`
1. Build the image using `docker build --tag=clj-cloud-playground .`
1. Run the app using `docker run -p 3000:3000 clj-cloud-playground`. This will run you app in a local container that exposes port 3000 to port 3000 locally. You might also try these invocations:
   * `docker run -e NREPL_PORT=3001 -p 80:3000 -p 3001:3001 clj-cloud-playground`: Sets the `nrepl-port` variable to to 3001 and map the container's port 3000 to local port 80. This allows you to connect to your running image and do interactive development.
   * `docker run -e IS_PRODUCTION=true -p 3000:3000 clj-cloud-playground`: Sets the `is-production` environment variable to true so you can modify your internal app as appropriate.

##### Direct Transfer of Docker Image to EC2
Let's say you want to transfer your image to an EC2 instance and not use DockerHub or any other sort of repo. Assuming you have an EC2 instance running and all the correct keys and permissions set up, you can run your app in the cloud by doing the following:
1. On you local machine, run `docker save -o clj-cloud-playground.tar clj-cloud-playground`. This will package up your app as a single archive.
1. On your local machine, run `scp -i ~/.ssh/mykey.pem clj-cloud-playground.tar ec2-user@XXXXXXXXXX.amazonaws.com:/home/ec2-user/` to copy the file to your remote machine. You must fill in the right values for your ssh key and host.
1. On the remote machine, run `docker load -i clj-cloud-playground.tar` to unpack and load your app.
1. On the remote machine, run the app using any of the same commands as you did above.

##### Transfer Docker Image Using ECR
To use Amazon Elastic Container Registry (ECR) to [push an image](https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html):
1. Using the AWS Console, create a repository (e.g. clj-cloud-playground). It will create a repository with a URI like `XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground`.
1. Click the button 'View push commands' at the top right to get the command sequence needed to upload your image.
1. Once you've done the above your image should be hosted in the repo.
1. Now, ssh into the EC2 instance you will be hosting your docker image from. Ensure docker is set up and running by following [these instructions](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/docker-basics.html#install_docker).
1. Ensure you are logged in to ECR by following [these instructions](https://docs.aws.amazon.com/AmazonECR/latest/userguide/Registries.html#registry_auth).
1. Launch the container with `docker run -e NREPL_PORT=3001 -p 80:3000 -p 3001:3001 XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground &`. Note that the & will run the process in the background.

To stop your app, use `docker ps` to identify the container id then `docker stop id` (id is the container you just identified) to stop the container.

#### Run with Amazon Elastic Beanstalk
[Amazon Elasticbeanstalk](https://aws.amazon.com/elasticbeanstalk/) with Clojure has two options:
1. Run as an uberwar using Apache Tomcat
1. Run as a standalone uberjar app

##### EBS with Tomcat
TLDR; Run `lein deploy-ebs-tomcat` to see this in action and inspect the alias in the project.clj file for details.

This project uses [lein-uberwar](https://github.com/luminus-framework/lein-uberwar) and [lein-beanstalk](https://github.com/weavejester/lein-beanstalk) for deployment. [lein-beanstalk](https://github.com/weavejester/lein-beanstalk) is a bit dated, but it works. Other forks include [lein-elastic-beanstalk](https://github.com/ktgit/lein-elastic-beanstalk) and [lein-aws-beanstalk](https://github.com/zombofrog/lein-aws-beanstalk).

Since configuration here will conflict with configuration for deployment as a docker container, all settings specific to this deploy are contained in the :ebs-tomcat profile of the project.clj file. Note that any commands below preceded by `with-profile +ebs-tomcat` can omit those terms if you don't use a profile.

To deploy your war app with Tomcat do the following:
1. Add  [lein-uberwar](https://github.com/luminus-framework/lein-uberwar) to your project.clj plugins vector.
1. Specify the application entry point for your uberjar in your project file (e.g. `:uberwar {:handler clj-cloud-playground.core/app}` in the :ebs-tomcat profile).
1. Run `lein with-profile +ebs-tomcat uberwar` to create your war file.
1. Add [lein-beanstalk](https://github.com/weavejester/lein-beanstalk) to your project.clj plugins vector.
1. Configure your aws credentials in your project.clj file as described [here](https://github.com/weavejester/lein-beanstalk#basic-configuration).
1. Specify the application entry point for the beanstalk plugin in your project file (e.g. `:ring {:handler clj-cloud-playground.core/app}` in the :ebs-tomcat profile).
1. Configure your aws environment as shown in the project file or take a look at [this useful blog post](https://victorjcheng.wordpress.com/2016/02/02/deploying-a-clojure-app-using-elastic-beanstalk/). One thing you must get right is the :stack-name field. You can get a list of current stack options by invoking the command `aws elasticbeanstalk list-available-solution-stacks` and looking for the desired Tomcat instance. Note that if you read the above blog post the stack name there is out of date. This project uses "64bit Amazon Linux 2018.03 v3.1.6 running Tomcat 8.5 Java 8". Also, the blog post indicates needed a VPC for a t2.nano instance. This did not seem to be the case for me.
1. Run `lein with-profile +ebs-tomcat beanstalk deploy $environment` where $environment is the environment specified in your project. For example, in this project the command is `lein with-profile +ebs-tomcat beanstalk deploy development`. In this project, this key can be found at `(get-in project [:profiles :ebs-tomcat :aws :beanstalk :environments 0 :name])`. If you aren't using a profile it would be found at `(get-in project [:aws :beanstalk :environments 0 :name])`. 

This should successfully launch your service. Your console should look something like:

```
Creating 'development' environment (this may take several minutes)
.................................................... Done
Environment deployed at: clj-cloud-playground-development.region.elasticbeanstalk.com
```

You can get your deployment status with the command `lein beanstalk info` and remove your application with `lein beanstalk terminate $environment` (e.g. development).

##### EBS with Docker
TLDR; Run `lein deploy-ebs-docker` to see this in action and inspect the alias in the project.clj file for details.

This option will create an uberjar application, package that application with your Dockerfile in a zip archive, and deploy the archive to EBS.

If you want to do this, do the following:
 1. Add [lein dockerstalk](https://github.com/juxt/lein-dockerstalk) and [lein zip](https://github.com/mrmcc3/lein-zip) to your plugins.
 1. Build your app with `lein uberjar`.
 1. Add the files to be archived as a :zip entry in your project file, like so: `:zip ["Dockerfile" "target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]`.
 1. Create the archive with `lein with-profile +ebs-docker zip`.
 1. Deploy the application with `lein with-profile +ebs-docker dockerstalk deploy development target/clj-cloud-playground-0.1.0-SNAPSHOT.zip`. 

Terminated the deployment with the beanstalk plugin a la `lein beanstalk terminate development`. Note that dockerstalk uses beanstalk so you just use the beanstalk plugin for application termination.

## TODO

Figure out how to get the drawbridge middleware working.

## More Links

 * [Docker Basics](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/docker-basics.html)
 * [Running Docker on AWS from the ground up](https://www.ybrikman.com/writing/2015/11/11/running-docker-aws-ground-up/)
 * [Running Docker on AWS EC2](https://hackernoon.com/running-docker-on-aws-ec2-83a14b780c56)
 * [AWS Beanstalk, Docker and Clojure – The JUXT experience of deploying Docker containers through Beanstalk](https://juxt.pro/blog/posts/beanstalk.html)

## License

Copyright © 2019 Mark Bastian

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
