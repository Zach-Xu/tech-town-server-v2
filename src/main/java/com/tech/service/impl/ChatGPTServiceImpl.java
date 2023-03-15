package com.tech.service.impl;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.tech.config.OpenAiConfig;
import com.tech.dto.PromptDTO;
import com.tech.service.ChatGPTService;
import com.tech.vo.ResponseResult;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatGPTServiceImpl implements ChatGPTService {

    @Resource
    private OpenAiConfig openAiConfig;

    private final OpenAiService openAiService;

    @Autowired
    public ChatGPTServiceImpl(OpenAiConfig openAiConfig) {
        this.openAiService = new OpenAiService(openAiConfig.getSecretKey(), openAiConfig.DEFAULT_TIMEOUT);
    }

    @Override
    public ResponseResult createCompletion(PromptDTO promptDTO) {
        String prompt = promptDTO.getPrompt();
        if (Strings.isBlank(prompt)) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
        CompletionRequest completionRequest = this.buildCompletionRequest(prompt);
        return new ResponseResult(HTTPResponse.SC_OK, "ChatGPT generated message successfully", this.createCompletion(completionRequest));
    }

    private CompletionRequest buildCompletionRequest(String message) {
        return CompletionRequest.builder().prompt(message)
                .model(this.openAiConfig.getModel())
                .maxTokens(this.openAiConfig.getMax_tokens())
                .echo(true)
                .build();
    }


    /**
     * Creates a completion for the provided prompt and parameters
     */
    protected String createCompletion(CompletionRequest completionRequest) {
        StringBuilder response = new StringBuilder();
        CompletionResult completion = this.openAiService.createCompletion(completionRequest);
        List<CompletionChoice> choices = completion.getChoices();
        choices.forEach(choice -> response.append(choice.getText()));

//        String res = StrUtil.replaceFirst(response.toString(), "\n\n", "");

        return response.toString();
    }


}
