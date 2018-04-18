package com.camsolute.code.camp.ui.test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.camsolute.code.camp.lib.models.Model;
import com.camsolute.code.camp.lib.models.ModelList;
import com.camsolute.code.camp.lib.models.order.Order;
import com.camsolute.code.camp.lib.models.order.OrderPosition;
import com.camsolute.code.camp.lib.models.order.OrderPositionList;
import com.camsolute.code.camp.lib.models.product.Product;
import com.camsolute.code.camp.lib.models.product.ProductList;
import com.camsolute.code.camp.lib.utilities.Util;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.icon.VaadinIcons;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.DateRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class CreateOrderDialog extends Dialog {

	private static final Logger LOG = LogManager.getLogger(CreateOrderDialog.class);
	private static String fmt = "[%15s] [%s]";
	
	private final H3 title = new H3();

	private final Button saveButton = new Button("Save",VaadinIcons.DISC.create());
  
	private final Button cancelButton = new Button("Cancel",VaadinIcons.CLOSE.create());

  private FlexLayout main = new FlexLayout();
  
  private final FormLayout formLayout = new FormLayout();
  
  private Div header = new Div();
  
  private HorizontalLayout body = new HorizontalLayout();

  private Binder<Order> binder = new Binder<Order>();
  
  private Order currentItem;

  private static OrderPositionList currentOrderPositions = new OrderPositionList();

  private final Consumer<Order> orderSaver;
  
  private DatePicker date = new DatePicker();

  private DatePicker byDate = new DatePicker();
  
  private TextField businessId = new TextField("Order BusinessId");

  private Input quantity = new Input();
  
  private Timestamp timestamp = Util.Time.timestamp();
    
  private final String nameInTitle = "Create New Order";
  
  private final String nameInText = "create and submit a new order to Order Service P";

  public CreateOrderDialog(Consumer<Order> saveHandler) {
  	
    this.orderSaver = saveHandler;
    
    main = new FlexLayout();
    
    main.setId("create-dialog-main");
    
    formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
               new FormLayout.ResponsiveStep("25em", 2));
    
    Div div = new Div(header,formLayout);
    div.addClassName("has-padding");
    div.setHeight("200px");
    div.setWidth("650px");
    
    main.add(div);
    
    VerticalLayout container = new VerticalLayout();
    
    
    saveButton.setAutofocus(true);
    saveButton.getElement().setAttribute("theme", "primary");
    saveButton.getElement().getStyle().set("top","32px");
    saveButton.setEnabled(true);
    saveButton.setWidth("100px");
    
    cancelButton.addClickListener(e -> close());
    cancelButton.getElement().getStyle().set("top","32px");
    cancelButton.setWidth("100px");
    
    title.setText(nameInTitle);
    title.setWidth("650px");
    
    header.setWidth("100%");
    header.getStyle().set("width", "100%");
    header.add(title);
    body = new HorizontalLayout();
    body.setWidth("100%");
    
    
    container.add(body);
    
    formLayout.add(container);  
    
    add(main);
   
    createBusinessIdField();

    createDatePicker();

    body.add(saveButton, cancelButton);
    setCloseOnEsc(true);
    
    setCloseOnOutsideClick(false);
  }

  public FlexLayout mainLayout() {
  	return main;
  }
  
  public Div header() {
  	return header;
  }

  public HorizontalLayout bodyLayout() {
  	return body;
  }

  private void createDatePicker() {
  	byDate.setLabel("Deliver-By-Date");        
    byDate.setRequired(true);
    byDate.setWeekNumbersVisible(true);
    byDate.setMax(Util.Time.getLocalDate(Util.Time.timestamp(Util.Time.nowPlus(100,Util.Time.formatDateTime))));
    byDate.setValue(byDate.getMin());
    byDate.setEnabled(true);
      
    byDate.addValueChangeListener(e -> updateTimestamp(e));
      
    body.add(byDate);

    binder.forField(byDate)
            .withValidator(Objects::nonNull,
                    "The date should be in MM/dd/yyyy format.")
            .withValidator(new DateRangeValidator(
                    "The date should be 10 to a maximum of 100 days in the future.",
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(100)))
            .bind(CreateOrderDialog::getDate, CreateOrderDialog::setDate);

  }

  private Object updateTimestamp(ValueChangeEvent<DatePicker, LocalDate> event) {
  	String time = Util.Time.time(Util.Time.timestamp(Util.Time.now(Util.Time.formatDateTime)));
  	LocalDate date = event.getValue();
  	String datetime = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" "+time;
  	
  	if(!Util._IN_PRODUCTION){String msg = "----[datetime("+datetime+")]----";LOG.info(String.format(fmt, "updateTimestamp",msg));}
  	timestamp = Util.Time.timestamp(datetime);
		return null;
	}


  private void createBusinessIdField() {
      businessId.setLabel("Order Number");
      businessId.setRequired(true);
      body.add(businessId);

      binder.forField(businessId)
              .withConverter(String::trim, String::trim)
              .withValidator(new StringLengthValidator(
                      "Order number must contain at least 9 printable characters",
                      9, null))
              .bind(CreateOrderDialog::getOrderBusinessId, CreateOrderDialog::updateOrderBusinessId);
  }

  private static String getOrderBusinessId(Order order) {
  	return order.onlyBusinessId();
  }
  
  private static void updateOrderBusinessId(Order order, String businessId) {
  	order.updateBusinessId(businessId);
  }
  
  private static LocalDate getDate(Order order) {
  	String date = Util.Time.date(order.byDate());
  	return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
  
  private static void setDate(Order order, LocalDate date) {
		String d = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)+" "+Util.Time.time(Util.Time.timestamp(Util.Time.now(Util.Time.formatDateTime)));
  	order.updateByDate(Util.Time.timestamp(d));
  	OrderBucket.instance().updateOrder(order);
  }
  
  public final void open(Order item) {
      currentItem = item;
      saveButton.addClickListener(e -> saveClicked());
      binder.readBean(currentItem);
      open();
  }

  private void saveClicked() {
      boolean isValid = binder.writeBeanIfValid(currentItem);
      if (isValid) {
        orderSaver.accept(currentItem);
        close();
      } else {
        BinderValidationStatus<Order> status = binder.validate();
        Notification.show(status.getFieldValidationErrors().get(0).getMessage().get(), 2000, Position.MIDDLE);
      }
  }
	protected static final OrderPosition currentOrderPostion(OrderPositionList positionList) {
		return positionList.selected();
	}
	protected static void setOrderPosition(OrderPositionList positionList, OrderPosition orderPosition) {
		positionList.select(orderPosition);
		currentOrderPositions.setSelectionIndex(positionList.selectionIndex());
	}
}
