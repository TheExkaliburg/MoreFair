package de.kaliburg.morefair.utils;

import com.icegreen.greenmail.spring.GreenMailBean;
import de.kaliburg.morefair.utils.FairTest.GreenMailCleanupListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.BeansException;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.support.AbstractTestExecutionListener;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestExecutionListeners(
    listeners = GreenMailCleanupListener.class,
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public @interface FairTest {

  class GreenMailCleanupListener extends AbstractTestExecutionListener {

    @Override
    public void afterTestClass(TestContext testContext) {
      try {
        System.out.println("Shutting down GreenMailBean GREENSTOP");
        GreenMailBean greenMailBean = testContext.getApplicationContext()
            .getBean(GreenMailBean.class);
        greenMailBean.stop();
      } catch (BeansException be) {
        // ignore
      }
    }
  }

}
