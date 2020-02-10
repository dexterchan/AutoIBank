# AutoIBank

##Business case
Matching of issuer and investor in a bond market <br>
Can be modelled as following math problem<br>
find the max Probably(investor allocation | Secondary market, $ issuer want, tenor) with certain investor allocation

*Applying Bayne Inference, with historical data, we would have <br>
1. P(Secondary market) ... to be ignored.... <br>
P(secondary market) is the same for all investor allocation. we want to compare different investor allocation resulting max probability

2. Likelihood : P(Secondary market, $ issuer want | investor allocation, tenor) <br>
>Posterier distribution: https://en.wikipedia.org/wiki/Posterior_distribution
key problem... complicated...

3. Prior probability: P(investor allocation|tenor) <br>
calculate from investor past trading activities (only Ask trade)

>Reference: https://towardsdatascience.com/probability-concepts-explained-bayesian-inference-for-parameter-estimation-90e8930e5348

###Prior probability:
Data needed
Investor past trading activities
parameterized investor behavior as Gaussian Distribution
For tenor, find the mean, variance of notional of trades


##Run the application
### Direct Runner
Build command:
```
gradle -Pdirect clean build
```


###Kafka dependency
#### Modules:
SecondaryMarketGateway - Publisher
SecondaryMarketAnalysis - Subscriber

Installation of Kafka
https://kafka.apache.org/quickstart
#### Start Kafka
````
rm -Rf /tmp/zookeeper
rm -Rf /tmp/kafka-logs
zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties & 
sleep 10
kafka-server-start.sh $KAFKA_HOME/config/server.properties 
````
#### Stop Kafka
````
zookeeper-server-stop.sh  & 
sleep 2
kafka-server-stop.sh 
sleep 2
rm -Rf /tmp/zookeeper
rm -Rf /tmp/kafka-logs
````

#### Create of topic - bondtrade
````
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic bondtrade
````

#### Health check
````
kafkacat -b localhost:9092 -P -t bondtrade 
kafkacat -b localhost:9092 -C -t bondtrade
````

#### Gateway Run
````
java -jar SecondaryMarketGateway/build/libs/SecondaryMarketGateway-1.0-SNAPSHOT.jar -k localhost:9092 -t bondtrade
````
#### Analysis Run
````
java -jar SecondaryMarketAnalysis/build/libs/SecondaryMarketAnalysis-1.0-SNAPSHOT.jar 
````