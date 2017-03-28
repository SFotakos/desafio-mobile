package stonepagamentos.sfotakos.desafiomobile.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import stonepagamentos.sfotakos.desafiomobile.R;
import stonepagamentos.sfotakos.desafiomobile.Util;
import stonepagamentos.sfotakos.desafiomobile.model.Product;

/**
 * Created by sfotakos on 21/03/2017.
 */

public class ShoppingCartRvAdapter extends RecyclerView.Adapter<ShoppingCartRvAdapter.ShoppingCartProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private IShoppingCartProductsAdapter mListener;

    public ShoppingCartRvAdapter(List<Product> productList, IShoppingCartProductsAdapter listener) {
        this.productList = productList;
        this.mListener = listener;
    }

    @Override
    public ShoppingCartProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_shopping_cart_product, parent, false);
        return new ShoppingCartProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShoppingCartProductViewHolder holder, final int position) {
        final Context context = holder.productImage.getContext();
        final Product product = productList.get(position);
        int productPrice = product.price * product.amount;

        holder.productName.setText(product.title);
        holder.productPrice.setText(Util.formatValueToDisplay(Integer.toString(product.price)));
        holder.productAmount.setText(Integer.toString(product.amount));
        holder.productTotalPrice.setText(Util.formatValueToDisplay(Integer.toString(productPrice)));

        Picasso.with(context)
                .load(product.thumbnailHd)
                .noPlaceholder()
                .into(holder.productImage);

        holder.addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                Runnable addProduct = new Runnable() {
                    @Override
                    public void run() {
                        mListener.addProduct(product, position);
                        v.setEnabled(true);
                    }
                };

                Util.clickEffectDefault(v, addProduct);
            }
        });

        holder.removeProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                Runnable removeProduct = new Runnable() {
                    @Override
                    public void run() {
                        mListener.removeProduct(product, position);
                        v.setEnabled(true);
                    }
                };

                Util.clickEffectDefault(v, removeProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ShoppingCartProductViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.productImage_imageView)
        ImageView productImage;
        @BindView(R.id.productName_textView)
        TextView productName;
        @BindView(R.id.productPrice_textView)
        TextView productPrice;
        @BindView(R.id.productAmount_textView)
        TextView productAmount;
        @BindView(R.id.productTotalPrice_textView)
        TextView productTotalPrice;
        @BindView(R.id.addProduct_imageView)
        ImageView addProduct;
        @BindView(R.id.removeProduct_imageView)
        ImageView removeProduct;

        public ShoppingCartProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface IShoppingCartProductsAdapter {
        void addProduct(Product product, int position);
        void removeProduct(Product product, int position);
    }
}
