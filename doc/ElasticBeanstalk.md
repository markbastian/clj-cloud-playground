## Run with Amazon Elastic Beanstalk
[Amazon Elasticbeanstalk](https://aws.amazon.com/elasticbeanstalk/) with Clojure has a few options:
1. Run as a standalone uberjar app
1. Run as an uberwar using Apache Tomcat
1. Run as a standalone uberjar app in a Docker container

If you need a remote repl (e.g. nREPL) the only solution currently working is the standalone uberjar app.

### EBS with a Standalone Java Jar App
TLDR; Run `lein deploy-ebs-java` to see this in action and inspect the alias in the project.clj file for details.

This option will create an uberjar application, package that application in a zip archive, and deploy the archive to EBS.

If you want to do this, do the following:
 1. Add [lein dockerstalk](https://github.com/juxt/lein-dockerstalk) and [lein zip](https://github.com/mrmcc3/lein-zip) to your plugins.
 1. Build your app with `lein uberjar`.
 1. Add the files to be archived as a :zip entry in your project file, like so: `:zip ["target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]`.
 1. Create the archive with `lein with-profile +ebs-java zip`.
 1. Deploy the application with `lein with-profile +ebs-java dockerstalk deploy development target/clj-cloud-playground-0.1.0-SNAPSHOT.zip`. 
 
To connect a REPL:
 1. Go to the EC2 Dashboard and located your instance
 1. Select the instance
 1. Select the instance security group (At the lower part of the screen)
 1. Click the *Inbound* rules tab
 1. Add a rule with the following params:
    1. Type "Custom TCP Rule"
    1. Protocol: TCP
    1. Port Range: Your REPL port (e.g. 3001)
    1. Source: Choose what you want. I use My IP. **TODO** Can I do anywhere with the assumption that a key is required?

### EBS with Docker
TLDR; Run `lein deploy-ebs-docker` to see this in action and inspect the alias in the project.clj file for details.

This option will create an uberjar application, package that application with your Dockerfile in a zip archive, and deploy the archive to EBS.

If you want to do this, do the following:
 1. Add [lein dockerstalk](https://github.com/juxt/lein-dockerstalk) and [lein zip](https://github.com/mrmcc3/lein-zip) to your plugins.
 1. Build your app with `lein uberjar`.
 1. Add the files to be archived as a :zip entry in your project file, like so: `:zip ["Dockerfile" "target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]`.
 1. Create the archive with `lein with-profile +ebs-docker zip`.
 1. Deploy the application with `lein with-profile +ebs-docker dockerstalk deploy development target/clj-cloud-playground-0.1.0-SNAPSHOT.zip`. 

### EBS with Tomcat
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

### Tips

Get your deployment status with the command `lein beanstalk info` and remove your application with `lein beanstalk terminate $environment` (e.g. development).

Terminate the deployment with the beanstalk plugin a la `lein beanstalk terminate development`. Note that dockerstalk uses beanstalk so you just use the beanstalk plugin for application termination.

##### TODOs
Figuring out how to connect an nNREPL session to an EBS instance has not been trivial. Here are a few things I'm investigating that will be absorbed into the correct documentation when I get everything working.

* [Does this work?](https://superuser.com/questions/1417848/ssh-tunnel-with-eb-cli-elastic-beanstalk-aws?rq=1)
* [Or this](https://stackoverflow.com/questions/4742478/ssh-to-elastic-beanstalk-instance)
`eb ssh --custom 'ssh -i ~/.ssh/keyfile.pem -L 3001:localhost:3001'`