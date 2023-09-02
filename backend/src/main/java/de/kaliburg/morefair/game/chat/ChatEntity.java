package de.kaliburg.morefair.game.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "chat", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_type_number", columnNames = {"type", "number"})})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_chat", sequenceName = "seq_chat", allocationSize = 1)
public class ChatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chat")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChatType type;
  @Column
  private Integer number;

  public String getDestination() {
    if (type.isParameterized()) {
      return getType().toString().toLowerCase() + "/" + getNumber();
    }

    return getType().toString().toLowerCase();
  }

  public String getIdentifier() {
    if (type.isParameterized()) {
      return getType().toString().toLowerCase() + "-" + getNumber();
    }

    return getType().toString().toLowerCase();

  }
}
