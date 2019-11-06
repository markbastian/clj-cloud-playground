lein uberjar
$(aws ecr get-login --no-include-email --region us-east-1)
docker build -t clj-cloud-playground .
docker tag clj-cloud-playground:latest XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground:latest
docker push XXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com/clj-cloud-playground:latest