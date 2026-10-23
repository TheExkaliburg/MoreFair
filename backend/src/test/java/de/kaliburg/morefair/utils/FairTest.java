package de.kaliburg.morefair.utils;

import com.icegreen.greenmail.spring.GreenMailBean;
import de.kaliburg.morefair.MoreFairApplication;
import de.kaliburg.morefair.utils.FairTest.GreenMailCleanupListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.support.AbstractTestExecutionListener;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestExecutionListeners(
    listeners = GreenMailCleanupListener.class,
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = MoreFairApplication.class)
@AutoConfigureMockMvc
public @interface FairTest {

  class GreenMailCleanupListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
      GreenMailBean greenMailBean = testContext.getApplicationContext()
          .getBean(GreenMailBean.class);
      greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
    }
  }

}
