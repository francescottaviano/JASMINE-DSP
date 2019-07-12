FROM ubuntu:16.04

ENV SCALA_VERSION 2.11
ENV KAFKA_VERSION 2.2.0
ENV KAFKA_HOME /usr/local/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"
ADD scripts/start_kafka.sh /usr/bin/start_kafka.sh

# install java + others
RUN apt-get update && apt-get install -y \
  wget \
  openjdk-8-jdk

# install kafka
RUN wget http://apache.cs.utah.edu/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
  tar -xzf kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
  mv kafka_"$SCALA_VERSION"-"$KAFKA_VERSION" $KAFKA_HOME

# change start_kafka permissions
RUN chmod 744 /usr/bin/start_kafka.sh

#expose port 9092 to external world
EXPOSE 9092

CMD ["/usr/bin/start_kafka.sh"]
