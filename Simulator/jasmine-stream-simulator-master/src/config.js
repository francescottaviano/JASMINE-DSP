module.exports = {
    kafka_topic: 'jasmine-input-topic',
    kafka_server: {
        local: 'localhost:9092',
        remote: 'www.kafka.simonemancini.eu:32093,www.kafka.simonemancini.eu:32094,www.kafka.simonemancini.eu:32095'
    },
    input_file_path: 'data/raw/final.csv',
    //input_file_path: 'data/raw/Comments_jan-apr2018.csv',
    time_compression: 10000
};