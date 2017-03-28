package stonepagamentos.sfotakos.desafiomobile.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import stonepagamentos.sfotakos.desafiomobile.connection.response.ConnectionController;
import stonepagamentos.sfotakos.desafiomobile.connection.response.ConnectionResponse;
import stonepagamentos.sfotakos.desafiomobile.model.Product;
import stonepagamentos.sfotakos.desafiomobile.connection.response.ProductFetchResponse;
import stonepagamentos.sfotakos.desafiomobile.ui.adapters.StoreProductsRvAdapter;
import stonepagamentos.sfotakos.desafiomobile.R;
import stonepagamentos.sfotakos.desafiomobile.ShoppingCart;
import stonepagamentos.sfotakos.desafiomobile.Util;

public class StoreActivity extends AppCompatActivity implements ConnectionController.IConnectionController_Fetch, StoreProductsRvAdapter.IProductsAdapter {
    private int productColumns = 1;

    @BindView(R.id.storeRoot)
    RelativeLayout storeRoot;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title_textView)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_shoppingCart_imageView)
    ImageView toolbarCartAction;

    @BindView(R.id.loading_progressBar)
    ProgressBar progressBar;

    @BindView(R.id.store_recyclerView)
    RecyclerView storeProductsRv;

    ShoppingCart mShoppingCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);

        productColumns =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbarTitle.setText(getResources().getString(R.string.app_name));
        toolbarCartAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreActivity.this, TransactionActivity.class));
            }
        });

        storeProductsRv.setLayoutManager(new GridLayoutManager(this, productColumns));

        mShoppingCart = ShoppingCart.getInstance();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        progressBar.setVisibility(View.VISIBLE);
        ConnectionController.fetchList(this);
        updateBadge();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void connectionError(ConnectionResponse connectionResponse) {
        progressBar.setVisibility(View.GONE);
        String message = connectionResponse.responseCode + "\n" + connectionResponse.responseMessage;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void productFetchCallback(ProductFetchResponse productFetchResponse) {
        progressBar.setVisibility(View.GONE);
        if (productFetchResponse != null && productFetchResponse.productList != null)
            storeProductsRv.setAdapter(new StoreProductsRvAdapter(productFetchResponse.productList, this));
    }

    @Override
    public void addProduct(@NonNull Drawable drawable, int posX, int posY, @NonNull Product product) {

        float _24dp = Util.convertDpToPixel(24, this);

        int[] coordinates = new int[2];

        toolbarCartAction.getLocationOnScreen(coordinates);
        float destX = coordinates[0] + toolbarCartAction.getWidth() / 2;
        float destY = coordinates[1] - toolbarCartAction.getHeight() / 2;

        RelativeLayout.LayoutParams addToCartLayoutParams =
                new RelativeLayout.LayoutParams(Math.round(_24dp), Math.round(_24dp));

        final ImageView addToCartIcon = new ImageView(this);
        addToCartIcon.setLayoutParams(addToCartLayoutParams);

        addToCartIcon.setTranslationX(posX);
        addToCartIcon.setTranslationY(posY);

        addToCartIcon.setImageDrawable(drawable);
        addToCartIcon.setBackground(ContextCompat.getDrawable(this, R.drawable.circular_transparent_background));

        storeRoot.addView(addToCartIcon);

        final Animation dragToCartAnimation =
                Util.fromAtoB(0, 0, destX - posX, destY - posY, 800, new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        storeRoot.removeView(addToCartIcon);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

        addToCartIcon.startAnimation(dragToCartAnimation);
        mShoppingCart.addProduct(product);

        updateBadge();
    }

    private void updateBadge(){
        int productsAmount = mShoppingCart.productsAmount();
        if (productsAmount == 0) {
            toolbarCartAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_shopping_cart_white_36dp));
            return;
        }

        String badgeText = productsAmount > 99 ? "99+" : Integer.toString(productsAmount);
        BitmapDrawable badge =
                Util.writeOnDrawable(this, R.drawable.ic_shopping_cart_white_36dp, badgeText);

        toolbarCartAction.setImageDrawable(badge);
    }
}
