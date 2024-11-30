package ingisis.manager.redis.consumer

//import ingisis.manager.redis.model.RuleChangeEvent
//import ingisis.manager.redis.model.SnippetToValidate
//import ingisis.manager.redis.model.SnippetsValidationMessage
//import ingisis.manager.redis.producer.SnippetValidationProducer
//import ingisis.manager.snippet.service.SnippetService
//import kotlinx.coroutines.reactor.mono
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Profile
//import org.springframework.data.redis.connection.stream.ObjectRecord
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.data.redis.stream.StreamReceiver
//import org.springframework.stereotype.Component
//import java.time.Duration
//
//@Component
//@Profile("!test")
//class RuleChangeListener(
//    @Value("\${stream.key.rules-changed-channel}") streamKey: String,
//    @Value("\${groups.rules}") groupId: String,
//    redisTemplate: RedisTemplate<String, String>,
//    private val snippetService: SnippetService,
//    private val validationProducer: SnippetValidationProducer,
//) : RedisStreamConsumer<RuleChangeEvent>(streamKey, groupId, redisTemplate) {
//    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, RuleChangeEvent>> =
//        StreamReceiver.StreamReceiverOptions
//            .builder()
//            .pollTimeout(Duration.ofMillis(10000))
//            .targetType(RuleChangeEvent::class.java)
//            .build()
//
//    override fun onMessage(record: ObjectRecord<String, RuleChangeEvent>) {
//        val event = record.value
//        println("Received rule change event: ${event.ruleType} for user: ${event.userId}, timestamp: ${event.timestamp}")
//
//        // Identify the rule type and user ID from the event
//        val ruleType = event.ruleType
//        val userId = event.userId
//
//        // Get all snippets for the user
//        val snippetsToValidate = snippetService.getSnippetsByUserId(userId)
//        if (snippetsToValidate.isEmpty()) {
//            println("No snippets found for validation.")
//            return
//        }
//
//        val validationMessage =
//            SnippetsValidationMessage(
//                ruleType = ruleType,
//                snippets =
//                    snippetsToValidate.map { snippet ->
//                        SnippetToValidate(
//                            id = snippet.id,
//                            authorId = snippet.authorId,
//                            name = snippet.name,
//                            content = snippetService.getSnippetContent(snippet.content),
//                            language = snippet.language,
//                            languageVersion = snippet.languageVersion,
//                            extension = snippet.extension,
//                        )
//                    },
//                authorizationHeader =
//            )
//
//        // Send the snippets to the validation service
//        mono {
//            validationProducer.publishValidationMessage(ruleType, validationMessage)
//        }.subscribe()
//    }
//}
