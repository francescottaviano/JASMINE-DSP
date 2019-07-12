#!/bin/bash

#Check if Kafka host env variable is setted
if [ ! -z "$KAFKA_HOST" ]; then
    #replace kafka_host config in config file
    echo "advertised host: $KAFKA_HOST --------> I'm goning to set it in config file"
    #use sed to find config variable field and replace its value
    #s/#(variable_name)=(.variable_value)/\change variable_value/g in file
    sed -r -i "s/#(advertised.host.name)=(.*)/\1=$KAFKA_HOST/g" $KAFKA_HOME/config/server.properties
fi

#Check if Kafka port env variable is setted
if [ ! -z "$KAFKA_PORT" ]; then
    #replace kafka_port config in config file
    echo "advertised port: $KAFKA_PORT --------> I'm goning to set it in config file"
    #use sed to find config variable field and replace its value
    #sed -r -i "s/#(advertised.port)=(.*)/\1=$KAFKA_PORT/g" $KAFKA_HOME/config/server.properties
fi

if [ ! -z "$KAFKA_HOST" && ! -z "$KAFKA_PORT"]; then
    #replace kafka_port config in config file
    echo "advertised host: $KAFKA_HOST, advertised port: $KAFKA_PORT --------> I'm goning to set them in config file"
    #use sed to find config variable field and replace its value
    sed -r -i "s/#(advertised.listeners)=(.*)/\1=PLAINTEXT://$KAFKA_HOST:$KAFKA_PORT/g" $KAFKA_HOME/config/server.properties
    sed -r -i "s/#(listeners)=(.*)/\1=PLAINTEXT://$KAFKA_HOST:$KAFKA_PORT/g" $KAFKA_HOME/config/server.properties
fi

#Check if brocker id env variable is setted
if [ ! -z "$KAFKA_BROKER_ID" ]; then
    #set brocker_id config in config file
    echo "broker id: $KAFKA_BROKER_ID --------> I'm goning to set it in config file"
    sed -r -i "s/(broker.id)=(.*)/\1=$KAFKA_BROKER_ID/g" $KAFKA_HOME/config/server.properties
fi

# Set the zookeeper connect from the environment variable
if [ ! -z "$ZOOKEEPER_CONNECT" ]; then
    echo "zookeeper connect: $ZOOKEEPER_CONNECT --------> I'm goning to set it in config file"
    sed -r -i "s/(zookeeper.connect)=(.*)/\1=$ZOOKEEPER_CONNECT/g" $KAFKA_HOME/config/server.properties
fi

# Allow specification of log retention policies
if [ ! -z "$LOG_RETENTION_HOURS" ]; then
    echo "log retention hours: $LOG_RETENTION_HOURS --------> I'm goning to set it in config file"
    sed -r -i "s/(log.retention.hours)=(.*)/\1=$LOG_RETENTION_HOURS/g" $KAFKA_HOME/config/server.properties
fi


if [ ! -z "$LOG_RETENTION_BYTES" ]; then
    echo "log retention bytes: $LOG_RETENTION_BYTES --------> I'm goning to set it in config file"
    sed -r -i "s/#(log.retention.bytes)=(.*)/\1=$LOG_RETENTION_BYTES/g" $KAFKA_HOME/config/server.properties
fi

# Configure the default number of log partitions per topic
if [ ! -z "$NUM_PARTITIONS" ]; then
    echo "default number of partition: $NUM_PARTITIONS --------> I'm goning to set it in config file"
    sed -r -i "s/(num.partitions)=(.*)/\1=$NUM_PARTITIONS/g" $KAFKA_HOME/config/server.properties
fi

# Enable/disable auto creation of topics
if [ ! -z "$AUTO_CREATE_TOPICS" ]; then
    echo "auto.create.topics.enable: $AUTO_CREATE_TOPICS --------> I'm goning to set it in config file"
    echo "auto.create.topics.enable=$AUTO_CREATE_TOPICS" >> $KAFKA_HOME/config/server.properties
fi

# Configure the default number of log partitions per topic
if [ ! -z "$DELETE_TOPIC_ENABLE" ]; then
    echo "delete.topic.enable: $DELETE_TOPIC_ENABLE --------> I'm goning to set it in config file"
    sed -r -i "s/(delete.topic.enable)=(.*)/\1=$DELETE_TOPIC_ENABLE/g" $KAFKA_HOME/config/server.properties
fi

# Run Kafka
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties

if [ ! -z "$INITIAL_TOPICS" ]; then
    echo "initial topics: $INITIAL_TOPICS --------> I'm going to create these topics"
    $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $ZOOKEEPER_CONNECT --replication-factor 1 --partitions 1 --topic $INITIAL_TOPICS
fi
