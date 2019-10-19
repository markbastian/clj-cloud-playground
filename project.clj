(defproject clj-cloud-playground "0.1.0-SNAPSHOT"
  :description "This project aims to walk through the steps required to deploy a Clojure application to the cloud."
  :url "https://github.com/markbastian/clj-cloud-playground"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [org.immutant/web "2.1.10"]
                 [integrant "0.7.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [ring/ring-defaults "0.3.2"]
                 [nrepl "0.6.0"]
                 [nrepl/drawbridge "0.2.1"]
                 [metosin/reitit "0.3.10"]
                 [markbastian/partsbin "0.1.0"]]

  :plugins [[lein-uberwar "0.2.1"]
            [lein-beanstalk "0.2.7"]
            [juxt/lein-dockerstalk "0.1.0"]
            [lein-zip "0.1.1"]
            [nrepl/drawbridge "0.2.0"]]

  :main clj-cloud-playground.core

  :aliases {"deploy-ebs-tomcat" ["do"
                                 ["clean"]
                                 ["with-profile" "+ebs-tomcat" "uberwar"]
                                 ["with-profile" "+ebs-tomcat" "beanstalk" "deploy" "development"]]
            "deploy-ebs-docker" ["do"
                                 ["clean"]
                                 ["uberjar"]
                                 ["with-profile" "+ebs-docker" "zip"]
                                 ["with-profile" "+ebs-docker" "dockerstalk" "deploy" "development" "target/clj-cloud-playground-0.1.0-SNAPSHOT.zip"]]}

  ;https://docs.aws.amazon.com/en_pv/elasticbeanstalk/latest/platforms/platforms-supported.html
  :profiles {:uberjar    {:aot :all}
             :ebs-tomcat {:uberwar {:handler clj-cloud-playground.core/app}
                          :ring    {:handler clj-cloud-playground.core/app}
                          :aws     {:beanstalk
                                    {:region       "us-east-1"
                                     :stack-name   "64bit Amazon Linux 2018.03 v3.2.2 running Tomcat 8.5 Java 8"
                                     ;:stack-name   "Tomcat 8.5 with Java 8 running on 64bit Amazon Linux"
                                     :s3-bucket    "clj-cloud-playground"
                                     :environments [{:name    "development"
                                                     :options {"aws:autoscaling:asg"
                                                               {"MinSize" "1" "MaxSize" "1"}
                                                               "aws:autoscaling:launchconfiguration"
                                                               {"InstanceType" "t2.nano"}}}]}}}
             :ebs-docker {:zip ["Dockerfile" "target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]
                          :aws {:beanstalk
                                {:region       "us-east-1"
                                 :stack-name   "Docker running on 64bit Amazon Linux/2.13.0"
                                 ;:stack-name   "64bit Amazon Linux 2018.03 v2.12.17 running Docker 18.06.1-ce"
                                 ;:stack-name   "64bit Amazon Linux 2018.03 v2.12.14 running Docker 18.06.1-ce"
                                 :s3-bucket    "clj-cloud-playground"
                                 :environments [{:name    "development"
                                                 :options {"aws:autoscaling:asg"
                                                           {"MinSize" "1" "MaxSize" "1"}
                                                           "aws:autoscaling:launchconfiguration"
                                                           {"InstanceType" "t2.nano"}}
                                                 ;This may not work since EBS appears to only open one port. Drawbridge may be the only option.
                                                 ;:env {"NREPL_PORT" "3001"}
                                                 }]}}}}

  :repl-options {:init-ns clj-cloud-playground.core})
