# PAM-ClientApp

The client application contains 3 major routes which are configured as following:

### 1. Kafka Consumer

From [ kafka | consume topic ]&nbsp;&rarr;&nbsp;To [ Log ]

- This route uses a kafka camel component to read from a kafka topic and print it to the console log for demo purposes.
- from("kafka:*topic?brokers=url:port*") creates a consumer endpoint.

### 2. PostgreSQL Read, Write

From [ direct ]&nbsp;&rarr;&nbsp;(SQL Read)&nbsp;&rarr;&nbsp; To [ JDBC ]&nbsp;&rarr;&nbsp; To [Log]&nbsp;&rarr;&nbsp;(SQL Write)&nbsp;&rarr;&nbsp; To [ JDBC ]&nbsp;&rarr;&nbsp;To [Log]

- Camel provides a jdbc component which can be used to pass SQL queries to a Database.
- PostgreSQL connector is used here to create a Datasource which is passed as an argument to the jdbc component. The SQL queries are passed in the message body.

### 3. Starting a Workflow Instance on a Kie-server
 
From [ direct ]&nbsp;&rarr;&nbsp;(Set Headers and Body)&nbsp;&rarr;&nbsp;To [HTTP]

- HTTP component provided by Camel can be leveraged here to make HTTP calls to a REST Api.
- Here a route is configured to call the Kie-server running on the local machine. It sends an HTTP Post request to the endpoint specified in the component.