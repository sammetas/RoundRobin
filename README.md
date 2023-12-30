This program will take the requests and will route through the available instances of SimpleAPI .
It will equally distribute the load to all the instances based on their availability.
# To start the round robin
 java -Dservver.port=8090 -jar RoundRobin-0.0.1-SNAPSHOT.jar
