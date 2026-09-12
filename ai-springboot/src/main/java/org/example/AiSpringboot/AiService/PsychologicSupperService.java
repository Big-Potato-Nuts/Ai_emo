package org.example.AiSpringboot.AiService;


import org.example.AiSpringboot.DTO.Command.ConsultationSessionCreatDTO;
import org.example.AiSpringboot.DTO.Response.ConsultationMessageResponseDTO;
import org.example.AiSpringboot.Entity.ConsultationSession;

import org.example.AiSpringboot.Service.ConsultationMessageService;
import org.example.AiSpringboot.Service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsychologicSupperService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreatDTO creatDTO){

        ConsultationSession consultationSession = consultationSessionService.createSession(userId, creatDTO);

        consultationMessageService.saveUserMessage(consultationSession.getId(),creatDTO.getInitialMessage(),null);

        String sessionId="session_"+consultationSession.getId();

        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                creatDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis()+8640000L,
                1,
                "ACTIVE"
        );


    }



    public Flux<String> streamPsychologicalChat(String sessionId,String userMassage){




        return Flux.create(sink -> {
            Long dbSessionId = extractSessionId(sessionId);
            if(dbSessionId==null){
                sink.error(new RuntimeException("会话id格式错误"));
                return;
            }

            boolean isInitialMessage=false;

            Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
            if(messageCount==1){
                ConsultationMessageResponseDTO lastMessageBySessionId = consultationMessageService.getLastMessageBySessionId(dbSessionId);
                if(lastMessageBySessionId!=null && lastMessageBySessionId.getSenderType().equals(1) && userMassage.equals(lastMessageBySessionId.getSenderType())){
                    isInitialMessage=true;

                }

            }
            if(!isInitialMessage){

                consultationMessageService.saveUserMessage(dbSessionId,userMassage,null);


            }

            String conversationId="conversation_"+sessionId;

            List<Message> userMessage = new ArrayList<>();
            userMessage.add(new UserMessage(userMassage));
            chatMemory.add(conversationId,userMessage);
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));


            StringBuilder sb=new StringBuilder();

            chatClient.prompt(prompt)
                    .user(userMassage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId))
                    .stream()
                    .content()
                    .doOnNext(Fragment->{
                        sb.append(Fragment);
                        sink.next(Fragment);
                    })
                    .doOnComplete(()->{
                        String completeRes = sb.toString();
                        consultationMessageService.saveAiMessage(dbSessionId,completeRes,"openai");
                        ArrayList<Message> aiMessage = new ArrayList<>();
                        aiMessage.add(new AssistantMessage(completeRes));
                        chatMemory.add(conversationId,aiMessage);

                        sink.complete();
                    })
                    .doOnError(error->{
                        sink.error(error);
                    })
                    .subscribe();







        });
    }

    public Long extractSessionId(String sessionId){
        if(sessionId!=null && sessionId.startsWith("session_")){
            return Long.parseLong(sessionId.substring("session_".length()));
        }
        return null;
    }








}
