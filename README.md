# bot-qa-enhancer
This project allows a chatbot to be extended with a default fallback state that makes queries to a language model to ask questions on a given .csv/.txt file

## Usage

0. Modify ```src/main/resources/configuration.properties``` if you need it, and make sure that the bot folder name coincides with the ones in ```runVisitor.sh``` and ```runBot.sh```.
1. Open a new terminal in the root directory of the project and run ```./runVisitor.sh``` to generate the enhanced chatbot.
2. Then, run ```./runBot.sh``` to run the new chatbot together with the flask server running the language model. Make sure that the server port coincides with the one specified in ```src/main/resources/configuration.properties```.
