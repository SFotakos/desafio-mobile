package stonepagamentos.sfotakos.desafiomobile;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import stonepagamentos.sfotakos.desafiomobile.model.Product;

/**
 * Created by sfotakos on 23/03/2017.
 */

public class ShoppingCart {

    private static ShoppingCart mShoppingCart = new ShoppingCart();

    private List<Product> shoppingCartList = new ArrayList<>();
    private Integer productsAmount = 0;

    private ShoppingCart() {

    }

    public static ShoppingCart getInstance() {
        return mShoppingCart;
    }

    public List<Product> getShoppingCart() {
        return shoppingCartList;
    }

    public void setShoppingCart(List<Product> shoppingCartList) {
        this.shoppingCartList = shoppingCartList;
        this.productsAmount = 0;

        for (Product product : shoppingCartList) {
            productsAmount += product.amount;
        }
    }

    public void addProduct(@NonNull Product product) {
        boolean exists = false;
        productsAmount++;


        for (Product auxProduct : shoppingCartList) {
            if (auxProduct.title.equals(product.title) &&
                    auxProduct.price.intValue() == product.price.intValue()) {
                auxProduct.amount++;
                exists = true;
            }
        }

        if (!exists)
            shoppingCartList.add(product);
    }

    public boolean removeProduct(@NonNull Product product) {
        boolean exists = false;
        productsAmount--;

        for (Product auxProduct : shoppingCartList) {
            if (auxProduct.title.equals(product.title) &&
                    auxProduct.price.intValue() == product.price.intValue() && product.amount > 1) {
                auxProduct.amount--;
                exists = true;
            }
        }

        if (!exists)
            shoppingCartList.remove(product);

        return exists;
    }

    public Integer getTotal() {
        Integer total = 0;
        for (Product product : shoppingCartList) {
            total += product.price * product.amount;
        }

        return total;
    }

    public Integer productsAmount() {
        return productsAmount;
    }
}
