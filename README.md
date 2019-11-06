# clj-cloud-playground

A Clojure demonstration project designed to enable you to rapidly spin up and experiment with a Clojure application using Docker, Containerization, and the like.

## The Deployment Options Zoo

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

##### DockerHub
1. Create a repo at [Docker's Cloud Site](https://cloud.docker.com/). In this example my repo name is `markbastian/clj-cloud-playground`.
1. Build the image using `docker build --tag=$REPO:$TAG .` where `$REPO` and `$TAG` are your repository and tag names. In this case the exact command I am using is `docker build --tag=markbastian/clj-cloud-playground:latest .` I selected a tag of `latest` arbitrarily. It can be whatever you want.
1. Push the image using `docker push markbastian/clj-cloud-playground:latest`.
1. TODO: Deploy commands...

##### Direct Transfer of Docker Image to EC2
Let's say you want to transfer your image to an EC2 instance and not use DockerHub or any other sort of repo. Assuming you have an EC2 instance running and all the correct keys and permissions set up, you can run your app in the cloud by doing the following:
1. On you local machine, run `docker save -o clj-cloud-playground.tar clj-cloud-playground`. This will package up your app as a single archive.
1. On your local machine, run `scp -i ~/.ssh/mykey.pem clj-cloud-playground.tar ec2-user@XXXXXXXXXX.amazonaws.com:/home/ec2-user/` to copy the file to your remote machine. You must fill in the right values for your ssh key and host.
1. On the remote machine, run `docker load -i clj-cloud-playground.tar` to unpack and load your app.
1. On the remote machine, run the app using any of the same commands as you did above.

##### Transfer Docker Image Using ECR
To use Amazon Elastic Container Registry (ECR) to [push an image](https://docs.aws.amazon.com/en_pv/AmazonECR/latest/userguide/docker-push-ecr-image.html):
1. Using the AWS Console, create a repository (e.g. clj-cloud-playground). It will create a repository with a URI like `XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground`.
1. Click the button 'View push commands' at the top right to get the command sequence needed to upload your image. For this project, the commands will be along these lines:
   1. `$(aws ecr get-login --no-include-email --region us-east-1)`
   1. `docker build -t clj-cloud-playground .`
   1. `docker tag clj-cloud-playground:latest XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground:latest`
   1. `docker push XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground:latest` Note that this can take several minutes.
1. Once you've done the above your image should be hosted in the repo.
1. To launch an EC2 instance:
   1. Go to the [launch instance wizard](https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#LaunchInstanceWizard:)
   1. Optional: Select free tier only instances
   1. Choose an Amazon Machine Image (AMI). Choose the _Amazon Linux 2 AMI_ 64-bit (x86) option.
   1. Select a _t2.micro_ instance as it is free tier eligible.
   1. Specify a keypair and launch the instance. This could take several minutes.
   1. Once the instance launches you can see it in the [EC2 Instances Panel](https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Instances:sort=instanceId).
1. Now, ssh into the EC2 instance you will be hosting your docker image. The commands to do this can be found in the "Connect" item on the Actions menu in the [EC2 Instances Panel](https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Instances:sort=instanceId). Alternatively, there is a browser based ssh client in the connect options.
1. Ensure docker is set up and running by following [these instructions](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/docker-basics.html#install_docker).
   1. You must also configure your EC2 instance with permissions to access ECR. 
   1. Create a policy that allows access to your ECR repository.
   1. Create a role with the above policy attached.
   1. From the EC2 console, select your instance and choose Actions -> Instance Settings -> Attach/Replace IAM Role. Choose the role you created.
   1. Test your role out by executing `$(aws ecr get-login --no-include-email --region us-east-1)` from your EC2 instance.
   1. You can now run your image with any of the `docker run` commands from above, except you need to specify the ECR Image URI in the command. For example, `docker run -e NREPL_PORT=3001 -p 80:3000 -p 3001:3001 XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playgrod:latest
`.
   1. You now need to open port 80 for access from the outside. From the EC2 console, choose your image. At the bottom of the screen will be a link next to the Security Groups item for the image's security group. Click this.
   1. From the newly-opened security group tab, choose the inbound rules tab at the bottom of the screen and select edit.
   1. Choose "Add Rule" and select HTTP with a Source of Anywhere. If your image is running you can now navigate to its Public DNS entry (found on the EC2 page) and see your app.
   1. Finally, to allow REPL access add another custom security rule with the settings "Custom TCP", Protocol: TCP, Port Range: your nREPL port (e.g. 3001), Source: My IP (This is critical - don't make it anywhere), and Description: nREPL (or whatever).
   1. Connect to your repl using a remote connection with the IP of the EC2 instance and your configured REPL port.
1. Ensure you are logged in to ECR by following [these instructions](https://docs.aws.amazon.com/AmazonECR/latest/userguide/Registries.html#registry_auth).
1. Launch the container with `docker run -e NREPL_PORT=3001 -p 80:3000 -p 3001:3001 XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground &`. Note that the & will run the process in the background.

To stop your app, use `docker ps` to identify the container id then `docker stop id` (id is the container you just identified) to stop the container.

### Deployment Options
If you want an end-to-end solution, use one of the following solutions.

#### Heroku
[Heroku is probably the easiest way to create a complete app](doc/Heroku.md)

#### Amazon Elastic Beanstalk
[This is a fairly straightforward way to create a complete app](doc/ElasticBeanstalk.md)

##### Cofiguring Cloudwatch Logging from EBS
Follow [this guide](https://docs.aws.amazon.com/en_pv/AmazonECS/latest/developerguide/using_cloudwatch_logs.html)

###### Challenge - Solution Stack Drift
Sometimes when you go to revist a stack it will fail since the :stack-name entry may be deprecated. One good way to list the most recent options is to use the [Cognitect AWS API](https://github.com/cognitect-labs/aws-api). To do so, add the following dependencies to your project:
* `[com.cognitect.aws/api "0.8.352"]`
* `[com.cognitect.aws/endpoints "1.1.11.651"]`
* `[com.cognitect.aws/elasticbeanstalk "746.2.533.0"]`

Then, in a repl invoke the following and look for the stack you want:
```
(require '[cognitect.aws.client.api :as aws])
(def ebs (aws/client {:api :elasticbeanstalk}))
(:SolutionStacks (aws/invoke ebs {:op :ListAvailableSolutionStacks}))
```

TODO: Create a new beanstalk plugin using the Cognitect API

##### Configuring https with EBS
By default, EBS provides only insecure (http) connections. To configure your app for https follow the directions [here](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https.html) and [here](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https-elb.html). For the second link, use a "Classic Load Balancer" on step 5.

## Debugging with the REPL
By design, this project is meant to be live-coded with a REPL connection. The following methods for connection are pre-wired:
* If launched with a repl and the system is initialized with (start) you are already in a REPL session.
* If launched with the NREPL_PORT environment variable set and that port is open, you can connect to a REPL server on that port.
* ring-drawbridge is configured such that you can connect via `lein repl :connect http://localhost:3000/repl` where the correct server name and port are used (in this case locahost:3000).

## Troubleshooting
If the aws cli or other commands start to fail, there's a good chance you have a Python problem (who doesn't?). For example, this error started randomly happening on my development box:
```
iMac:clj-cloud-playground mbastian$ $(aws ecr get-login --no-include-email --region us-east-1)
Traceback (most recent call last):
  File "/usr/local/bin/aws", line 6, in <module>
    from aws.main import main
  File "/usr/local/lib/python3.7/site-packages/aws/main.py", line 23
    print '%(name)s: %(endpoint)s' % {
                                 ^
SyntaxError: invalid syntax
```
The best way to remedy this is with the [pipenv](https://pipenv.kennethreitz.org/en/latest/). I did the following to fix the above:
``` 
pipenv shell
pipenv install awscli
```
Once the above was done and everything worked in the Python virtual env.

## TODO

 * Better understand ring-middleware such that defaults work for all cases. Currently, `(wrap-defaults (assoc-in api-defaults [:responses :content-types] false))` handles content and the drawbridge connection correctly.
 * Maybe add security to ring-drawbridge. The goal of this project is not to show how to do everything, so this isn't necessarily a requirement for this project, but should absolutely be done in any real application.
 * Figure out port forwarding or some other solution for drawbridge+cursive connections (https://groups.google.com/forum/#!topic/cursive/7zethDTEIXo)
 * Convert this into Github pages
 * Develop new beanstalk plugin with the cognitect APIs

## More Links

 * [Docker Basics](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/docker-basics.html)
 * [Running Docker on AWS from the ground up](https://www.ybrikman.com/writing/2015/11/11/running-docker-aws-ground-up/)
 * [Running Docker on AWS EC2](https://hackernoon.com/running-docker-on-aws-ec2-83a14b780c56)
 * [AWS Beanstalk, Docker and Clojure – The JUXT experience of deploying Docker containers through Beanstalk](https://juxt.pro/blog/posts/beanstalk.html)
 * [Configuring HTTPS for Your Elastic Beanstalk Environment](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https.html)
 * [Configuring Your Elastic Beanstalk Environment's Load Balancer to Terminate HTTPS](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https-elb.html)
 * [Drawbridge - HTTP Transport for nREPL](https://github.com/nrepl/drawbridge)
 * [Live-Debugging Remote Clojure Apps with Drawbridge](https://devcenter.heroku.com/articles/debugging-clojure)
 * [Configuring the Reverse Proxy](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-nginx.html)
 * [How do I use my own security group for my load balancer when I deploy an AWS Elastic Beanstalk application?](https://aws.amazon.com/premiumsupport/knowledge-center/security-group-elastic-beanstalk/)
 * [Configuring AWS Elastic Beanstalk Environments](https://docs.aws.amazon.com/en_pv/elasticbeanstalk/latest/dg/customize-containers.html)
 * [Elastic Beanstalk without Elastic Load Balancer](https://stackoverflow.com/questions/8014046/elastic-beanstalk-without-elastic-load-balancer): Might this be the answer along with a correct security group?
 * [Deploying Spring Boot Jar Application on Beanstalk Java SE Platform](https://medium.com/@autumn.bom/deploying-spring-boot-jar-application-on-beanstalk-java-se-platform-45d8d04608ae). This could give some clues for java jar deployment.
 
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

[ECR_repo]: resources/ECR_repo.png "ECR Landing Page"