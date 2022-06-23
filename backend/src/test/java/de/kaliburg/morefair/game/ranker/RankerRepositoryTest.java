package de.kaliburg.morefair.game.ranker;


import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankerRepositoryTest {

  @Autowired
  RankerRepository repository;

  @Test
  @DataSet("yml/datasets/data.yml")
  void findFirstByAccountAndGrowingIsTrueOrderByLadder_NumberDesc() {

  }

}