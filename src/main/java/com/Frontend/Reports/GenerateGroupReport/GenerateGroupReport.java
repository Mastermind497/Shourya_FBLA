package com.Frontend.Reports.GenerateGroupReport;

import com.Frontend.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "group-report", layout = MainView.class)
@PageTitle("FBLA Genie | Generate Group Report")
public class GenerateGroupReport extends VerticalLayout {
    Button selector = new Button("Generate Report");
    public GenerateGroupReport() {
        MainView.setActiveTab(MainView.REPORT_TAB);
        add(selector);
        selector.addClickListener(onClick -> {
            remove(selector);
            generateReport();
        });
        setAlignItems(Alignment.CENTER);
        setAlignSelf(Alignment.CENTER);
    }
    public void generateReport() {

    }
}
