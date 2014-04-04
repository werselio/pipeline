package com.continuuity.gateway.run;

import com.continuuity.common.conf.CConfiguration;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.junit.Test;

/**
 * Test {@link MetricsTwillRunnable}.
 */
public class MetricsTwillRunnableTest {

  @Test
  public void testInjection() throws Exception {
    Injector injector = MetricsTwillRunnable.createGuiceInjector(CConfiguration.create(), HBaseConfiguration.create());
    Assert.assertNotNull(injector);
  }
}
