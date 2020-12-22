package com.android.task.ui.products

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.task.R
import com.android.task.data.model.Product
import com.android.task.databinding.ItemLoadMoreBinding
import com.android.task.databinding.ItemProductBinding
import java.util.*

class ProductsAdapter(private val mContext: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var searchTerm: String? = null
    private val products: MutableList<Product?>

    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_LOADING = 2
    }

    init {
        products = ArrayList()
    }

    fun setSearchTerm(searchTerm: String) {
        this.searchTerm = searchTerm
    }

    fun setItems(products: List<Product?>) {
        this.products.clear()
        this.products.addAll(products)
        notifyDataSetChanged()
    }

    fun addItems(products: List<Product?>) {
        val previousSize = this.products.size
        this.products.addAll(products)
        notifyItemRangeInserted(previousSize, products.size)
    }

    fun getItem(index: Int): Product? {
        return products[index]
    }

    fun insertItem(product: Product?) {
        products.add(product)
        notifyItemInserted(products.size - 1)
    }

    fun removeItem(index: Int) {
        if (index <= products.size - 1) {
            products.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (products[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val productBinding: ItemProductBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(mContext),
                    R.layout.item_product,
                    parent,
                    false
                )
            ProductViewHolder(productBinding)
        } else {
            val loadMoreBinding: ItemLoadMoreBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(mContext),
                    R.layout.item_load_more,
                    parent,
                    false
                )
            LoadMoreViewHolder(loadMoreBinding)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ProductViewHolder) {
            val product = products[position]

            viewHolder.productBinding.tvProductDescription.text = product!!.description
            viewHolder.productBinding.tvProductType.text = product.type
            viewHolder.productBinding.tvProductPrice.text = product.price.toString()

            val content = product.title.toLowerCase()
            val startIndex = content.indexOf(searchTerm!!)
            var endIndex = startIndex + searchTerm!!.length
            if (endIndex > content.length) {
                endIndex = content.length - 1
            }

            val spannableString = SpannableString(content)
            spannableString.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            viewHolder.productBinding.tvProductTitle.text = spannableString

        } else if (viewHolder is LoadMoreViewHolder) {
            viewHolder.loadMoreBinding.progressBar.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return if (products.isEmpty()) 0 else products.size
    }

    internal class ProductViewHolder(val productBinding: ItemProductBinding) :
        RecyclerView.ViewHolder(
            productBinding.root
        )

    internal class LoadMoreViewHolder(val loadMoreBinding: ItemLoadMoreBinding) :
        RecyclerView.ViewHolder(
            loadMoreBinding.root
        )
}