package com.imran.openaiapi.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;

@RestController
@RequestMapping("/api")
public class ChatGptController {

    @Value("${chat.gpt.key}")
    private String chatGptApi;
    private final String CSVFile = "chatData.csv";
    private final String CSV_SEPARATOR =";";
    private final String[] CSV_HEADER = {"Question", "Answer"};
    @SneakyThrows
    @RequestMapping("/chatgpt")
    public String chatGpt(@RequestBody String question){
        String chatGptUrl = "https://api.openai.com/v1/completions";

        String authorizationHeader = "Bearer " + this.chatGptApi;
        String requestBody = "{\n"
                +"     \"Content-Type\": \"application/json\",\n"
                +"      \"Authorization\": \""+ authorizationHeader + "\",\n"
                +"     \"model\": \"text-davinci-003\",\n"
                + "    \"prompt\": \"" + question + "\",\n"
                + "    \"max_tokens\": 4000,\n"
                + "    \"temperature\": 1,\n"
                + "}";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(chatGptUrl, requestBody, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String answer = jsonNode.get("choices").get(0).get("text").asText();

        String[] records = {question, answer};
        File csvFile = new File(CSVFile);
        try {
            boolean fileExist = csvFile.exists();
            FileWriter fileWriter = new FileWriter(csvFile, true);
            CSVWriter csvWriter = new CSVWriter(fileWriter, CSV_SEPARATOR.charAt(0),
                    CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            if(!fileExist){
                csvWriter.writeNext(CSV_HEADER);
            }
            csvWriter.writeNext(records);
            csvWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return answer;

    }
}
