package stonepagamentos.sfotakos.desafiomobile.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import stonepagamentos.sfotakos.desafiomobile.R;
import stonepagamentos.sfotakos.desafiomobile.ShoppingCart;
import stonepagamentos.sfotakos.desafiomobile.Util;
import stonepagamentos.sfotakos.desafiomobile.connection.response.ConnectionController;
import stonepagamentos.sfotakos.desafiomobile.connection.response.ConnectionResponse;
import stonepagamentos.sfotakos.desafiomobile.connection.response.FinishSaleResponse;
import stonepagamentos.sfotakos.desafiomobile.model.Product;
import stonepagamentos.sfotakos.desafiomobile.model.Transaction;
import stonepagamentos.sfotakos.desafiomobile.ui.adapters.ShoppingCartRvAdapter;

public class TransactionActivity extends AppCompatActivity implements ShoppingCartRvAdapter.IShoppingCartProductsAdapter, ConnectionController.IConnectionController_Sell{

    private static String ISCHECKOUT = "CHECKOUT";
    private static String HASFINISHED = "FINISHED";
    private static String RECEIPTMESSAGE = "RECEIPTMESSAGE";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.transactionContent)
    LinearLayout transactionContent;

    @BindView(R.id.receiptContent)
    LinearLayout receiptContent;

    //ShoppingCart
    @BindView(R.id.noItems_textView)
    TextView noItems;

    @BindView(R.id.shoppingCart_recyclerView)
    RecyclerView shoppingCartRv;

    @BindView(R.id.transactionTotal_textView)
    TextView cartTotal;

    //Payment Information

    @BindView(R.id.loading_progressBar)
    ProgressBar loadingBar;

    @BindView(R.id.payment_content)
    ScrollView paymentContent;

    @BindView(R.id.name_inputLayout)
    TextInputLayout nameInputLayout;

    @BindView(R.id.name_editText)
    TextInputEditText nameField;

    @BindView(R.id.cardNumber_inputLayout)
    TextInputLayout cardNumberInputLayout;

    @BindView(R.id.cardNumber_editText)
    TextInputEditText cardNumberField;

    @BindView(R.id.cardExpMonth_inputLayout)
    TextInputLayout expMonthInputLayout;

    @BindView(R.id.cardExpMonth_editText)
    TextInputEditText expMonthField;

    @BindView(R.id.cardExpYear_inputLayout)
    TextInputLayout expYearInputLayout;

    @BindView(R.id.cardExpYear_editText)
    TextInputEditText expYearField;

    @BindView(R.id.cvv_inputLayout)
    TextInputLayout cvvInputLayout;

    @BindView(R.id.cvv_editText)
    TextInputEditText cvvField;

    //Transaction
    @BindView(R.id.transactionAction_button)
    Button actionBtn;

    //Receipt
    @BindView(R.id.receiptMessage_textView)
    TextView receiptMessage;

    private ShoppingCart mShoppingCart;
    private ShoppingCartRvAdapter shoppingCartAdapter;

    private boolean isCheckout = false;
    private boolean hasFinished = false;

    private String receiptMessageString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));

        switchCheckoutContent(false);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mShoppingCart = ShoppingCart.getInstance();

        if (mShoppingCart.getShoppingCart() == null || mShoppingCart.getShoppingCart().size() == 0) {
            noItems.setVisibility(View.VISIBLE);
            transactionContent.setVisibility(View.GONE);
        } else {
            shoppingCartAdapter = new ShoppingCartRvAdapter(mShoppingCart.getShoppingCart(), this);
            shoppingCartRv.setLayoutManager(new LinearLayoutManager(this));
            shoppingCartRv.setAdapter(shoppingCartAdapter);
        }

        cartTotal.setText(Util.formatValueToDisplay(Integer.toString(mShoppingCart.getTotal())));

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCheckout) {
                    if (validateFields()) {
                        actionBtn.setEnabled(false);

                        Transaction transaction = new Transaction();
                        transaction.card_holder_name = nameField.getText().toString();
                        transaction.card_number = cardNumberField.getText().toString();
                        transaction.card_exp_date = expMonthField.getText().toString() + "/" + expYearField.getText().toString();
                        transaction.card_cvv = cvvField.getText().toString();
                        transaction.transaction_value = cartTotal.getText().toString();

                        loadingBar.setVisibility(View.VISIBLE);
                        ConnectionController.finishSale(TransactionActivity.this,
                              TransactionActivity.this, transaction);
                    }
                } else {
                    switchCheckoutContent(true);
                }
            }
        });

        cardNumberField.addTextChangedListener(Util.maskTextWatcher("#### #### #### ####", cardNumberField));

//        mockup();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ISCHECKOUT, isCheckout);
        outState.putBoolean(HASFINISHED, hasFinished);
        outState.putString(RECEIPTMESSAGE, receiptMessageString);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isCheckout = savedInstanceState.getBoolean(ISCHECKOUT);
        hasFinished = savedInstanceState.getBoolean(HASFINISHED);
        receiptMessageString = savedInstanceState.getString(RECEIPTMESSAGE);
        receiptMessage.setText(receiptMessageString);
        switchCheckoutContent(isCheckout);
        if (hasFinished)
            showReceipt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Util.hideKeyboard(toolbar);
        if (hasFinished) {
            finishUp();
            return;
        }

        if (isCheckout) {
            isCheckout = false;
            switchCheckoutContent(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void addProduct(Product product, int position) {
        mShoppingCart.addProduct(product);
        shoppingCartAdapter.notifyItemChanged(position);
        cartTotal.setText(Util.formatValueToDisplay(Integer.toString(mShoppingCart.getTotal())));
    }

    @Override
    public void removeProduct(Product product, int position) {
        boolean bSubtracted = mShoppingCart.removeProduct(product);
        if (bSubtracted) {
            shoppingCartAdapter.notifyItemChanged(position);
        } else {
            shoppingCartAdapter.notifyItemRemoved(position);
            shoppingCartAdapter.notifyItemRangeChanged(position, mShoppingCart.getShoppingCart().size());
        }
        cartTotal.setText(Util.formatValueToDisplay(Integer.toString(mShoppingCart.getTotal())));
    }


    private boolean validateFields() {
        boolean bValid = true;

        if (nameField.getText() == null || nameField.getText().toString().trim().isEmpty() ||
              nameField.getText().toString().trim().length() < 5) {
            nameInputLayout.setError("Nome inválido");
            bValid = false;
        } else {
            nameInputLayout.setErrorEnabled(false);
            nameInputLayout.setError(null);
        }

        if (cardNumberField.getText() == null || cardNumberField.getText().toString().trim().isEmpty() ||
              Util.unmaskToNumberOnly(cardNumberField.getText().toString()).length() < 16) {
            cardNumberInputLayout.setError("Cartão inválido");
            bValid = false;
        } else {
            cardNumberInputLayout.setErrorEnabled(false);
            cardNumberInputLayout.setError(null);
        }

        if (expMonthField.getText() == null ||
              expMonthField.getText().toString().isEmpty() ||
              Integer.valueOf(expMonthField.getText().toString()) > 12 ||
              Integer.valueOf(expMonthField.getText().toString()) < 1) {
            expMonthInputLayout.setError("Mês inválido");
            bValid = false;
        } else {
            expMonthInputLayout.setErrorEnabled(false);
            expMonthInputLayout.setError(null);
        }

        if (expYearField.getText() == null || expYearField.getText().toString().isEmpty() ||
              expYearField.getText().toString().length() < 2) {
            expYearInputLayout.setError("Ano inválido");
            bValid = false;
        } else {
            expYearInputLayout.setErrorEnabled(false);
            expYearInputLayout.setError(null);
        }

        //TODO validar se a data é anterior a nossa

        if (cvvField.getText() == null || cvvField.getText().toString().isEmpty() ||
              cvvField.getText().toString().length() < 3) {
            cvvInputLayout.setError("Código de segurança inválido");
            bValid = false;
        } else {
            cvvInputLayout.setErrorEnabled(false);
            cvvInputLayout.setError(null);
        }


        return bValid;
    }

    @Override
    public void connectionError(ConnectionResponse connectionResponse) {
        actionBtn.setEnabled(true);
        loadingBar.setVisibility(View.GONE);

        String message = connectionResponse.responseCode + "\n" + connectionResponse.responseMessage;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void saleCallback(@NonNull FinishSaleResponse finishSaleResponse) {
        actionBtn.setEnabled(true);
        loadingBar.setVisibility(View.GONE);

        if (finishSaleResponse.responseCode == 200) {
            receiptMessageString = finishSaleResponse.transactionRespMessage;
            receiptMessage.setText(receiptMessageString);
            showReceipt();
        } else {
            String message = finishSaleResponse.responseCode + "\n" + finishSaleResponse.responseMessage;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();  //TODO showError
        }
    }

//    private void mockup() {
//        nameField.setText("Name Surname");
//        cardNumberField.setText("1111222233334444");
//        expMonthField.setText("01");
//        expYearField.setText("19");
//        cvvField.setText("340");
//    }

    private void switchCheckoutContent(boolean checkout) {
        isCheckout = checkout;
        if (checkout) {
            toolbar.setTitle(getResources().getString(R.string.transaction_activity_payment_title));
            actionBtn.setText(getResources().getString(R.string.transaction_activity_action_pay_text));
            shoppingCartRv.setVisibility(View.GONE);
            paymentContent.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle(getResources().getString(R.string.transaction_activity_cart_title));
            actionBtn.setText(getResources().getString(R.string.transaction_activity_action_proceed_text));
            shoppingCartRv.setVisibility(View.VISIBLE);
            paymentContent.setVisibility(View.GONE);
        }
    }

    private void showReceipt() {
        hasFinished = true;
        toolbar.setTitle(getResources().getString(R.string.receipt_title));
        transactionContent.setVisibility(View.GONE);
        receiptContent.setVisibility(View.VISIBLE);
        Util.hideKeyboard(toolbar);
    }

    private void finishUp() {
        hasFinished = false;
        isCheckout = false;

        mShoppingCart.setShoppingCart(new ArrayList<Product>());
        Util.hideKeyboard(toolbar);
        finish();
    }
}
