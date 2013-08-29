package com.continuuity.testsuite.purchaseanalytics.datamodel;

/**
 *  Represents total amount in inventory for a given object.
 */
public class Inventory {
  private final int productId;
  private final long quantity;

  public int getProductId() {
    return productId;
  }

  public long getQuantity() {
    return quantity;
  }

  public Inventory(int productId, long quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
}
