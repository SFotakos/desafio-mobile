package stonepagamentos.sfotakos.desafiomobile.connection.response;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import stonepagamentos.sfotakos.desafiomobile.model.Product;
import stonepagamentos.sfotakos.desafiomobile.model.Transaction;

/**
 * Created by sfotakos on 22/03/2017.
 */

public class ConnectionController {

    private static String PRODUCTLISTURL = "https://raw.githubusercontent.com/stone-pagamentos/desafio-mobile/master/products.json";
    private static String SALEFINISHURL = "https://private-9b01e-moseisley.apiary-mock.com/payment";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static int fetchList(final IConnectionController_Fetch listener) {
        if (listener == null) return -99;

        new AsyncTask<Void, Void, ConnectionResponse>() {
            Response response = null;

            @Override
            protected ConnectionResponse doInBackground(Void... params) {
                
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(PRODUCTLISTURL).build();

                try {
                    response = client.newCall(request).execute();
                    if (response == null) return null;

                    int responseCode = response.code();
                    String responseMessage = response.message();
                    String responseBody = response.body().string();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Product>>() {
                    }.getType();
                    List<Product> products = gson.fromJson(responseBody, listType);

                    ProductFetchResponse productFetchResponse = new ProductFetchResponse();

                    productFetchResponse.productList = products;
                    productFetchResponse.responseCode = responseCode;
                    productFetchResponse.responseMessage = responseMessage;

                    return productFetchResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ConnectionResponse connectionResponse) {
                super.onPostExecute(connectionResponse);

                if (connectionResponse == null) {
                    connectionResponse = new ConnectionResponse();
                    connectionResponse.responseCode = -999;
                    connectionResponse.responseMessage = "Falha ao conectar";
                    listener.connectionError(connectionResponse);
                } else {
                    if (connectionResponse.responseCode == 200) {
                        ProductFetchResponse productFetchResponse = (ProductFetchResponse) connectionResponse;
                        listener.productFetchCallback(productFetchResponse);
                    } else {
                        listener.connectionError(connectionResponse);
                    }
                }
            }
        }.execute();

        return 0;
    }

    public static int finishSale(final Context context, final IConnectionController_Sell listener, final Transaction transaction) {
        if (transaction == null)
            return -99;

        new AsyncTask<Void, Void, ConnectionResponse>() {
            Response response = null;

            @Override
            protected ConnectionResponse doInBackground(Void... params) {

                Gson gson = new Gson();

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(JSON, gson.toJson(transaction));
                Request request = new Request.Builder()
                      .header("Content-Type", "application/json")
                      .post(requestBody)
                      .url(SALEFINISHURL)
                      .build();

                try {
                    response = client.newCall(request).execute();
                    if (response == null) return null;

                    int responseCode = response.code();
                    String responseMessage = response.message();
                    String responseBody = response.body().string();

                    FinishSaleResponse finishSaleResponse
                          = gson.fromJson(responseBody, FinishSaleResponse.class);

                    finishSaleResponse.responseCode = responseCode;
                    finishSaleResponse.responseMessage = responseMessage;

                    return finishSaleResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ConnectionResponse connectionResponse) {
                super.onPostExecute(connectionResponse);

                Realm.init(context);
                Realm realm = Realm.getDefaultInstance();
                
                if (connectionResponse == null) {
                    connectionResponse = new ConnectionResponse();
                    connectionResponse.responseCode = -999;
                    connectionResponse.responseMessage = "Falha ao conectar";
                    listener.connectionError(connectionResponse);
                } else {
                    if (connectionResponse.responseCode == 200) {
                        FinishSaleResponse finishSaleResponse = (FinishSaleResponse) connectionResponse;

                        String cardNumber = transaction.card_number;

                        realm.beginTransaction();

                        Transaction realmTransaction =
                              realm.createObject(Transaction.class, System.nanoTime() +
                                    transaction.card_number.substring(cardNumber.length() - 4));
                        realmTransaction.loadTransaction(transaction);
                        realmTransaction.card_number =
                              "XXXX XXXX XXXX" + cardNumber.substring(cardNumber.length() - 4);

                        realm.copyToRealmOrUpdate(realmTransaction);
                        realm.commitTransaction();

                        listener.saleCallback(finishSaleResponse);
                    } else {
                        listener.connectionError(connectionResponse);
                    }
                }
            }
        }.execute();

        return 0;
    }

    
    public interface IConnectionController_Default {
        void connectionError(ConnectionResponse connectionResponse);
    }

    public interface IConnectionController_Fetch extends IConnectionController_Default {
        void productFetchCallback(ProductFetchResponse productFetchResponse);
    }

    public interface IConnectionController_Sell extends IConnectionController_Default {
        void saleCallback(FinishSaleResponse finishSaleResponse);
    }


}
