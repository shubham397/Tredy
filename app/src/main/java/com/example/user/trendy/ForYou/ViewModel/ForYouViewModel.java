package com.example.user.trendy.ForYou.ViewModel;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.trendy.BuildConfig;
import com.example.user.trendy.ForYou.AllCollection.AllCollectionModel;
import com.example.user.trendy.ForYou.GroceryHome.GroceryHomeModel;
import com.example.user.trendy.ForYou.NewArrival.NewArrivalModel;
import com.example.user.trendy.ForYou.TopCollection.TopCollectionModel;
import com.example.user.trendy.ForYou.TopSelling.TopSellingModel;
import com.example.user.trendy.Util.Constants;
import com.shopify.buy3.GraphCall;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.GraphError;
import com.shopify.buy3.GraphResponse;
import com.shopify.buy3.HttpCachePolicy;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ForYouViewModel extends ViewModel {

    Context mContext;
    private RequestQueue mRequestQueue;
    ForyouInterface foryouInterface;
    ArrayList<TopSellingModel> topSellingModelArray = new ArrayList<>();
    ArrayList<TopCollectionModel> topCollectionModelArray = new ArrayList<>();
    ArrayList<NewArrivalModel> newArrivalModelArray = new ArrayList<>();
    ArrayList<AllCollectionModel> allCollectionModelArrayList = new ArrayList<>();
    private ArrayList<GroceryHomeModel> GroceryHomeModelArrayList = new ArrayList<>();
    private String collectionid, title, id, price, image;
    private String collectionname;
    ArrayList<String> bannerlist = new ArrayList<>();
    GraphClient graphClient;

    public ForYouViewModel(Context mContext, ForyouInterface foryouInterface) {
        this.mContext = mContext;
        this.foryouInterface = foryouInterface;
        banner();
        collectionList();
        collectionList1();
        getCollection();
    }

    public ForYouViewModel(Context mContext) {
        this.mContext = mContext;
    }

    private void collectionList() {
        mRequestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.navigation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {


                            JSONObject obj = new JSONObject(response);
                            allCollectionModelArrayList.clear();
                            JSONObject menu = obj.getJSONObject("menu");
                            String title = menu.getString("title");
                            JSONArray jsonarray = menu.getJSONArray("items");

                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject collectionobject = jsonarray.getJSONObject(i);


                                String id = "" + collectionobject.getString("subject_id");
                                String collectiontitle = collectionobject.getString("title");
                                String nav = collectionobject.getString("type");

                                if (id.trim().length() != 0) {
                                    String text = "gid://shopify/Collection/" + id.trim();

                                    String converted = Base64.encodeToString(text.toString().getBytes(), Base64.DEFAULT);
                                }

                                if (nav.trim().equals("http") || nav.trim().equals("collection")) {


                                    JSONArray jsonarray1 = collectionobject.getJSONArray("items");
                                    if (jsonarray1.length() != 0) {
                                        for (int j = 0; j < jsonarray1.length(); j++) {
                                            JSONObject subcollectionobject = jsonarray1.getJSONObject(j);

                                            String subid = "" + subcollectionobject.getString("subject_id");
                                            String subcollectiontitle = subcollectionobject.getString("title");
                                            String type = subcollectionobject.getString("type");
                                            if (type.trim().equals("collection")) {
                                                String image1 = subcollectionobject.getString("image");
                                                if (!subid.trim().equals("null")) {


                                                    if (subid.trim().length() != 0) {
                                                        String text = "gid://shopify/Collection/" + subid.trim();

                                                        String converted = Base64.encodeToString(text.toString().getBytes(), Base64.DEFAULT);
                                                        Log.e("coverted", converted.trim());
                                                    }

                                                    AllCollectionModel allCollectionModel = new AllCollectionModel(subid, image1, subcollectiontitle);
                                                    allCollectionModelArrayList.add(allCollectionModel);
                                                }


                                            }

                                        }
                                        foryouInterface.allcollection(allCollectionModelArrayList);
//
                                    }

                                }


                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "" + error.getMessage());
//                        progressDialog.dismiss();
                    }
                }) {

            @Override
            protected void deliverResponse(String response) {
                Log.e("ree", " " + response);
                super.deliverResponse(response);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.e("reen", " " + response.headers);
                return super.parseNetworkResponse(response);
            }
        };
        stringRequest.setTag("categories_page");
        // VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

        int socketTimeout = 10000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        mRequestQueue.add(stringRequest);

    }


    private void collectionList1() {

        RequestQueue mRequestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.collectionid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject obj = new JSONObject(response);
                            topSellingModelArray.clear();
                            topCollectionModelArray.clear();
                            newArrivalModelArray.clear();


                            Iterator keys = obj.keys();
                            Log.e("Keys", "" + String.valueOf(keys));

                            while (keys.hasNext()) {
                                String dynamicKey = (String) keys.next();
                                Log.d("Dynamic Key", "" + dynamicKey);

                                JSONArray array = null;
                                try {

                                    array = obj.getJSONArray(dynamicKey);


                                    for (int i = 0; i < array.length(); i++) {
                                        Log.e("inti", String.valueOf(i));
                                        JSONObject object1 = array.getJSONObject(i);
                                        collectionid = object1.getString("id");
                                        collectionname = object1.getString("title");
                                        if (collectionname.trim().toLowerCase().equals("home page")) {
                                            collectionname = "Trending";
                                        }

                                        JSONArray array1 = object1.getJSONArray("products");
                                        for (int j = 0; j < array1.length(); j++) {
                                            JSONObject objec = array1.getJSONObject(j);

                                            title = objec.getString("title");
                                            if (title.trim().toLowerCase().equals("home page")) {
                                                title = "Treding";
                                            }
                                            JSONArray varientsarray = objec.getJSONArray("variants");
                                            for (int k = 0; k < varientsarray.length(); k++) {
                                                JSONObject objec1 = varientsarray.getJSONObject(k);

                                                id = objec1.getString("product_id");
                                                price = objec1.getString("price");

                                            }
                                            JSONArray array2 = objec.getJSONArray("images");
                                            for (int l = 0; l < array2.length(); l++) {
                                                JSONObject objec1 = array2.getJSONObject(l);
                                                image = objec1.getString("src");
                                            }
                                            if (i == 0) {
                                                TopSellingModel topSellingModel = new TopSellingModel(id, title, price, image, collectionname);
                                                Log.e("product", title);
                                                topSellingModel.setCollectionid(collectionid);
                                                topSellingModelArray.add(topSellingModel);

                                            } else if (i == 1) {
                                                TopCollectionModel topCollectionModel = new TopCollectionModel(id, title, price, image, collectionname);
                                                topCollectionModel.setCollectionid(collectionid);
                                                topCollectionModelArray.add(topCollectionModel);

//                                                resultCallBackInterface.bestCollection(collectionid, id, title, price, image, collectionname);
                                            } else if (i == 2) {
                                                NewArrivalModel newArrivalModel = new NewArrivalModel(id, title, price, image, collectionname);
                                                newArrivalModel.setCollectionid(collectionid);
                                                newArrivalModelArray.add(newArrivalModel);

//                                                resultCallBackInterface.newArrivals(collectionid, id, title, price, image, collectionname);
                                            }

                                        }
                                    }
                                    foryouInterface.collectionlist(topSellingModelArray, topCollectionModelArray, newArrivalModelArray);
//                                    resultCallBackInterface.topSelling(topSellingModelArray);
//                                    resultCallBackInterface.bestCollection(topCollectionModelArray);
//                                    resultCallBackInterface.newArrivals(newArrivalModelArray);
//                                    progressDialog.dismiss();
                                } catch (JSONException e1) {
                                    e1.printStackTrace();

                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        progressDialog.dismiss();
                    }
                }) {

        };
        stringRequest.setTag("categories_page");
        // VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

        int socketTimeout = 10000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        mRequestQueue.add(stringRequest);

    }

    private void banner() {

        mRequestQueue = Volley.newRequestQueue(mContext);
        JsonArrayRequest request = new JsonArrayRequest(Constants.banner,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        bannerlist.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String bannerimage = jsonObject.getString("image_src");
                                bannerlist.add(bannerimage);

                                foryouInterface.bannerlist(bannerlist);
                            } catch (JSONException e) {

                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

        };
        request.setTag("categories_page");
        // VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

        int socketTimeout = 10000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mRequestQueue.add(request);

    }

    private void getCollection() {
        String id = "58881703997";
        String text = "gid://shopify/Collection/" + id.trim();
        String converted = Base64.encodeToString(text.trim().getBytes(), Base64.DEFAULT);

        GroceryHomeModelArrayList.clear();

        graphClient = GraphClient.builder(mContext)
                .shopDomain(BuildConfig.SHOP_DOMAIN)
                .accessToken(BuildConfig.API_KEY)
                .httpCache(new File(mContext.getCacheDir(), "/http"), 10 * 1024 * 1024) // 10mb for http cache
                .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST.expireAfter(5, TimeUnit.MINUTES)) // cached response valid by default for 5 minutes
                .build();

        Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                .node(new ID(converted.trim()), nodeQuery -> nodeQuery
                        .onCollection(collectionQuery -> collectionQuery
                                .title()
                                .products(arg -> arg.first(10), productConnectionQuery -> productConnectionQuery
                                        .edges(productEdgeQuery -> productEdgeQuery
                                                .node(productQuery -> productQuery
                                                        .title()
                                                        .productType()
                                                        .description()
                                                        .descriptionHtml()
                                                        .images(arg -> arg.first(10), imageConnectionQuery -> imageConnectionQuery
                                                                .edges(imageEdgeQuery -> imageEdgeQuery
                                                                        .node(imageQuery -> imageQuery
                                                                                .src()
                                                                        )
                                                                )
                                                        )
                                                        .tags()
                                                        .options(option -> option.name())
                                                        .variants(arg -> arg.first(10), variantConnectionQuery -> variantConnectionQuery
                                                                .edges(variantEdgeQuery -> variantEdgeQuery
                                                                        .node(productVariantQuery -> productVariantQuery
                                                                                .price()
                                                                                .title()
                                                                                .image(args -> args.src())
                                                                                .weight()
                                                                                .weightUnit()
                                                                                .available()
                                                                        )
                                                                )
                                                        )
                                                )
                                        )


                                ))));

        graphClient.queryGraph(query).enqueue(new GraphCall.Callback<Storefront.QueryRoot>() {
            @Override
            public void onResponse(@NonNull GraphResponse<Storefront.QueryRoot> response) {
                Storefront.Collection product = (Storefront.Collection) response.data().getNode();

                for (Storefront.ProductEdge productEdge : product.getProducts().getEdges()) {
                    GroceryHomeModel GroceryHomeModel = new GroceryHomeModel();
                    GroceryHomeModel.setProduct(productEdge.getNode());
                    GroceryHomeModel.setTitle(product.getTitle());
                    GroceryHomeModel.setQty("1");
                    GroceryHomeModelArrayList.add(GroceryHomeModel);
                }
                foryouInterface.grocerylist(GroceryHomeModelArrayList);
            }

            @Override
            public void onFailure(@NonNull GraphError error) {

            }
        });
    }

}