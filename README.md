# AutoIBank

## Our business problem
An automation to match investors and Bond issuers in an automated platform. <br>
In short, the automation models the Bond Issuance operation with Sales, Traders of a typical Investment Bank <br>
It speeds up the deal between Banking and Market with minimum human intervention. <br>
Also, it enforces the segregation duty and best execution to both investors and issuers. <br>
It ensures: <br>
1. system act neutrally on the benefit between Banking and Market in Primary market <br>
2. referencing secondary market activities, source right price for issuers reference <br>
3. project the potential bond sales figure for different Bond issue price reflected from historical secondary market behavior<br>
4. find potential investors in the Market behind of the Chinese wall of Banking <br>
 
### Workflow diagram
to be added

## How to model the workflow?
Any system implementation is a solution to mathematic workflow. <br>
This matching workflow can be modelled with Bayne Inference as follow <br>


*Applying Bayne Inference, with historical data, we would have <br>
find the max Probably(investor allocation | Secondary market, $ issuer issuance, tenor) with certain investor allocation <br>
= P($ issuer issuance | investor allocation, tenor) * P(investor allocation|tenor) / P(Secondary market)
1. P(Secondary market) ... to be ignored.... <br>
P(secondary market) is the same for all investor allocation. <br>
As we want to compare different investor allocation to find max probability, we can ignore it.

2. Likelihood : P($ issuer issuance | investor allocation, tenor) <br>
>Posterier distribution: https://en.wikipedia.org/wiki/Posterior_distribution <br>
to be addressed <br>

3. Prior probability: P(investor allocation|tenor) <br>
calculate from investor historical trading activities (only Ask trade)

>Reference: https://towardsdatascience.com/probability-concepts-explained-bayesian-inference-for-parameter-estimation-90e8930e5348

### Prior probability implementation:
Data needed <br>
Investor historical trading activities <br>
parameterized investor behavior as Gaussian Distribution <br>
For tenor, find the mean, variance of notional of trades <br>


## System run
### How to run the secondary market analysis component?
Implementation is with Kafka and Apache Dataflow. <br>
Now, we run a dummy trade generator of 10 investors for testing purpose

#### Direct Runner
Build command:
```
gradle -Pdirect clean build
```


#### Kafka dependency
##### Modules:
SecondaryMarketGateway - Publisher
SecondaryMarketAnalysis - Subscriber

Installation of Kafka
https://kafka.apache.org/quickstart
##### Start Kafka
````
rm -Rf /tmp/zookeeper
rm -Rf /tmp/kafka-logs
zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties & 
sleep 10
kafka-server-start.sh $KAFKA_HOME/config/server.properties 
````
##### Stop Kafka
````
zookeeper-server-stop.sh  & 
sleep 2
kafka-server-stop.sh 
sleep 2
rm -Rf /tmp/zookeeper
rm -Rf /tmp/kafka-logs
````

##### Create of topic - bondtrade
````
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic bondtrade
````

##### Health check
````
kafkacat -b localhost:9092 -P -t bondtrade 
kafkacat -b localhost:9092 -C -t bondtrade
````

##### Gateway Run
````
java -jar SecondaryMarketGateway/build/libs/SecondaryMarketGateway-1.0-SNAPSHOT.jar -k localhost:9092 -t bondtrade
````
##### Analysis Run
````
java -jar SecondaryMarketAnalysis/build/libs/SecondaryMarketAnalysis-1.0-SNAPSHOT.jar 
````