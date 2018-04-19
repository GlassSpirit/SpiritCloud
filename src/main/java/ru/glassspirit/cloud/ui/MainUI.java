package ru.glassspirit.cloud.ui;

import com.vaadin.annotations.Title;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUI
@PushStateNavigation
@Title("Spirit Cloud")
public class MainUI extends UI {

    @Autowired
    SpringNavigator navigator;

    @Override
    protected void init(VaadinRequest request) {
        navigator.init(this, this);
    }
}
