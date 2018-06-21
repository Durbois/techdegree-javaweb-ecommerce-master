package com.acme.ecommerce.util;

import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Function;

/**
 * Created by Rumy on 8/31/2017.
 */
public class Utils {


    //converts the subtotal to string
    public static Function<BigDecimal, String> currencyFormatter = num -> {
        Locale argLocale = Locale.US;
        Currency currentCurrency = Currency.getInstance(argLocale);
        NumberFormat currencyFormatter =
                NumberFormat.getCurrencyInstance(argLocale);
        return String.format("%s",
                currencyFormatter.format(num)
        );
    };


    public static BigDecimal calcSubtotal(Purchase purchase){
        return purchase.getProductPurchases().stream()
                .map(pp->(pp.getProduct().getPrice()).multiply(new BigDecimal(pp.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }


    public static String formatCartSubTotal(ShoppingCart cart) {
        String s="";
        if (cart!=null) {
            Purchase purchase = cart.getPurchase();
            if (purchase != null && !purchase.getProductPurchases().isEmpty()) {

                BigDecimal subTotal = purchase.getProductPurchases().stream()
                        .map(pp -> (pp.getProduct().getPrice()).multiply(new BigDecimal(pp.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                s = currencyFormatter.apply(subTotal);
            }
        }
        return s;
   }
}
