// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.manager;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import com.google.enterprise.connector.pusher.GsaFeedConnection;
import com.google.enterprise.connector.spi.SimpleTraversalContext;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import java.util.Properties;

public class ContextTest extends TestCase {
  @Override
  protected void setUp() {
    Context.refresh();
  }

  @Override
  protected void tearDown() {
    Context.refresh();
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.manager.Context#start()}.
   */
  public final void testStart() {
    Context.getInstance().setStandaloneContext(
        Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    Context.getInstance().setFeeding(false);
    Context.getInstance().start();
    ApplicationContext ac = Context.getInstance().getApplicationContext();
    printBeanNames(ac);
    Context.getInstance().shutdown(true);
  }

  private void printBeanNames(ApplicationContext ac) {
    String[] beanList = ac.getBeanDefinitionNames();
    String contextName = ac.getDisplayName();
    System.out.println("Beans application context: " + contextName);
    for (int i = 0; i < beanList.length; i++) {
      Object bean = ac.getBean(beanList[i]);
      if (bean != null) {
        System.out.println(beanList[i] + " " + bean.getClass().toString());
      }
    }
  }

  public void testInitTraversalContext_false_null() {
    // GSA 6.14, not disabled, no inherited, no deny
    testInitTraversalContext(false, null, false, false);
  }

  public void testInitTraversalContext_false_false() {
    // GSA 6.14, not disabled, no inherited, no deny
    testInitTraversalContext(false, false, false, false);
  }

  public void testInitTraversalContext_false_true() {
    // GSA 6.14, disabled, no inherited, no deny
    testInitTraversalContext(false, true, false, false);
  }

  public void testInitTraversalContext_true_null() {
    // GSA 7.0, not disabled, inherited, deny
    testInitTraversalContext(true, null, true, true);
  }

  public void testInitTraversalContext_true_false() {
    // GSA 7.0, not disabled, inherited, deny
    testInitTraversalContext(true, false, true, true);
  }

  public void testInitTraversalContext_true_true() {
    // GSA 7.0, disabled, no inherited, deny
    testInitTraversalContext(true, true, false, true);
  }

  private void testInitTraversalContext(boolean gsaSupportsInheritedAcls,
      Boolean feedDisableInheritedAcls, boolean expectedSupportsInheritedAcls,
      boolean expectedSupportsDenyAcls) {
    SimpleTraversalContext simpleContext = new SimpleTraversalContext();
    GsaFeedConnection feeder = createMock(GsaFeedConnection.class);
    expect(feeder.supportsInheritedAcls())
        .andReturn(gsaSupportsInheritedAcls)
        .anyTimes();
    replay(feeder);
    Properties props = new Properties();
    if (feedDisableInheritedAcls != null) {
      props.setProperty(Context.FEED_DISABLE_INHERITED_ACLS,
          feedDisableInheritedAcls.toString());
    }

    Context out = Context.getInstance();
    out.initTraversalContext(simpleContext, props, feeder);

    assertEquals("supportsInheritedAcls", expectedSupportsInheritedAcls,
        simpleContext.supportsInheritedAcls());
    assertEquals("supportsDenyAcls", expectedSupportsDenyAcls,
        simpleContext.supportsDenyAcls());
  }
}
