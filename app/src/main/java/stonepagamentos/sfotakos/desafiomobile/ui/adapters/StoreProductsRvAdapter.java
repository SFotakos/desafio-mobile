package stonepagamentos.sfotakos.desafiomobile.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import stonepagamentos.sfotakos.desafiomobile.model.Product;
import stonepagamentos.sfotakos.desafiomobile.R;
import stonepagamentos.sfotakos.desafiomobile.Util;

/**
 * Created by sfotakos on 21/03/2017.
 */

public class StoreProductsRvAdapter extends RecyclerView.Adapter<StoreProductsRvAdapter.StoreProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private IProductsAdapter mListener;

    // Lista dos produtos a serem adicionados, posicao final da animacao de adicao do carrinho.
    public StoreProductsRvAdapter(List<Product> productList, IProductsAdapter listener) {
        this.productList = productList;
        this.mListener = listener;
    }

    @Override
    public StoreProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_store_product, parent, false);
        return new StoreProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StoreProductViewHolder holder, int position) {
        final Context context = holder.productImage.getContext();
        final Product product = productList.get(position);

        holder.productName.setText(product.title);
        holder.productPrice.setText(Util.formatValueToDisplay(Integer.toString(product.price)));
        holder.productSalesman.setText(product.seller);

        Picasso.with(context)
                .load(product.thumbnailHd)
                .noPlaceholder()
                .into(holder.productImage);


        View.OnClickListener addActionListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.addProductAction.setEnabled(false);
                holder.productRoot.setEnabled(false);
                final Runnable addProductRunnable = new Runnable() {
                    @Override
                    public void run() {

                        int[] coordinates = new int[2];

                        holder.addProductAction.getLocationOnScreen(coordinates);
                        int posX = coordinates[0];
                        int posY = coordinates[1] - holder.addProductAction.getHeight();

//                        mListener.addProduct(holder.productImage, product);
                        mListener.addProduct(holder.addProductAction.getDrawable(), posX, posY, product);
                        holder.addProductAction.setEnabled(true);
                        holder.productRoot.setEnabled(true);
                    }
                };

                Util.clickEffectDefault(holder.addProductAction, addProductRunnable);
            }
        };

        holder.addProductAction.setOnClickListener(addActionListener);
        holder.productRoot.setOnClickListener(addActionListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class StoreProductViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.storeProduct_cardView)
        CardView productRoot;

        @BindView(R.id.productImage_imageView)
        ImageView productImage;
        @BindView(R.id.addProduct_imageView)
        ImageView addProductAction;
        @BindView(R.id.productName_textView)
        TextView productName;
        @BindView(R.id.productPrice_textView)
        TextView productPrice;
        @BindView(R.id.productSalesman_textView)
        TextView productSalesman;

        public StoreProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface IProductsAdapter {
        void addProduct(@NonNull Drawable drawable, int posX, int posY, @NonNull Product product);
    }
}
