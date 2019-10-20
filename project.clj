(defproject clj-cloud-playground "0.1.0-SNAPSHOT"
  :description "This project aims to walk through the steps required to deploy a Clojure application to the cloud."
  :url "https://github.com/markbastian/clj-cloud-playground"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [integrant "0.7.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [ring/ring-defaults "0.3.2"]
                 [nrepl "0.6.0"]
                 [nrepl/drawbridge "0.2.1"]
                 [metosin/reitit "0.3.10"]
                 [markbastian/partsbin "0.1.1"]
                 ;Experimenting
                 [com.cognitect.aws/api "0.8.352"]
                 [com.cognitect.aws/endpoints "1.1.11.655"]
                 [com.cognitect.aws/elasticbeanstalk "746.2.533.0"]]

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

  ;(require '[cognitect.aws.client.api :as aws])
  ;(def ebs (aws/client {:api :elasticbeanstalk}))

  ;https://docs.aws.amazon.com/en_pv/elasticbeanstalk/latest/platforms/platforms-supported.html
  :profiles {:uberjar    {:aot :all}
             :ebs-tomcat {:uberwar      {:handler clj-cloud-playground.core/app}
                          :ring         {:handler clj-cloud-playground.core/app}
                          :aws          {:beanstalk
                                         {:region       "us-east-1"
                                          :stack-name   "64bit Amazon Linux 2018.03 v3.3.0 running Tomcat 8.5 Java 8"
                                          :s3-bucket    "clj-cloud-playground"
                                          :environments [{:name    "development"
                                                          :options {"aws:autoscaling:asg"
                                                                    {"MinSize" "1" "MaxSize" "1"}
                                                                    "aws:autoscaling:launchconfiguration"
                                                                    {"InstanceType" "t2.nano"}}}]}}
                          :dependencies [[commons-fileupload/commons-fileupload "1.4"]
                                         [javax.xml.bind/jaxb-api "2.4.0-b180830.0359"]]}
             :ebs-docker {:zip ["Dockerfile" "target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]
                          :aws {:beanstalk
                                {:region       "us-east-1"
                                 :stack-name   "64bit Amazon Linux 2018.03 v2.13.0 running Docker 18.06.1-ce"
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
