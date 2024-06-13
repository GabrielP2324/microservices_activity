package management.users.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FailedLoginKafkaProducer {

  @Value("${topic.name.producer}")
  private String topicName;
  private static final Logger log = LoggerFactory.getLogger(FailedLoginKafkaProducer.class);

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void send(String message) {
    kafkaTemplate.send(topicName, message);
    log.debug("Enviado mensagem...");
  }

  public void sendMessage(String username) {
    kafkaTemplate.send(topicName, username);
  }
}