const KafkaClient = require('kafka-node').KafkaClient;
const Producer = require('kafka-node').Producer;
const config = require('./config');
const fs = require('fs');
const csv = require('csv-parser');
const sleep = require('system-sleep');

let firstTimestamp;
let lastTimestamp;

String.prototype.toHHMMSS = function () {
    const sec_num = parseInt(this, 10); // don't forget the second param
    let hours = Math.floor(sec_num / 3600);
    let minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    let seconds = sec_num - (hours * 3600) - (minutes * 60);

    if (hours < 10)
        hours = "0" + hours;
    if (minutes < 10)
        minutes = "0" + minutes;
    if (seconds < 10)
        seconds = "0" + seconds;
    return hours + ':' + minutes + ':' + seconds;
};

function run() {
    try {
        const client = new KafkaClient({kafkaHost: config.kafka_server.local});
        const producer = new Producer(client, {partitionerType: 2});

        producer.on('ready', async function () {
            fs.createReadStream(config.input_file_path)
                .pipe(csv())
                .on('data', function (data) {
                    try {
                        //console.log(data);

                        if (!firstTimestamp) {
                            firstTimestamp = data.createDate;
                            lastTimestamp = data.createDate;
                        }

                        let delta = (data.createDate - lastTimestamp) / config.time_compression;
                        lastTimestamp = data.createDate;

                        console.log(String(data.createDate - firstTimestamp).toHHMMSS());

                        if (delta > 0)
                            sleep(delta * 1000);

                        let payloads = [
                            {
                                topic: config.kafka_topic,
                                messages: JSON.stringify(data)
                            }
                        ];
                        producer.send(payloads, (err, data) => {
                            /*if (err) {
                                console.log('[kafka-producer -> '+config.kafka_topic+']: broker update failed');
                            } else {
                                console.log('[kafka-producer -> '+config.kafka_topic+']: broker update success');
                            }*/
                        });


                    }
                    catch (err) {
                        //error handler
                    }
                })
                .on('end', function () {
                    //some final operation
                    process.exit();
                });
        });

        producer.on('error', function (err) {
            console.log(err);
            console.log('[kafka-producer -> ' + config.kafka_topic + ']: connection error');
            throw err;
        });
    }
    catch (e) {
        console.log(e);
    }
}

run();