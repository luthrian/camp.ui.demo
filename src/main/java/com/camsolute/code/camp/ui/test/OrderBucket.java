package com.camsolute.code.camp.ui.test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.camsolute.code.camp.lib.models.order.Order;
import com.camsolute.code.camp.lib.models.order.OrderList;
import com.camsolute.code.camp.lib.models.order.OrderMap;
import com.camsolute.code.camp.lib.models.order.OrderRest;
import com.camsolute.code.camp.lib.utilities.Util;

public class OrderBucket {

	private static final Logger LOG = LogManager.getLogger(OrderBucket.class);
	private static String fmt = "[%15s] [%s]";
	
  private static class SingletonHolder {

   private static boolean loaded = false;
   private static OrderBucket service;

   static final OrderBucket INSTANCE = getOrderService();
   private SingletonHolder() {
   }

      private static OrderBucket getOrderService() {
       if(!loaded) {
          final OrderBucket orderService = new OrderBucket();
          OrderList ol = OrderRest.instance().loadList(!Util._IN_PRODUCTION);
          for(Order o:ol) {
           o.processes().addAll(OrderRest.instance().loadProcessReferences(o.onlyBusinessId(), !Util._IN_PRODUCTION));
          }
          if(!ol.isEmpty()) {
           orderService.saveOrderList(ol);
          }
          loaded = true;
          return orderService;
       } else {
         return service;
       }
      }
			protected static void needsReload() {
       loaded = false;
      }
  }

  private static OrderMap orders = new OrderMap();

  /**
   * Declared private to ensure uniqueness of this Singleton.
   */
  private OrderBucket() {
  }

  public static OrderList orderList() {
  	OrderList ol = new OrderList();
  	for(Order o: orders.values()){
  		ol.add(o);
  	}
  	return ol;
  }
  	
  public static OrderMap orders() {
  	return orders;
  }
  
  public static OrderBucket instance() {
      return SingletonHolder.INSTANCE;
  }

  public OrderList findOrder(String filter) {

  	String normalizedFilter = filter.toLowerCase();

    OrderList ol = new OrderList();
    if(filter.isEmpty()) {
    	return orderList();
    }
    for(Order o: orders.values().stream().filter(
        order -> filterTextOf(order).contains(normalizedFilter))
        .sorted((r1, r2) -> r2.businessKey().compareTo(r1.businessKey()))
        .collect(Collectors.toList())){
    	ol.add(o);
    }
    return ol;
  }

  public Order findOrder(int id) {
      for(Order o: orders.values()) {
      	if(o.id() == id && o.id()!=0) {
      		return o;
      	}
      }
      return null;
  }

  private static String filterTextOf(Order order) {
      OrderDateToString dateConverter = new OrderDateToString();
      String filterableText = Stream
              .of(order.onlyBusinessId(),
              		order.businessKey(),
              		dateConverter.toPresentation(Util.Time.getLocalDate(order.date())))
              .collect(Collectors.joining("\t"));
      return filterableText.toLowerCase();
  }

  public boolean deleteOrder(Order order) {
      return orders.remove(order.onlyBusinessId()) != null;
  }

  public void saveOrder(Order o) {
      if (!orders.containsKey(o.onlyBusinessId())) {
          orders.put(o.onlyBusinessId(),o);
          SingletonHolder.needsReload();
      } 
  }

  public void saveOrderList(OrderList orderList) {
  	for(Order o: orderList) {
  		saveOrder(o);
  	}
  }

  public void updateOrder(Order o) {
      if(
    		OrderBucket.orders.containsKey(o.onlyBusinessId()) || findOrder(o.id())!=null ){
      	OrderRest.instance().update(o,!Util._IN_PRODUCTION);
      	if(!Util._IN_PRODUCTION){String msg = "----[Order updated. ]----";LOG.info(String.format(fmt, "updateOrder",msg));}
   			Order oldOrder = orders.remove(o.onlyBusinessId());
      	orders.put(o.onlyBusinessId(),o);
      }
  }

}
