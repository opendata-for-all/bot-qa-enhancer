rm -R EnhancedBot
cp -R Bot EnhancedBot
cp -R src/main/resources EnhancedBot/src/main
mvn clean compile
mvn exec:java -Dexec.mainClass="Visitor"
