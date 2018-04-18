package com.camsolute.code.camp.ui.test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.camsolute.code.camp.lib.dao.rest.ProcessControlRest;
import com.camsolute.code.camp.lib.models.order.Order;
import com.camsolute.code.camp.lib.models.order.Order.UpdateAttribute;
import com.camsolute.code.camp.lib.models.order.OrderList;
import com.camsolute.code.camp.lib.models.order.OrderRest;
import com.camsolute.code.camp.lib.models.process.OrderProcess;
import com.camsolute.code.camp.lib.models.process.Process;
import com.camsolute.code.camp.lib.models.rest.Message.MessageType;
import com.camsolute.code.camp.lib.models.rest.OrderProcessMessage;
import com.camsolute.code.camp.lib.models.rest.OrderProcessMessage.CustomerOrderMessage;
import com.camsolute.code.camp.lib.models.rest.Request.Principal;
import com.camsolute.code.camp.lib.utilities.Util;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcons;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route(value = "", layout = LandingPage.class)
@PageTitle("Customer Order List")
public class OrdersView extends VerticalLayout {
	private static final Logger LOG = LogManager.getLogger(OrdersView.class);
	private static String fmt = "[%15s] [%s]";
	 public static enum OrderAction {
	    CREATE_ORDER("Create Order","Order created.",Order.Status.CREATED,"customer_order_process"),
	    SUBMIT_ORDER("Submit Order","Order submitted.",Order.Status.SUBMITTED,"customer_order_process"),
	    REJECT_ORDER("Reject Order","Order rejected.",Order.Status.REJECTED,"customer_order_management_process"),
	    RELEASE_ORDER("Release Order","Order released to production.",Order.Status.PRODUCTION,"customer_order_management_process"),
	    UPDATE_ORDER("Update Order","Order updated.",Order.Status.UPDATED,"customer_order_process"),
	    CANCEL_ORDER("Cancel Order","Order cancelled.",Order.Status.CANCELLED,"customer_order_process"),
	    ACKNOWLEDGE_UPDATE("Accept Update","Order production.",Order.Status.PRODUCTION,"production_process"),
	    SHIP_ORDER("Ship Order","Order shipped.",Order.Status.SHIPPING,"production_process"),
	    ACKNOWLEDGE_DELIVERY("Accept Delivery","Order fulfilled.",Order.Status.FULFILLED,"customer_order_process");
	  	
		 private BiConsumer<Order, OrderAction> action;
		 private final String processName;
		 private final String message;
		 private final String title;
		 private Order.Status currentStatus;
		 private ArrayList<OrderAction> nextAllowed;
	  	
		 OrderAction(String title,String message, Order.Status status, String processName){
	  		this.message = message;
	  		this.title = title;
	  		this.currentStatus = status;
	  		this.processName = processName;
	  	}
	  	public void initRules(OrderStatus status) {
	  		this.nextAllowed = new ArrayList<OrderAction>();
	  		switch(status) {
	  		case CREATED:
	  			nextAllowed.add(SUBMIT_ORDER);
	  			break;
	  		case SUBMITTED:
	  			nextAllowed.add(REJECT_ORDER);
	  			nextAllowed.add(RELEASE_ORDER);
	  			break;
	  		case REJECTED:
	  			nextAllowed.add(SUBMIT_ORDER);
	  			break;
	  		case UPDATED:
	  			nextAllowed.add(ACKNOWLEDGE_UPDATE);
	  			nextAllowed.add(CANCEL_ORDER);
	  			break;
	  		case CANCELLED:
	  			break;
	  		case PRODUCTION:
	  			nextAllowed.add(UPDATE_ORDER);
	  			nextAllowed.add(SHIP_ORDER);
	  			nextAllowed.add(CANCEL_ORDER);
	  			break;
	  		case SHIPPING:
	  			nextAllowed.add(ACKNOWLEDGE_DELIVERY);
	  			break;
	  		case FULFILLED:
	  			break;
	  		default:
	  			break;
	  		}
	  	}
	  	
	  	public String processName() {
	  		return processName;
	  	}
	  	
	  	public Order.Status status() {
	  		return this.currentStatus;
	  	}
	  	public static OrderAction action(String actionTitle) {
	  		switch(actionTitle){
	  		case "Create Order":
	  			return OrderAction.CREATE_ORDER;
	  		case "Submit Order":
	  			return OrderAction.SUBMIT_ORDER;
	  		case "Reject Order":
	  			return OrderAction.REJECT_ORDER;
	  		case "Release Order":
	  			return OrderAction.RELEASE_ORDER;
	  		case "Update Order":
	  			return OrderAction.UPDATE_ORDER;
	  		case "Cancel Order":
	  			return OrderAction.CANCEL_ORDER;
	  		case "Accept Update":
	  			return OrderAction.ACKNOWLEDGE_UPDATE;
	  		case "Ship Order":
	  			return OrderAction.SHIP_ORDER;
	  		case "Accept Delivery":
	  			return OrderAction.ACKNOWLEDGE_DELIVERY;
	  		default:
	  			return null;
	  		}
	  	}
	  	public ArrayList<OrderAction> nextAllowed() {
	  		return nextAllowed;
	  	}
	  	public String message(){
	  		return message;
	  	}
	  	public final String title(){
	  		return title;
	  	}
	  	public Collection<String> names(OrderStatus status) {
	  		this.initRules(status);
	  		Collection<String> vs = new ArrayList<String>();
	  		for(OrderAction oa:this.nextAllowed()){
	  			vs.add(oa.title());
	  		}
	  		return vs;
	  	}
	  }
	
	 public static enum OrderStatus {
	    CREATED("blue","Created",Process.DEFAULT_CUSTOMER_PROCESS_KEY,OrderAction.CREATE_ORDER),
	    SUBMITTED("orange","Submitted",Process.DEFAULT_MANAGEMENT_PROCESS_KEY,OrderAction.SUBMIT_ORDER),
	    REJECTED("red","Rejected",Process.DEFAULT_CUSTOMER_PROCESS_KEY,OrderAction.REJECT_ORDER),
	    UPDATED("yellow","Updated",Process.DEFAULT_PRODUCTION_PROCESS_KEY,OrderAction.UPDATE_ORDER),
	    CANCELLED("grey","Cancelled",Process.DEFAULT_CUSTOMER_PROCESS_KEY,OrderAction.CANCEL_ORDER),
	    PRODUCTION("blue","Production",Process.DEFAULT_PRODUCTION_PROCESS_KEY,OrderAction.RELEASE_ORDER,OrderAction.ACKNOWLEDGE_UPDATE),
	    SHIPPING("blue","Shipping",Process.DEFAULT_PRODUCTION_PROCESS_KEY,OrderAction.SHIP_ORDER),
//	    TRANSIT("blue","Transit",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
	    FULFILLED("green","Fulfilled",Process.DEFAULT_CUSTOMER_PROCESS_KEY,OrderAction.ACKNOWLEDGE_DELIVERY);
//	    PAID("grey","Paid",MessageType.CustomerProcessMessage, CustomerOrderMessage._ordertransit),
//	    RECALLED("red","Recalled",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
//	    INTURNED("grey","Inturned",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
//	    DELETED("grey","Deleted",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
//	    CLEAN("black","Clean",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
//	    MODIFIED("black","Modified",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit),
//	    DIRTY("black","Dirty",MessageType.OrderProcessMessage, CustomerOrderMessage.opp_ordertransit);
	  	
	  	private String color = "black";
	  	private String intext;
	  	private String processKey;
	  	private ArrayList<OrderStatus> nextAllowed;
	  	private OrderAction[] currentAction;
	  	
	  	OrderStatus(String color,String intext, String processKey,OrderAction... action){
	  		this.color = color;
	  		this.intext = intext;
	  		this.processKey = processKey;
	  		this.currentAction = action;
	  	}
	  	public OrderAction[] action() {
	  		return currentAction;
	  	}
	
	  	public void initRules() {
	  		this.nextAllowed = new ArrayList<OrderStatus>();
	  		switch(this) {
	  		case CREATED:
	  			nextAllowed.add(CREATED);
	  			nextAllowed.add(SUBMITTED);
	  			nextAllowed.add(CANCELLED);
	  			break;
	  		case SUBMITTED:
	  			nextAllowed.add(SUBMITTED);
	  			nextAllowed.add(REJECTED);
	  			nextAllowed.add(PRODUCTION);
	  			nextAllowed.add(CANCELLED);
	  			break;
	  		case REJECTED:
	  			nextAllowed.add(REJECTED);
	  			nextAllowed.add(SUBMITTED);
	  			nextAllowed.add(CANCELLED);
	  			break;
	  		case UPDATED:
	  			nextAllowed.add(UPDATED);
	  			nextAllowed.add(PRODUCTION);
	  			nextAllowed.add(CANCELLED);
	  			break;
	  		case CANCELLED:
	  			nextAllowed.add(CANCELLED);
//	  			nextAllowed.add(DELETED);
	  			break;
	  		case PRODUCTION:
	  			nextAllowed.add(PRODUCTION);
	  			nextAllowed.add(UPDATED);
	  			nextAllowed.add(SHIPPING);
	  			break;
	  		case SHIPPING:
	  			nextAllowed.add(SHIPPING);
//	  			nextAllowed.add(TRANSIT);
	  			nextAllowed.add(FULFILLED);
	  			break;
//	  		case TRANSIT:
//	  			nextAllowed.add(TRANSIT);
//	  			nextAllowed.add(FULFILLED);
//	  			break;
	  		case FULFILLED:
	  			nextAllowed.add(FULFILLED);
//	  			nextAllowed.add(PAID);
	  			break;
//	  		case PAID:
//	  			nextAllowed.add(PAID);
//	  			nextAllowed.add(RECALLED);
//	  			break;
//	  		case RECALLED:
//	  			nextAllowed.add(RECALLED);
//	  			nextAllowed.add(PRODUCTION);
//	  			break;
//	  		case INTURNED:
//	  			nextAllowed.add(INTURNED);
//	  			nextAllowed.add(CANCELLED);
//	  			break;
	  		default:
	  			break;
	  		}
//	  		nextAllowed.add(INTURNED);
	  	}
	  	
	  	public String processKey() {
	  		return processKey;
	  	}
	  	
	  	public String color(){
	  		return color;
	  	}
	  	public ArrayList<OrderStatus> nextAllowed() {
	  		return nextAllowed;
	  	}
	  	public String inText(){
	  		return intext;
	  	}
	  	public static Collection<String> names(OrderStatus status) {
	  		Collection<String> vs = new ArrayList<String>();
	  		for(OrderStatus os:status.nextAllowed()){
	  			vs.add(os.name());
	  		}
	  		return vs;
	  	}
	  	public static Collection<String> names() {
	  		Collection<String> vs = new ArrayList<String>();
	  		for(OrderStatus os:values()){
	  			vs.add(os.name());
	  		}
	  		return vs;
	  	}
	  	public static String inText(Order o) {
	  		return OrderStatus.valueOf(o.status().name()).inText();
	  	}
	  	public static OrderStatus orderStatus(OrderAction action) {
	  		for(OrderStatus os: OrderStatus.values()) {
	  			for(OrderAction a:os.action()) {
	  				if(a.name().equals(action.name())){
	  					return os;
	  				}
	  			}
	  		}
	  		return null;
	  	}
	  }
	

		/**
	 * 
	 */
	 private static final long serialVersionUID = 6329475541570223642L;
	 private final TextField searchField = new TextField("", "Search orders");
   private final H2 header = new H2("Orders");
   private final Div total = new Div();
   private final Grid<Order> grid = new Grid<>();
   private final CreateOrderDialog form = new CreateOrderDialog(this::saveOrder);
   private Map<String,Registration> registrationForAction = new HashMap<String,Registration>();
    
	  public OrdersView() {
			initView();
			addSearchBar();
			addContent();
			updateView();
		}

	  private void initView() {
			addClassName("order-list");
			setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
			total.addClassName("total");
	  }

    private void addSearchBar() {
        Div viewToolbar = new Div();
        viewToolbar.addClassName("view-toolbar");

        searchField.setPrefixComponent(new Icon("lumo", "search"));
        searchField.addClassName("view-toolbar__search-field");
        searchField.addValueChangeListener(e -> updateView());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Button newButton = new Button("New Order", new Icon("lumo", "plus"));
        newButton.getElement().setAttribute("theme", "primary");
        newButton.addClassName("view-toolbar__button");
        newButton.addClickListener(e -> form.open(
        		new Order(
	        		Util.Config.instance().defaultBusinessId("Order"),
	        		Util.Config.instance().defaultBusinessKey("Order"),
	        		Util.Time.timestamp(
	        				Util.Time.nowPlus(
	        						Util.Config.instance().defaultByDateDays("Order"), 
	        						Util.Time.formatDateTime)))));

        viewToolbar.add(searchField, newButton);
        add(viewToolbar);
    }

    private void addContent() {
        VerticalLayout container = new VerticalLayout();
        container.setClassName("view-container");
        container.setAlignItems(Alignment.STRETCH);

        Div headerLine = new Div();
        headerLine.addClassName("header-line");
        headerLine.add(header, total);
        
        
        grid.addColumn(this::orderId).setHeader("Id").setWidth("20px").setResizable(false);
        grid.addColumn(this::orderBusinessId).setHeader("Order Number").setWidth("200px").setResizable(true);
        grid.addComponentColumn(this::orderByDate).setHeader("By Date").setWidth("50px").setResizable(true);
        grid.addComponentColumn(this::orderStatus).setHeader("Order Status").setWidth("30px").setResizable(false);
        grid.addComponentColumn(this::createAction).setHeader("Next Action").setWidth("30px").setResizable(false);
        grid.setSelectionMode(SelectionMode.NONE);
        container.add(headerLine, grid);
        add(container);
    }

    private String orderId(Order order) {
    	return String.valueOf(order.id());
//        List<Review> reviewsInCategory = OrderService.getInstance()
//                .findReviews(category.getName());
//        int sum = reviewsInCategory.stream().mapToInt(Review::getCount).sum();
//        return Integer.toString(sum);
    }

    private String orderBusinessId(Order order) {
    	return order.onlyBusinessId();
    }
    
    private TextField orderStatus(Order order) {
    	if(!Util._IN_PRODUCTION){String msg = "----[ORDERSTATUS("+order.status().name()+")]----";LOG.info(String.format(fmt, "orderStatus",msg));}
    	OrderStatus status = OrderStatus.valueOf(order.status().name());
    	status.initRules();
    	TextField tf = new TextField();
    	tf.setValue(order.status().name());
    	tf.addValueChangeListener(e -> updateStatus(order,e));
			tf.getStyle().set("background-color", getColor(order));
     return tf;
    }
    
    private ComboBox<String> createAction(Order order) {
    	if(!Util._IN_PRODUCTION){String msg = "----[ORDERSTATUS("+order.status().name()+")]----";LOG.info(String.format(fmt, "orderStatus",msg));}
    	OrderStatus status = OrderStatus.valueOf(order.status().name());
    	status.initRules();
    	OrderAction action = status.action()[0];
    	ComboBox<String> sel = new ComboBox<String>("",action.names(status));
//    	sel.setValue(action.title());
//    	sel.addCustomValueSetListener(e -> updateStatus(order,e));
    	Registration r = sel.addValueChangeListener(e -> updateAction(order,e));
    	registrationForAction.put(order.onlyBusinessId(),r);
			sel.getStyle().set("background-color", "#ff9966");
			if(action.status().name().equals(Order.Status.FULFILLED.name())
					|| action.status().name().equals(Order.Status.CANCELLED.name())) {
				sel.setEnabled(false);
				sel.getStyle().set("color", "#ffffff");
				sel.getStyle().set("background-color", "#ffffff");
			}
     return sel;
    }
    
    private Object updateAction(Order order, ValueChangeEvent<ComboBox<String>,String> e) {
    	String a = e.getSource().getValue();
			if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONTITLE("+a+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
			OrderAction action = OrderAction.action(a);
			if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONNAME("+action.name()+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
			if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONSTATUS("+action.status().name()+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
			if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONPROCESS("+action.processName()+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
			
			OrderStatus ostatus = OrderStatus.orderStatus(action);
			e.getSource().setFilteredItems(action.names(ostatus));
			order.setStatus(action.status());
			if(action.status().name().equals(Order.Status.FULFILLED.name())
					|| action.status().name().equals(Order.Status.CANCELLED.name())) {
				e.getSource().setEnabled(false);
				e.getSource().getStyle().set("background-color", "#ffffff");
			}
//			order.setProcesses(OrderRest.instance().loadProcessReferences(order.onlyBusinessId(), !Util._IN_PRODUCTION));
			String instanceId = "";
			for(Process<?> p: order.processes()) {
				if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONPROCESSNAME("+p.processName()+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
				if(p.processName().equals(action.processName())) {
					instanceId = p.instanceId();
					if(!Util._IN_PRODUCTION){String msg = "----[UPDATEACTIONINSTANCEID("+instanceId+")]----";LOG.info(String.format(fmt, "updateAction",msg));}
				}
			}
			ProcessControlRest.instance().completeTask(instanceId,Principal.Order.name(), order, !Util._IN_PRODUCTION);
			order.mirror(OrderRest.instance().loadByBusinessId(order.onlyBusinessId(), !Util._IN_PRODUCTION));
			order.setProcesses(OrderRest.instance().loadProcessReferences(order.onlyBusinessId(), !Util._IN_PRODUCTION));
			OrderBucket.instance().updateOrder(order);
			updateView();
			return null;
		}

    private Object updateStatus(Order order, ValueChangeEvent<TextField,String> e) {
			e.getSource().getStyle().set("background-color", getColor(order));
			return null;
		}

    public static String getColor(Order o ) {
    	OrderStatus s = OrderStatus.valueOf(o.status().name());
    	return s.color();
    }

		private DatePicker orderByDate(Order order) {
    	DatePicker byDate = new DatePicker();
      byDate.setRequired(true);
      byDate.setWeekNumbersVisible(true);
      byDate.setMax(Util.Time.getLocalDate(Util.Time.timestamp(Util.Time.nowPlus(100,Util.Time.formatDateTime))));
      byDate.setMin(Util.Time.getLocalDate(Util.Time.timestamp()));
      byDate.setValue(Util.Time.getLocalDate(order.byDate()));
      byDate.addValueChangeListener(e -> updateTimestamp(order, e));
      byDate.setEnabled(true);
    	return byDate;
    }
    
    private void updateView() {
    	OrderBucket.instance();
    	OrderList orders = OrderBucket.instance().findOrder(searchField.getValue());
    	grid.setItems(orders);
      if (searchField.getValue().length() > 0) {
          header.setText("Search for “"+ searchField.getValue() +"”");
      } else {
          header.setText("Orders");
      }
      total.setText(orders.size()+" in total.");
    }

    private Object updateTimestamp(Order o, ValueChangeEvent<DatePicker, LocalDate> e) {
    	String time = Util.Time.time(Util.Time.timestamp(Util.Time.now(Util.Time.formatDateTime)));
    	LocalDate date = e.getValue();
    	String datetime = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" "+time;
    	o.updateByDate(Util.Time.timestamp(datetime));
    	OrderRest.instance().update(o, !Util._IN_PRODUCTION);
    	OrderBucket.instance().updateOrder(o);
    	if(!Util._IN_PRODUCTION){String msg = "----[Date changed to "+datetime+"]----";LOG.info(String.format(fmt, "updateTimestamp",msg));}
    	updateView();
			return null;
		}

    private void saveOrder(Order order) {
			boolean log = !Util._IN_PRODUCTION;
			order.setBusinessKey(Util.Config.instance().defaultBusinessKey("Order"));
			Order o = OrderRest.instance().save(order,log);
		  OrderBucket.instance().saveOrder(o);
		  Process<?> p = ProcessControlRest.instance().startProcess(OrderProcess.DEFAULT_CUSTOMER_ORDER_PROCESS_KEY, order, Principal.Order, log);
		  o.processes().add(p);
		  Notification.show(
		            "Order successfully created.", 3000, Position.BOTTOM_START);
		  updateView();
		}
    
//    private static LocalDate getDate(Order order) {
//    	String date = Util.Time.date(order.date());
//    	return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//    }
//    
//    private static void setDate(Order order, LocalDate date) {
//			String d = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)+" "+Util.Time.time(Util.Time.timestamp(Util.Time.now(Util.Time.formatDateTime)));
//    	order.updateDate(Util.Time.timestamp(d));
//    	OrderBucket.instance().updateOrder(order);
//    }
}
