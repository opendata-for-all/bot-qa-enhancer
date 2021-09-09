from flask import Flask, request
from transformers import AutoTokenizer, AutoModelForTableQuestionAnswering, AutoModelForQuestionAnswering
import torch
import logging
import pandas as pd
from io import StringIO
logging.basicConfig(level=logging.INFO)

MAX_TOKENS = 512
MAX_TOKENS_XL = 4096

modelNames = []
models = []
tokenizers = []


app = Flask(__name__)

@app.route('/set-model-text', methods=['POST'])
def seModelText():
    modelNames.clear()
    models.clear()
    tokenizers.clear()

    body = request.get_json()
    modelName = body["modelName"]
    modelNames.append(modelName)
    logging.info("Loading " + modelName + " tokenizer")
    tokenizer = AutoTokenizer.from_pretrained(modelName)
    logging.info(modelName + " tokenizer loaded")
    logging.info("Loading " + modelName + " model")
    model = AutoModelForQuestionAnswering.from_pretrained(modelName)
    logging.info(modelName + " model loaded")
    print("------------------------------------")
    models.append(model)
    tokenizers.append(tokenizer)
    response = {
        "status": "done"
        }
    return response, 200

@app.route('/set-model-table', methods=['POST'])
def setModelTable():
    modelNames.clear()
    models.clear()
    tokenizers.clear()

    body = request.get_json()
    modelName = body["modelName"]
    modelNames.append(modelName)
    
    logging.info("Loading " + modelName + " tokenizer")
    tokenizer = AutoTokenizer.from_pretrained("google/tapas-base-finetuned-wtq", drop_rows_to_fit = True)
    # tokenizer = TapasTokenizer.from_pretrained("google/tapas-base-finetuned-tabfact", drop_rows_to_fit=True)
    logging.info(modelName + " tokenizer loaded")
    logging.info("Loading " + modelName + " model")
    model = AutoModelForTableQuestionAnswering.from_pretrained("google/tapas-base-finetuned-wtq")
    # model = TapasForQuestionAnswering.from_pretrained("google/tapas-base-finetuned-tabfact")
    logging.info(modelName + " model loaded")
    print("------------------------------------")
    models.append(model)
    tokenizers.append(tokenizer)
    response = {
        "status": "done"
        }
    return response, 200
    
@app.route('/qa-text', methods=['POST'])
def QAText():
    body = request.get_json()
    question = body["question"]
    corpus = body["corpus"]
    response = {}
    for i, modelName in enumerate(modelNames):
        tokenizer = tokenizers[i]
        model = models[i]
        inputs = tokenizer(question, corpus, add_special_tokens=True, truncation=True, return_tensors="pt")
        input_ids = inputs["input_ids"].tolist()[0]
        # corpus_tokens = tokenizer.convert_ids_to_tokens(input_ids)
        outputs = model(**inputs)
        answer_start_scores = outputs.start_logits
        answer_end_scores = outputs.end_logits
        answer_start = torch.argmax(answer_start_scores)  # Get the most likely beginning of answer with the argmax of the score
        answer_end = torch.argmax(answer_end_scores) + 1  # Get the most likely end of answer with the argmax of the score
        answer = tokenizer.convert_tokens_to_string(tokenizer.convert_ids_to_tokens(input_ids[answer_start:answer_end]))
        response[modelName] = {
            "question": question,
            "answer": answer,
            # "beginPosition": answer_start.item(),
            # "endPosition": answer_end.item()
        }
    return response, 200

@app.route('/qa-table', methods=['POST'])
def QATable():
    body = request.get_json()
    question = body["question"]
    corpus = body["corpus"]
    response = {}
    for i, modelName in enumerate(modelNames):
        tokenizer = tokenizers[i]
        model = models[i]
        
        table = pd.read_csv(StringIO(corpus), sep=",")
        table = table.applymap(str)

        inputs = tokenizer(table=table, queries=question, padding='max_length', truncation=True, return_tensors="pt")
        outputs = model(**inputs)
        
        predicted_table_cell_coords, predicted_aggregation_operators = tokenizer.convert_logits_to_predictions(
          inputs,
          outputs.logits.detach(),
          outputs.logits_aggregation.detach()
        )
        aggregation_operators = {0: "NONE", 1: "SUM", 2: "AVERAGE", 3:"COUNT"}
        aggregation_prediction = aggregation_operators[predicted_aggregation_operators[0]]
      
        coordinates = predicted_table_cell_coords[0]
        """
        if len(coordinates) == 1:
            # 1 cell
            answer = table.iat[coordinates[0]]
        else:
            # > 1 cell
            cell_values = []
            for coordinate in coordinates:
                cell_values.append(table.iat[coordinate])
            answer = (", ".join(cell_values))
        """
        answer = []
        for coordinate in coordinates:
          answer.append(table.iat[coordinate])
        
        response[modelName] = {
            "question": question,
            "answer": answer,
            "aggregation": aggregation_prediction
        }
    return response, 200

