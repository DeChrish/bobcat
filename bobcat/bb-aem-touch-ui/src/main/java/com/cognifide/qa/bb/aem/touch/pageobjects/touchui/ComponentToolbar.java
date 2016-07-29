/*-
 * #%L
 * Bobcat Parent
 * %%
 * Copyright (C) 2016 Cognifide Ltd.
 * %%
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
 * #L%
 */
package com.cognifide.qa.bb.aem.touch.pageobjects.touchui;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.cognifide.qa.bb.qualifier.CurrentScope;
import com.cognifide.qa.bb.qualifier.PageObject;
import com.cognifide.qa.bb.aem.touch.util.Conditions;
import com.google.inject.Inject;

@PageObject
public class ComponentToolbar {

  public static final String CSS = "#EditableToolbar";

  private static final String BUTTON_XPATH_TEMPLATE = ".//button[contains(@title, '%s')]";

  @Inject
  private Conditions conditions;

  @Inject
  @CurrentScope
  private WebElement toolbar;

  public void clickOption(ToolbarOptions option) {
    By locator = By.xpath(String.format(BUTTON_XPATH_TEMPLATE, option.getTitle()));
    toolbar.findElement(locator).click();
  }

  public void verifyIsDisplayed() {
    conditions.verify(visibilityOf(toolbar));
  }
}
