package com.hackathon.sos.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);

    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private Integer maxTokens;

    /**
     * GPT를 사용하여 음성 텍스트에서 위급 상황을 분석합니다
     */
    public String analyzeEmergencySituation(String audioTranscript) {
        logger.info("AI 위급 상황 분석 시작");

        if (audioTranscript == null || audioTranscript.trim().isEmpty()) {
            return "음성 인식 실패 - 주변 소음 또는 긴급 상황으로 추정됨";
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();

            // 시스템 프롬프트
            messages.add(new ChatMessage("system",
                    "당신은 긴급 상황 분석 전문가입니다. " +
                            "주어진 음성 텍스트를 분석하여 어떤 위급 상황인지 판단하고, " +
                            "상황의 심각도와 필요한 조치사항을 간단명료하게 설명해주세요. " +
                            "분석 결과는 200자 이내로 작성해주세요."));

            // 사용자 프롬프트
            messages.add(new ChatMessage("user",
                    "다음은 긴급 상황에서 녹음된 음성을 텍스트로 변환한 내용입니다:\n\n" +
                            audioTranscript + "\n\n" +
                            "이 상황을 분석하여 다음 정보를 제공해주세요:\n" +
                            "1. 상황의 종류 (사고, 폭행, 납치, 응급의료, 화재, 재난 등)\n" +
                            "2. 상황의 심각도\n" +
                            "3. 즉각적으로 필요한 조치"));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(0.3)  // 일관된 응답을 위해 낮은 temperature 사용
                    .build();

            String analysis = openAiService.createChatCompletion(completionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            logger.info("AI 분석 완료: {}", analysis);
            return analysis;

        } catch (Exception e) {
            logger.error("AI 분석 중 오류 발생: {}", e.getMessage(), e);
            return "AI 분석 실패 - 긴급 상황으로 추정되어 알림을 발송합니다";
        }
    }

    /**
     * 분석 결과를 바탕으로 긴급 상황 유형을 판단합니다
     */
    public String determineEmergencyType(String analysis) {
        String lowerAnalysis = analysis.toLowerCase();

        if (lowerAnalysis.contains("사고") || lowerAnalysis.contains("accident")) {
            return "ACCIDENT";
        } else if (lowerAnalysis.contains("폭행") || lowerAnalysis.contains("범죄") ||
                lowerAnalysis.contains("assault")) {
            return "ASSAULT";
        } else if (lowerAnalysis.contains("납치") || lowerAnalysis.contains("유괴") ||
                lowerAnalysis.contains("kidnap")) {
            return "KIDNAPPING";
        } else if (lowerAnalysis.contains("응급") || lowerAnalysis.contains("의료") ||
                lowerAnalysis.contains("medical")) {
            return "MEDICAL";
        } else if (lowerAnalysis.contains("화재") || lowerAnalysis.contains("fire")) {
            return "FIRE";
        } else if (lowerAnalysis.contains("재난") || lowerAnalysis.contains("disaster")) {
            return "NATURAL_DISASTER";
        } else if (lowerAnalysis.contains("스토킹") || lowerAnalysis.contains("stalking")) {
            return "STALKING";
        } else {
            return "OTHER";
        }
    }
}