package com.gtnewhorizons.navigator.internal.nei;

import com.gtnewhorizons.navigator.internal.FormattedTextField;

import codechicken.nei.SearchField;
import codechicken.nei.SearchTextFormatter;

public final class NEISearchFormatter {

    private NEISearchFormatter() {}

    public static FormattedTextField.TextFormatter create() {
        final SearchTextFormatter formatter = new SearchTextFormatter(SearchField.searchParser);
        return formatter::format;
    }
}
