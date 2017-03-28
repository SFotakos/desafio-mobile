package stonepagamentos.sfotakos.desafiomobile.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sfotakos on 23/03/2017.
 */

public class Transaction extends RealmObject {

    @PrimaryKey
    public int id;

    public String card_number;
    public String card_holder_name;
    public String card_exp_date;
    public String card_cvv;
    public String transaction_value;

    public void loadTransaction(Transaction transaction){
        this.card_number = transaction.card_number;
        this.card_holder_name = transaction.card_holder_name;
        this.card_exp_date = transaction.card_exp_date;
        this.card_cvv = transaction.card_cvv;
        this.transaction_value = transaction.transaction_value;
    }

    public Transaction(){

    }
}
