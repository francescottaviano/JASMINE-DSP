docker build -t jasmine-kafka-streams .
docker tag jasmine-kafka-streams zed25/jasmine-kafka-streams:latest
docker push zed25/jasmine-kafka-streams:latest
