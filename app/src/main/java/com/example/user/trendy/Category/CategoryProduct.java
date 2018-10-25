package com.example.user.trendy.Category;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.trendy.BuildConfig;
import com.example.user.trendy.Category.ProductDetail.Filter.Filter1;
import com.example.user.trendy.Category.ProductDetail.ProductView;
import com.example.user.trendy.Filter.Filter_Fragment;
import com.example.user.trendy.ForYou.AllCollection.AllCollectionModel;
import com.example.user.trendy.ForYou.NewArrival.NewArrivalModel;
import com.example.user.trendy.ForYou.TopCollection.TopCollectionModel;
import com.example.user.trendy.ForYou.TopSelling.TopSellingModel;
import com.example.user.trendy.Interface.CartController;
import com.example.user.trendy.Interface.CommanCartControler;
import com.example.user.trendy.Interface.ProductClickInterface;
import com.example.user.trendy.Navigation;
import com.example.user.trendy.Product.ProductListModel;
import com.example.user.trendy.R;
import com.example.user.trendy.Util.Constants;
import com.example.user.trendy.Util.FilterSharedPreference;
import com.example.user.trendy.Util.SharedPreference;
import com.example.user.trendy.Util.WordUtils;
import com.google.zxing.common.StringUtils;
import com.shopify.buy3.GraphCall;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.GraphError;
import com.shopify.buy3.GraphResponse;
import com.shopify.buy3.HttpCachePolicy;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;
import com.shopify.graphql.support.Input;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.Base64;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CategoryProduct extends Fragment implements ProductAdapter.OnItemClick, View.OnClickListener, ProductClickInterface {
    GraphClient graphClient;
    RecyclerView recyclerView;
    ArrayList<ProductModel> productDetalList = new ArrayList<>();
    ArrayList<ProductModel> productDetalList1 = new ArrayList<>();
    ArrayList vendorarray = new ArrayList();
    ArrayList producttype = new ArrayList();
    ArrayList producttag = new ArrayList();
    ProductAdapter productAdapter;
    ProductAdapter productAdapter1;
    String productid = "", productidapi = "", price = "";
    String checkoutId;
    TextView category_title;
    TextView view1, subcategory, filter;
    TextView sublistname, all;
    public static int i = 0;
    public static boolean isViewWithCatalog = true;
    CategoryModel detail = new CategoryModel();
    String id, title = "";
    private RequestQueue mRequestQueue;
    String min_price = "", max_price = "", dynamicKey, dynamicKey1;
    ArrayList<String> selectedFilterList = new ArrayList<>();
    private String collectionname;
    CartController cartController;
    CommanCartControler commanCartControler;
    private int requestCount = 1, requestCount1 = 1;
    RequestQueue requestQueue;
    private String sortbykey;

    private Boolean isFilterData = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.category_product, container, false);

        graphClient = GraphClient.builder(getActivity())
                .shopDomain(BuildConfig.SHOP_DOMAIN)
                .accessToken(BuildConfig.API_KEY)
                .httpCache(new File(getActivity().getCacheDir(), "/http"), 10 * 1024 * 1024) // 10mb for http cache
                .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST.expireAfter(5, TimeUnit.MINUTES)) // cached response valid by default for 5 minutes
                .build();

        category_title = view.findViewById(R.id.category_title);
        filter = view.findViewById(R.id.filter);
        view1 = view.findViewById(R.id.view);
        filter.setOnClickListener(this);
        view1.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(getActivity());


        recyclerView = view.findViewById(R.id.product_recyclerview);


//        isViewWithCatalog = !isViewWithCatalog;


        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(layoutManager1);

        recyclerView.setLayoutManager(isViewWithCatalog ? new LinearLayoutManager(getActivity()) : new GridLayoutManager(getActivity(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


//        getProductByCollection(id.trim());


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        String category = getArguments().getString("collection");
        Log.e("categorycheck", category);
        if (category.trim().equals("topselling")) {
            TopSellingModel topSellingModel = (TopSellingModel) getArguments().getSerializable("category_id");
//            detail.setCollection(topSellingModel.getCollection());
            id = topSellingModel.getCollectionid().trim();
            title = topSellingModel.getCollectionTitle();
        } else if (category.trim().equals("bestcollection")) {
            TopCollectionModel topCollectionModel = (TopCollectionModel) getArguments().getSerializable("category_id");
            id = topCollectionModel.getCollectionid().trim();
            title = topCollectionModel.getCollectionTitle();
        } else if (category.trim().equals("api")) {
            CategoryModel detail = (CategoryModel) getArguments().getSerializable("category_id");
            Log.e("iud", detail.getId());
            id = detail.getId().trim();
            title = detail.getCollectiontitle();
        } else if (category.trim().equals("allcollection")) {
            AllCollectionModel allCollectionModel = (AllCollectionModel) getArguments().getSerializable("category_id");
            id = allCollectionModel.getId().trim();
            title = allCollectionModel.getTitle();

        } else if (category.trim().equals("newarrival")) {
            NewArrivalModel newArrivalModel = (NewArrivalModel) getArguments().getSerializable("category_id");
            id = newArrivalModel.getCollectionid().trim();
            title = newArrivalModel.getCollectionTitle();
        } else if (category.trim().equals("filter")) {

            Log.e("iddc", id);

        }
        if (title != null) {
            if (title.trim().length() != 0) {
                ((Navigation) getActivity()).getSupportActionBar().setTitle(title);

            }
        } else {
            ((Navigation) getActivity()).getSupportActionBar().setTitle("Categories");

        }

        category_title.setText(title);
        SharedPreference.saveData("collectionid", id, getActivity());

//        if (isFilterData=true) {
//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                    super.onScrollStateChanged(recyclerView, newState);
//                }
//
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//
//                    if (isLastItemDisplaying(recyclerView)) {
//                        //Calling the method getdata again
//                        postFilter();
//                    }
//                }
//            });
//        }
//        else {
//            requestCount=1;

//            collectionList(id.trim(),requestCount);


        if (isFilterData == true) {

            productAdapter1 = new ProductAdapter(getActivity(), productDetalList1, getFragmentManager(), this);
            recyclerView.setAdapter(productAdapter1);
            productAdapter1.notifyDataSetChanged();

        }else if(isFilterData==false) {
            onBackPressed();
            productAdapter = new ProductAdapter(getActivity(), productDetalList, getFragmentManager(), this);
            recyclerView.setAdapter(productAdapter);
            productAdapter.notifyDataSetChanged();
        }


        getData();


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isLastItemDisplaying(recyclerView)) {
                    //Calling the method getdata again
                    if (isFilterData == true) {
                        postFilter();
                    } else {
                        getData();
                    }
                }
            }
        });


//        }


    }

    public void getFilterData(String minprice, String maxprice, String sortby, String collectionid, ArrayList<String> selectedFilterLists, String CollectionName) {


        requestCount1 = 1;

        isFilterData = true;
        min_price = minprice;
        max_price = maxprice;
        sortbykey = sortby;
        id = collectionid;
        selectedFilterList = selectedFilterLists;
        FilterSharedPreference.saveArrayList(selectedFilterList,"filter",getActivity());
        Log.e("sort_by", sortby);
        if(sortby.equals("sortBy=min_price&order=desc")){
            sortby="Price : High to Low";
            FilterSharedPreference.saveData("sort",sortby,getActivity());
        }else{
            sortby="Price : Low to High";
            FilterSharedPreference.saveData("sort",sortby,getActivity());
        }

        dynamicKey1 = CollectionName;

        productDetalList1.clear();
//        productAdapter.notifyDataSetChanged();
        postFilter();
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (isLastItemDisplaying(recyclerView)) {
//                    //Calling the method getdata again
//                    postFilter();
//                }
//            }
//        });

    }


    public void onBackPressed()
    {

        if(FilterSharedPreference.getArrayList("filter",getActivity())!=null) {
            for (int i = 0; i < FilterSharedPreference.getArrayList("filter", getActivity()).size(); i++) {
                FilterSharedPreference.saveInSp(FilterSharedPreference.getArrayList("filter", getActivity()).get(i), false, getActivity());
            }
        }

        if(FilterSharedPreference.getData("sort",getActivity())!=null){
            FilterSharedPreference.saveInSp_sort(FilterSharedPreference.getData("sort",getActivity()),false,getActivity());

    }
    }

    public void postFilter() {
        isFilterData = true;
        requestQueue.add(postfilter(id, requestCount1));
        Log.d("request counter1", String.valueOf(requestCount1));
        requestCount1++;

    }


    private StringRequest postfilter(String id, int count) {
        StringRequest stringRequest = null;
        try {

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("collection_id", id);

            JSONObject price = new JSONObject();
            price.put("min_price", min_price);
            price.put("max_price", max_price);
            jsonBody.put("price", price);

            JSONArray food = new JSONArray();
            JSONObject food1 = new JSONObject();

            for (int i = 0; i < selectedFilterList.size(); i++) {
                String type = selectedFilterList.get(i).trim();
                Log.e("type", type);
                food1.put("name", dynamicKey1);
                food1.put("value", "Filter" + " " + dynamicKey1 + " " + type);
            }
            food.put(food1);
            jsonBody.put("food", food);


            Log.d("check JSON", jsonBody.toString());


            final String requestBody = jsonBody.toString();
            String a;
            if (sortbykey.trim().length() == 0) {
                a = "?page_size=10&page=" + count;
            } else {
                a = "?" + sortbykey.trim() + "&page_size=10&page=" + count;
            }

            stringRequest = new StringRequest(Request.Method.POST, Constants.filter_post + a, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        title = obj.getString("collection_name");
                        if (title.toLowerCase().equals("home page")) {
                            category_title.setText("Trending");
                        } else {
                            category_title.setText(title);
                        }

                        Log.e("title", "" + title);
                        Iterator keys = obj.keys();
                        Log.e("Keys", "" + String.valueOf(keys));

                        while (keys.hasNext()) {
                            dynamicKey = (String) keys.next();
                            Log.d("Dynamic Key", "" + dynamicKey);

                            JSONArray array = null;
                            try {
                                array = obj.getJSONArray(dynamicKey);

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object1 = array.getJSONObject(i);
                                    String title = object1.getString("title");
                                    String min_price = object1.getString("min_price");
                                    Log.e("image1", title + min_price);

                                    JSONArray array1 = object1.getJSONArray("images");
                                    for (int j = 0; j < array1.length(); j++) {
                                        JSONObject object = array1.getJSONObject(j);
                                        productidapi = object.getString("product_id");
                                        String imagesrc = object.getString("src");
                                        ProductModel productModel = new ProductModel(productidapi, min_price, title, imagesrc);
                                        Log.e("image", productidapi + imagesrc);
//                                        productDetalList1.add(productModel);
                                        productDetalList1.add(productModel);
                                    }


                                }

//                                productAdapter1 = new ProductAdapter(getActivity(), productDetalList1, getFragmentManager(), CategoryProduct.this);
//                                productAdapter = new ProductAdapter(getActivity(), productDetalList, getFragmentManager(), CategoryProduct.this);
//                                recyclerView.setAdapter(productAdapter);
                                productAdapter1.notifyDataSetChanged();
                            } catch (JSONException e1) {
                                e1.printStackTrace();

                            }


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
//                        return requestBody == null;
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    //TODO if you want to use the status code for any other purpose like to handle 401, 403, 404
                    String statusCode = String.valueOf(response.statusCode);
                    //Handling logic
                    return super.parseNetworkResponse(response);
                }

            };

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stringRequest;


    }

    private void getData() {
//        isFilterData=false;

        requestQueue.add(collectionList(id, requestCount));
        Log.d("request counter", String.valueOf(requestCount));
        requestCount++;
    }

    private StringRequest collectionList(String id, int count) {

        String URL = "http://...";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("collection_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("check JSON", jsonBody.toString());


        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.filter_post + "?page_size=10&page=" + count, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("VOLLEY", response);
                try {
                    JSONObject obj = new JSONObject(response);

                    Iterator keys = obj.keys();
                    Log.e("Keys", "" + String.valueOf(keys));

                    while (keys.hasNext()) {
                        String dynamicKey = (String) keys.next();
                        Log.d("Dynamic Key", "" + dynamicKey);

                        JSONArray array = null;
                        try {
                            array = obj.getJSONArray(dynamicKey);

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object1 = array.getJSONObject(i);
                                String title = object1.getString("title");
                                price = object1.getString("min_price");
                                Log.e("image1", title + price);

                                JSONArray array1 = object1.getJSONArray("images");
                                for (int j = 0; j < array1.length(); j++) {
                                    JSONObject object = array1.getJSONObject(j);
                                    productidapi = object.getString("product_id");
                                    String imagesrc = object.getString("src");
                                    ProductModel productModel = new ProductModel(productidapi, price, title, imagesrc);
                                    productDetalList.add(productModel);
                                }


                            }
                            productAdapter.notifyDataSetChanged();
                        } catch (JSONException e1) {
                            e1.printStackTrace();

                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
//                    return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");

                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                //TODO if you want to use the status code for any other purpose like to handle 401, 403, 404
                String statusCode = String.valueOf(response.statusCode);
                //Handling logic
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                // headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
//}
        };

        return stringRequest;
    }

    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.filter:


//                String ps2 = "dGVjaFBhC3M=";
//               byte[] tmp2 = Base64.decode(id,Base64.DEFAULT);
//
//                String val2 = new String(tmp2);
//                String[] str = val2.split("/");
//
////                String decodeid=Base64.decode(id,)
//                Log.d("str value", str[4]);

                Fragment fragment = new Filter_Fragment();
                Bundle bundle = new Bundle();
                bundle.putString("collectionid", id);
//                bundle.putStringArrayList("vendorarray", vendorarray);
//                bundle.putStringArrayList("producttag", producttag);
//                bundle.putStringArrayList("producttype", producttype);
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.home_container, fragment, "filter");
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
//                ft.addToBackStack(null);
                if (getFragmentManager().findFragmentByTag("filter") == null) {
                    ft.addToBackStack("filter");
                    ft.commit();
                } else {
                    ft.commit();
                }


                break;

            case R.id.view:
                isViewWithCatalog = !isViewWithCatalog;

                recyclerView.setLayoutManager(isViewWithCatalog ? new LinearLayoutManager(getActivity()) : new GridLayoutManager(getActivity(), 2));
                recyclerView.setAdapter(productAdapter);

                break;
        }
    }


    @Override
    public void clickProduct(String productid) {

        Log.d("product value", productid);
        Fragment fragment = new ProductView();
        Bundle bundle = new Bundle();
        bundle.putString("category", "ca_adapter");
        bundle.putString("product_id", productid);
        fragment.setArguments(bundle);
        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.home_container, fragment, "fragment");
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        if (getFragmentManager().findFragmentByTag("fragment") == null) {
            ft.addToBackStack("fragment");
            ft.commit();
        } else {
            ft.commit();
        }
    }

    @Override
    public void OnclickPlus(String productid) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cartController = new CartController(getActivity());
                commanCartControler = (CommanCartControler) cartController;
                commanCartControler.AddToCart(productid.trim());
//                productAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void OnclickWhislilst(String productid) {
        cartController = new CartController(getActivity());
        commanCartControler = (CommanCartControler) cartController;
        commanCartControler.AddToWhislist(productid.trim());
        Toast.makeText(getActivity(), "Added to Wishlist", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(String value) {
        productid = value;
        Log.d("productid", productid);
        cart(productid);
    }


    public void cart(String id) {

        Storefront.CheckoutCreateInput input = new Storefront.CheckoutCreateInput()
                .setLineItemsInput(Input.value(Arrays.asList(
                        new Storefront.CheckoutLineItemInput(3, new ID(id))
                )));

        Storefront.MutationQuery query = Storefront.mutation(mutationQuery -> mutationQuery

                .checkoutCreate(input, createPayloadQuery -> createPayloadQuery
                        .checkout(checkoutQuery -> checkoutQuery
                                .webUrl()
                        )
                        .userErrors(userErrorQuery -> userErrorQuery
                                .field()
                                .message()
                        )
                )
        );

        graphClient.mutateGraph(query).enqueue(new GraphCall.Callback<Storefront.Mutation>() {
            @Override
            public void onResponse(@NonNull GraphResponse<Storefront.Mutation> response) {
                if (!response.data().getCheckoutCreate().getUserErrors().isEmpty()) {
                    Log.e("data", response.data().getCheckoutCreate().toString());
                    // handle user friendly errors
                } else {
                    checkoutId = String.valueOf(response.data().getCheckoutCreate().getCheckout().getId());
                    String checkoutWebUrl = response.data().getCheckoutCreate().getCheckout().getWebUrl();
                    Log.d("checkoutId", checkoutId);
                    Log.d("checkoutWebUrl", checkoutWebUrl);
                    //    checkId();
                }
            }

            @Override
            public void onFailure(@NonNull GraphError error) {
                // handle errors
            }
        });
    }

}

