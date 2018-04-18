package com.camsolute.code.camp.ui.test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.camsolute.code.camp.lib.utilities.Util;
import com.vaadin.flow.templatemodel.ModelConverter;
import com.vaadin.flow.templatemodel.TemplateModelUtil;

/**
 * Converts between DateTime-objects and their String-representations
 * 
 */

public class OrderDateToString
        implements ModelConverter<LocalDate, String> {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1317853403352224221L;

		@Override
    public LocalDate toModel(String presentationValue) {
        return LocalDate.parse(presentationValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String toPresentation(LocalDate modelValue) {
        return modelValue == null ? null : modelValue.toString();
    }

}
