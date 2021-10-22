package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import com.mashape.unirest.http.Unirest;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.nio.charset.StandardCharsets;
import static java.util.Objects.isNull;

public class GreetingsBot {

    private static String qaModelServerUrl = "http://127.0.0.1:5002/";

    private static String setModelEndpoint = "set-model-text";

    private static String transformerEndpoint = "qa-text";

    private static String qaModelName = "ktrapeznikov/albert-xlarge-v2-squad-v2";

    private static String DEFAULT_FALLBACK_CORPUS_FILE = "civil_rights.txt";

    private static String defaultFallbackCorpus = "";

    private static boolean setModel() {
        JSONObject request = new JSONObject();
        request.put("modelName", qaModelName);
        try {
            JSONObject response = new JSONObject();
            response = Unirest.post(qaModelServerUrl + setModelEndpoint).header("Content-Type", "application/json").body(request).asJson().getBody().getObject();
        } catch (Exception e) {
            Log.error(e, "An error occurred while setting the model, see the attached exception");
            defaultFallbackCorpus = null;
            return false;
        }
        return true;
    }

    private static String processQA(String question) {
        JSONObject request = new JSONObject();
        request.put("question", question);
        request.put("corpus", defaultFallbackCorpus);
        request.put("modelName", qaModelName);
        JSONObject response = new JSONObject();
        try {
            response = Unirest.post(qaModelServerUrl + transformerEndpoint).header("Content-Type", "application/json").body(request).asJson().getBody().getObject();
            JSONObject modelResponse = response.getJSONObject(qaModelName);
            String answer = modelResponse.getString("answer");
            return answer;
        } catch (Exception e) {
            Log.error(e, "An error occurred while computing the answer, see the attached exception");
        }
        return null;
    }

    private static void setDefaultFallbackCorpus() {
        try (InputStream inputStream = GreetingsBot.class.getClassLoader().getResourceAsStream(DEFAULT_FALLBACK_CORPUS_FILE)) {
            if (isNull(inputStream)) {
                Log.error("Cannot find the file {0}, the default fallback state won't use a language model", DEFAULT_FALLBACK_CORPUS_FILE);
                defaultFallbackCorpus = null;
            } else {
                defaultFallbackCorpus = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Log.error("An error occurred when processing the default fallback corpus file {0}, this process may " + "produce unexpected behavior. Check the logs for more information.", DEFAULT_FALLBACK_CORPUS_FILE);
        }
        Log.debug("Loaded corpus file {0} for the default fallback state", DEFAULT_FALLBACK_CORPUS_FILE);
    }

    public static void main(String[] args) {
        if (setModel()) {
            setDefaultFallbackCorpus();
        }
        val greetings = intent("Greetings")
                .trainingSentence("Hi")
                .trainingSentence("Hello")
                .trainingSentence("Good morning")
                .trainingSentence("Good afternoon");

        val howAreYou = intent("HowAreYou")
                .trainingSentence("How are you?")
                .trainingSentence("What's up?")
                .trainingSentence("How do you feel?");

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleWhatsUp = state("HandleWhatsUp");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(greetings)).moveTo(handleWelcome)
                .when(intentIs(howAreYou)).moveTo(handleWhatsUp);

        handleWelcome
                .body(context -> reactPlatform.reply(context, "Hi, nice to meet you!"))
                .next()
                .moveTo(awaitingInput);

        handleWhatsUp.body(context -> reactPlatform.reply(context, "I am fine and you?"))
                .next()
                .moveTo(awaitingInput);

        val defaultFallback = fallbackState().body(context -> {
            if (isNull(defaultFallbackCorpus)) {
                reactPlatform.reply(context, "Sorry, I didn't, get it");
            } else {
                String question = context.getIntent().getMatchedInput();
                String qaAnswer = processQA(question);
                if (isNull(qaAnswer)) {
                    reactPlatform.reply(context, "Sorry, I didn't, get it");
                } else {
                    reactPlatform.reply(context, "I don't know the exact answer, but I found this potential result:");
                    reactPlatform.reply(context, qaAnswer);
                }
            }

        });
        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
    }
}
