package com.hjchoi.locationcheckout.ui.view;

import java.util.List;

public interface MapsView {
    void showProgress();

    void hideProgress();

    void setItems(List<String> items);

    void showMessage(String message);
}
