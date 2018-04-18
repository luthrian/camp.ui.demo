package com.camsolute.code.camp.ui.test;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.Theme;

/**
 * 
 */
@HtmlImport("frontend://styles/shared-styles.html")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(Lumo.class)
public class LandingPage extends Div implements RouterLayout,
        AfterNavigationObserver, PageConfigurator {

	
  /**
	 * 
	 */
	private static final long serialVersionUID = 2291491252889870241L;
	
	public LandingPage() {
      Div header = new Div(new H2("Camp.UI.Demo Application"));
      header.addClassName("main-layout__header");
      add(header);

      addClassName("main-layout");
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
  }

  @Override
  public void configurePage(InitialPageSettings settings) {
      settings.addMetaTag("apple-mobile-web-app-capable", "yes");
      settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");
  }
}
