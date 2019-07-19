(defproject clj-cloud-playground "0.1.0-SNAPSHOT"
  :description "This project aims to walk through the steps required to deploy a Clojure application to the cloud."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-time "0.15.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [compojure "1.6.1"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [org.immutant/web "2.1.10"]
                 [integrant "0.7.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [nrepl "0.6.0"]
                 [nrepl/drawbridge "0.2.0"]
                 ;[ring "1.7.1"]
                 ;[ring/ring-defaults "0.3.2"]
                 ]

  :plugins [[lein-uberwar "0.2.1"]
            ;https://github.com/weavejester/lein-beanstalk
            [lein-beanstalk "0.2.7"]
            ;https://github.com/juxt/lein-dockerstalk
            ;[juxt/lein-dockerstalk "0.1.0"]
            ;https://github.com/zombofrog/lein-aws-beanstalk
            ;[lein-aws-beanstalk "0.2.82"]
            ;[lein-zip "0.1.1"]
            ]

  :main clj-cloud-playground.core

  :zip ["Dockerfile" "target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar"]

  ;For lein uberwar
  :uberwar {:handler clj-cloud-playground.core/app}

  ;For lein beanstalk
  :ring {:handler clj-cloud-playground.core/app}

  :profiles {:uberjar {:aot :all}}

  :repl-options {:init-ns clj-cloud-playground.core}

  :aws
  {:beanstalk
   {:region     "us-east-1"
    :stack-name "64bit Amazon Linux 2018.03 v3.1.6 running Tomcat 8.5 Java 8"
    :environments
                [{:name    "development"
                  :options {"aws:autoscaling:asg"
                            {"MinSize" "1" "MaxSize" "1"}
                            "aws:autoscaling:launchconfiguration"
                            {"InstanceType" "t2.nano"}}}]}}
  )
