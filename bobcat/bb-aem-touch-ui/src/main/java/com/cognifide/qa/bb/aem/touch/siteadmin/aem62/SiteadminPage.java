/*
 * Copyright 2016 Cognifide Ltd..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.qa.bb.aem.touch.siteadmin.aem62;

import java.time.LocalDateTime;

import javax.annotation.Nullable;

import com.cognifide.qa.bb.aem.touch.siteadmin.SiteadminActions;
import com.cognifide.qa.bb.aem.touch.siteadmin.common.ActivationStatus;
import com.cognifide.qa.bb.aem.touch.siteadmin.common.PageActivationStatus;
import com.cognifide.qa.bb.aem.touch.siteadmin.common.SiteadminLayout;

import com.cognifide.qa.bb.aem.touch.util.Conditions;
import com.cognifide.qa.bb.constants.AemConfigKeys;
import com.cognifide.qa.bb.constants.Timeouts;
import com.cognifide.qa.bb.provider.selenium.BobcatWait;
import com.cognifide.qa.bb.qualifier.Global;
import com.cognifide.qa.bb.qualifier.PageObject;
import com.cognifide.qa.bb.utils.PageObjectInjector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

@PageObject
public class SiteadminPage implements SiteadminActions {

  private static final String SITEADMIN_PATH = "/sites.html";
  private static final String CHILD_PAGE_WINDOW_SELECTOR = ".cq-siteadmin-admin-childpages";

  @Inject
  @Named(AemConfigKeys.AUTHOR_URL)
  private String url;

  @Inject
  private Conditions conditions;

  @Inject
  private WebDriver driver;

  @FindBy(css = ".cq-siteadmin-admin-childpages")
  @Global
  private ChildPageWindow childPageWindow;

  @FindBy(css = ".granite-collection-selectionbar .granite-selectionbar .coral-ActionBar-container")
  private SiteadminToolbar siteadminToolbar;

  @FindBy(css = "#granite-shell-actionbar")
  private ContentToolbar contentToolbar;

  @Inject
  private BobcatWait wait;

  @Inject
  private PageObjectInjector pageObjectInjector;

  @Override
  public SiteadminActions open(String nodePath) {
    driver.manage().addCookie(SiteadminLayout.LIST.getCookie62());
    driver.get(getSiteAdminUrl() + nodePath);
    wait.withTimeout(Timeouts.SMALL).until(input -> isLoaded(), 2);
    return this;
  }

  private String getSiteAdminUrl() {
    return url + SITEADMIN_PATH;
  }

  @Override
  public SiteadminActions open() {
    driver.manage().addCookie(SiteadminLayout.LIST.getCookie());
    driver.get(getSiteAdminUrl());
    driver.navigate().refresh();
    return this;
  }

  @Override
  public SiteadminActions createNewPage(String title, String templateName) {
    contentToolbar.getCreateButton().click();
    contentToolbar.getCreatePageButton().click();
    contentToolbar.getCreatePageWizard()
        .selectTemplate(templateName)
        .provideTitle(title).submit();
    return this;
  }

  @Override public SiteadminActions waitForPageCount(int pageCount) {
    throw new NotImplementedException("This feature is not implemented yet.");
  }

  @Override
  public SiteadminActions createNewPage(String title, String name, String templateName) {
    contentToolbar.getCreateButton().click();
    contentToolbar.getCreatePageButton().click();
    contentToolbar.getCreatePageWizard()
        .selectTemplate(templateName)
        .provideName(name)
        .provideTitle(title).submit();
    return this;
  }

  @Override
  public SiteadminActions publishPage(String title) {
    childPageWindow.selectPage(title);
    siteadminToolbar.publishPageNow();
    waitForExpectedStatus(title, ActivationStatus.PUBLISHED);
    return this;
  }

  @Override
  public SiteadminActions unpublishPage(String title) {
    childPageWindow.selectPage(title);
    siteadminToolbar.unpublishPageNow();
    waitForExpectedStatus(title, ActivationStatus.NOT_PUBLISHED);
    return this;
  }

  @Override
  public SiteadminActions publishPageLater(String title, LocalDateTime scheduledDateTime) {
    childPageWindow.selectPage(title);
    siteadminToolbar.publishPageLater(scheduledDateTime);
    wait.withTimeout(Timeouts.SMALL).until(input -> isPagePresent(title));
    waitForExpectedStatus(title, ActivationStatus.SCHEDULED);
    return this;
  }

  @Override
  public SiteadminActions unpublishPageLater(String title, LocalDateTime scheduledDateTime) {
    childPageWindow.selectPage(title);
    siteadminToolbar.unpublishPageLater(scheduledDateTime);
    wait.withTimeout(Timeouts.SMALL).until(input -> isPagePresent(title));
    waitForExpectedStatus(title, ActivationStatus.SCHEDULED);
    return this;
  }

  @Override
  public SiteadminActions deletePage(String title) {
    childPageWindow.selectPage(title);
    siteadminToolbar.deleteSelectedPages();
    wait.withTimeout(Timeouts.MEDIUM).until(input -> isLoaded(), Timeouts.SMALL);
    return this;
  }

  @Override public SiteadminActions deleteSubPages() {
    if (childPageWindow.hasSubPages()) {
      childPageWindow.pressSelectAllPages();
      siteadminToolbar.deleteSelectedPages();
    }
    return this;
  }

  @Override
  public SiteadminActions copyPage(String title, String destination) {
    childPageWindow.selectPage(title);
    siteadminToolbar.copyPage();
    open(destination);
    contentToolbar.pastePage();
    return this;
  }

  @Override
  public SiteadminActions movePage(String title, String destinationPath) {
    childPageWindow.selectPage(title);
    siteadminToolbar.movePage(destinationPath);
    return this;
  }

  @Override
  public boolean isPagePresent(String title) {
    return childPageWindow.containsPage(title);
  }

  @Override public boolean hasChildPages() {
    return childPageWindow.hasSubPages();
  }

  public ChildPageRow getPageFromList(String title) {
    return childPageWindow.getChildPageRow(title);
  }

  public void refresh() {
    driver.navigate().refresh();
  }

  public boolean isLoaded() {
    boolean isLoaded = isLoadedCondition();
    if (!isLoaded) {
      retryLoad();
    }
    return isLoaded;
  }

  private void waitForExpectedStatus(final String title, ActivationStatus status) {
    wait.withTimeout(Timeouts.MEDIUM).until(new ExpectedCondition<Boolean>() {
      @Nullable @Override public Boolean apply(@Nullable WebDriver webDriver) {
        webDriver.navigate().refresh();
        ChildPageRow childPageRow = getChildPageWindow(webDriver).getChildPageRow(title);
        PageActivationStatus pageActivationStatusCell = childPageRow.getPageActivationStatus();
        ActivationStatus activationStatus = pageActivationStatusCell.getActivationStatus();
        return activationStatus.equals(status);
      }
    }, Timeouts.MINIMAL);
  }

  private ChildPageWindow getChildPageWindow(WebDriver webDriver) {
    WebElement childPageWindow =
        webDriver.findElement(By.cssSelector(CHILD_PAGE_WINDOW_SELECTOR));
    return pageObjectInjector.inject(ChildPageWindow.class, childPageWindow);
  }

  private void retryLoad() {
    conditions.verify(new ExpectedCondition<Object>() {
      @Nullable
      @Override
      public Object apply(WebDriver driver) {
        driver.navigate().refresh();
        return isLoadedCondition();
      }
    }, Timeouts.MEDIUM);
  }

  private boolean isLoadedCondition() {
    return conditions.isConditionMet(ignored -> childPageWindow.isLoaded());
  }

}