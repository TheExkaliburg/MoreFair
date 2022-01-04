mvn clean package -Dmaven.test.skip=true

java -jar -Dspring.profiles.active=prod -Dspring.datasource.password=pw morefair-1.0.jar