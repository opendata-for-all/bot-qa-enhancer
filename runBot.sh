flask run --port=5002 &
cd EnhancedBot
mvn clean compile
mvn exec:java -Dexec.mainClass="com.xatkit.example.GreetingsBot"
