package com.android.task.ui.products

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.task.R
import com.android.task.data.model.SearchResponse
import com.android.task.data.repository.ProductsRepository
import com.android.task.databinding.FragmentProductsBinding
import com.android.task.helpers.VerticalRecyclerViewMargin
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class ProductsFragment : Fragment() {

    private lateinit var fragmentBinding: FragmentProductsBinding

    private lateinit var productsRepository: ProductsRepository
    private var compositeDisposable = CompositeDisposable()
    private lateinit var productsAdapter: ProductsAdapter
    private var lastSearchResponse: SearchResponse? = null

    private var offset = 0
    private val limit = 10
    private var isLoading = false

    companion object {
        private const val TAG = "ProductsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentBinding = FragmentProductsBinding.inflate(inflater, container, false)

        productsRepository = ProductsRepository(activity!!)
        productsAdapter = ProductsAdapter(activity!!)

        fragmentBinding.rvProducts.addItemDecoration(VerticalRecyclerViewMargin(24, 1))
        fragmentBinding.rvProducts.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        fragmentBinding.rvProducts.adapter = productsAdapter

        fragmentBinding.fabScrollTop.setOnClickListener {
            fragmentBinding.rvProducts.scrollToPosition(
                0
            )
        }

        return fragmentBinding.root
    }

    private fun observeSearchView(searchView: SearchView): Observable<String> {
        val subject = PublishSubject.create<String>()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                subject.onNext(newText)
                return true
            }
        })

        return subject
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.item_search)

        val searchView = searchItem.actionView as SearchView

        val queryDisposable = observeSearchView(searchView)
            .skip(1)
            .debounce(500, TimeUnit.MILLISECONDS)
            .map { query ->
                offset = 0
                query?.trim()?.toLowerCase() ?: ""
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ query: String ->

                when {
                    query.isEmpty() -> {
                        fragmentBinding.loadingIndicator.visibility = View.GONE
                        fragmentBinding.tvMessageStartTyping.visibility = View.VISIBLE
                        fragmentBinding.tvErrorMessage.visibility = View.GONE
                        fragmentBinding.rvProducts.visibility = View.GONE
                        fragmentBinding.fabScrollTop.visibility = View.GONE
                    }
                    query.length <= 2 -> {
                        fragmentBinding.loadingIndicator.visibility = View.GONE
                        fragmentBinding.tvMessageStartTyping.visibility = View.GONE
                        fragmentBinding.rvProducts.visibility = View.GONE
                        fragmentBinding.fabScrollTop.visibility = View.GONE
                        fragmentBinding.tvErrorMessage.visibility = View.VISIBLE
                        fragmentBinding.tvErrorMessage.setText(R.string.no_products_found)
                    }
                    else -> {
                        fragmentBinding.loadingIndicator.visibility = View.VISIBLE

                        val responseSingle = productsRepository.searchProductsAPI(
                            query, offset, limit
                        )
                        val disposable = responseSingle.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ searchResponse ->
                                Log.d(
                                    TAG,
                                    "onCreateOptionsMenu: searchResponse = " + Gson().toJson(
                                        searchResponse
                                    )
                                )

                                lastSearchResponse = searchResponse

                                fragmentBinding.loadingIndicator.visibility = View.GONE
                                fragmentBinding.tvMessageStartTyping.visibility = View.GONE
                                fragmentBinding.tvErrorMessage.visibility = View.GONE
                                fragmentBinding.rvProducts.visibility = View.VISIBLE
                                fragmentBinding.rvProducts.scrollToPosition(0)

                                productsAdapter.setSearchTerm(searchResponse.searchQuery)
                                productsAdapter.setItems(searchResponse.products)

                                if (searchResponse.products.isEmpty()) {
                                    fragmentBinding.rvProducts.visibility = View.GONE
                                    fragmentBinding.fabScrollTop.visibility = View.GONE
                                    fragmentBinding.tvErrorMessage.visibility = View.VISIBLE
                                    fragmentBinding.tvErrorMessage.setText(R.string.no_products_found)
                                } else {
                                    loadMore()
                                }
                            }, { e: Throwable? ->
                                Log.e(TAG, "onCreateOptionsMenu: ", e)

                                fragmentBinding.loadingIndicator.visibility = View.GONE
                                fragmentBinding.tvMessageStartTyping.visibility = View.GONE
                                fragmentBinding.rvProducts.visibility = View.GONE
                                fragmentBinding.fabScrollTop.visibility = View.GONE
                                fragmentBinding.tvErrorMessage.visibility = View.VISIBLE
                                fragmentBinding.tvErrorMessage.setText(R.string.error_occurred)
                            })
                        compositeDisposable.add(disposable)
                    }
                }

            }, { e: Throwable? ->
                Log.e(TAG, "onCreateOptionsMenu: ", e)

                fragmentBinding.loadingIndicator.visibility = View.GONE
                fragmentBinding.tvMessageStartTyping.visibility = View.GONE
                fragmentBinding.rvProducts.visibility = View.GONE
                fragmentBinding.fabScrollTop.visibility = View.GONE
                fragmentBinding.tvErrorMessage.visibility = View.VISIBLE
                fragmentBinding.tvErrorMessage.setText(R.string.error_occurred)
            })
        compositeDisposable.add(queryDisposable)
    }

    private fun loadMore() {
        fragmentBinding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    fragmentBinding.fabScrollTop.visibility = View.GONE
                } else {
                    fragmentBinding.fabScrollTop.visibility = View.VISIBLE
                }

                if (productsAdapter.itemCount >= 20 || offset >= 10 || lastSearchResponse!!.products.size < limit) {
                    return
                }

                if (!isLoading) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == productsAdapter.itemCount - 1) {
                        isLoading = true
                        productsAdapter.insertItem(null)

                        offset += limit

                        getMoreProducts(lastSearchResponse!!.searchQuery)
                    }
                }
            }
        })
    }

    private fun getMoreProducts(query: String) {
        fragmentBinding.loadingIndicator.visibility = View.VISIBLE

        val responseSingle = productsRepository.searchProductsAPI(
            query, offset, limit
        )
        val disposable = responseSingle.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ searchResponse ->
                Log.d(TAG, "getMoreProducts: searchResponse = " + Gson().toJson(searchResponse))

                fragmentBinding.loadingIndicator.visibility = View.GONE

                lastSearchResponse = searchResponse

                if (searchResponse.products.isEmpty()) {
                    if (productsAdapter.itemCount > 0 && productsAdapter.getItem(productsAdapter.itemCount - 1) == null) {
                        productsAdapter.removeItem(productsAdapter.itemCount - 1)
                        isLoading = false
                    }
                } else {
                    if (productsAdapter.itemCount > 0 && productsAdapter.getItem(productsAdapter.itemCount - 1) == null) {
                        productsAdapter.removeItem(productsAdapter.itemCount - 1)
                        isLoading = false
                    }

                    productsAdapter.addItems(searchResponse.products)
                }
            }, { e: Throwable? ->
                Log.e(TAG, "getMoreProducts: ", e)

                fragmentBinding.loadingIndicator.visibility = View.GONE
            })
        compositeDisposable.add(disposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        compositeDisposable.clear()
    }
}