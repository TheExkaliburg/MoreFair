package de.kaliburg.morefair.repository.chat;

import de.kaliburg.morefair.entity.chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}
