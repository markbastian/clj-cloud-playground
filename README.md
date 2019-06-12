# clj-cloud-playground

A Clojure demonstration project designed to enable you to rapidly spin up and experiment with a Clojure application using Docker, Containerization, and the like.

## Usage

To run locally, do one of:
* Launch a REPL and evaluate `(clj-cloud-playground.core/start)`
* lein run
* lein uberjar, java -jar target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar

To run using Docker:
1. Build the standalone app with `lein uberjar`
1. Build the image using `docker build --tag=clj-cloud-playground .`
1. Run the app using `docker run -p 3000:3000 clj-cloud-playground`. This will run you app in a local container that exposes port 3000 to port 3000 locally. You might also try these invocations:
   * `docker run -e NREPL_PORT=3001 -p 80:3000 -p 3001:3001 clj-cloud-playground`: Set the `nrepl-port` variable to to 3001 and map the container's port 3000 to local port 80. This allows you to connect to your running image and do interactive development.
   * `docker run -e IS_PRODUCTION=true -p 3000:3000 clj-cloud-playground`: Set the `is-production` environment variable to true so you can modify your internal app as appropriate.

Let's say you want to transfer your image to an EC2 instance and not use DockerHub or any other sort of repo. Assuming you have an EC2 instance running and all the correct keys and permissions set up, you can run your app in the cloud by doing the following:
1. On you local machine, run `docker save -o clj-cloud-playground.tar clj-cloud-playground`. This will package up your app as a single archive.
1. On your local machine, run `scp -i ~/.ssh/mykey.pem clj-cloud-playground.tar ec2-user@XXXXXXXXXX.amazonaws.com:/home/ec2-user/` to copy the file to your remote machine. You must fill in the right values for your ssh key and host.
1. On the remote machine, run `docker load -i clj-cloud-playground.tar` to unpack and load your app.
1. On the remote machine, run the app using any of the same commands as you did above.

## Power Overwhelming

## TODO
Add directions for deployment with Elastic Beanstalk

Add directions for deployment with 

Figure out how to get the drawbridge middleware working.

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
