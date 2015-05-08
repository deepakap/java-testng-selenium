package com.github.jsdevel.testng.selenium;

import java.lang.reflect.Method;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * An AbstractSuite that is thread safe, allows methods to be run in
 * parallel, and creates instances of interfaces that extend
 * {@link com.github.jsdevel.testng.selenium.PageFactory}.  This suite also
 * manages {@link org.openqa.selenium.WebDriver} instances,
 * by creating and configuring them before a test, and destroying them after a
 * test.
 * 
 * @author Joe Spencer
 * @param <PF> The {@link com.github.jsdevel.testng.selenium.PageFactory}
 * interface used to instantiate
 * {@link com.github.jsdevel.testng.selenium.Page}s during tests.
 */
public class AbstractSuite<PF extends PageFactory> {
  private final ThreadLocal<MethodContextImpl<PF>> methodContext = new ThreadLocal();

  /**
   * Returns an implementation of the
   * {@link com.github.jsdevel.testng.selenium.PageFactory} passed as a Type
   * parameter to this Suite.  All methods of the factory should return
   * instances of {@link com.github.jsdevel.testng.selenium.Page}.
   * 
   * @return An implementation of {@link PageFactory}.
   */
  protected PF getPageFactory() {
    return methodContext.get().getPageFactory();
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(Method method) {
    MethodContextImpl<PF> context = new MethodContextImpl(method);

    context.setEndpoint(EnvironmentConfig.ENDPOINT);
    AbsractSuiteHelpers.addWebDriver(context);
    AbsractSuiteHelpers.<PF>addPageFactory(context);

    methodContext.set(context);
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {
    MethodContextImpl<PF> context = methodContext.get();

    if (context == null) {
      return; 
    }

    if (context.getOutput() != null) {
      for (String line: context.getOutput()) {
        TestNGSeleniumLogger.log(line);
      }
    }

    context.getWebDriver().quit();

    methodContext.remove();
  }
}