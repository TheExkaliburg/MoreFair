package de.kaliburg.morefair.security;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.MoreFairApplication;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTest {

  private final PasswordEncoder passwordEncoder = new MoreFairApplication().passwordEncoder();

  @Test
  void matchesPasswordsEncodedWithBouncyCastle170() {
    // Generated with the previous provider and the existing application's Argon2 settings.
    String hash = "$argon2id$v=19$m=16384,t=2,p=1$0lPkaCsOQRry+gUj95DJaQ"
        + "$fHwoMQRu2bBPG7nQZMs2KpkJmTVjKyLnG1mudYFiAnA";

    assertThat(passwordEncoder.matches("legacy-test-password", hash)).isTrue();
    assertThat(passwordEncoder.matches("wrong-password", hash)).isFalse();
    assertThat(passwordEncoder.upgradeEncoding(hash)).isFalse();
  }

  @Test
  void encodesAndVerifiesSaltedPasswords() {
    String first = passwordEncoder.encode("new-test-password");
    String second = passwordEncoder.encode("new-test-password");

    assertThat(first).startsWith("$argon2id$v=19$m=16384,t=2,p=1$");
    assertThat(first).isNotEqualTo(second);
    assertThat(passwordEncoder.matches("new-test-password", first)).isTrue();
    assertThat(passwordEncoder.matches("new-test-password", second)).isTrue();
    assertThat(passwordEncoder.matches("wrong-password", first)).isFalse();
  }
}
