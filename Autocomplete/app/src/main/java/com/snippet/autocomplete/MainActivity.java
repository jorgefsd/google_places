package com.snippet.autocomplete;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.snippet.com.snippet.debug.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GooglePlacesSDMX";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyBIxry3xLx15lwwlB7795qtsqJrEbBV1nU";

    MyCustomAdapter dataAdapter = null;
    EditText myFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ArrayList<String> resultList = new ArrayList<>();

        dataAdapter = new MyCustomAdapter(this,
                R.layout.country_info,resultList);
        ListView listView = (ListView) findViewById(R.id.resultsListView);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                String country = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        country, Toast.LENGTH_SHORT).show();
            }
        });

        myFilter = (EditText) findViewById(R.id.searchEditText);

        myFilter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dataAdapter.getFilter().filter(s);
            }
        });
    }

    class MyCustomAdapter extends ArrayAdapter<String> {

        private ArrayList<String> originalList;
        private ArrayList<String> countryList;
        private PlacesFilter filter;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<String> countryList) {

            super(context, textViewResourceId, countryList);

            this.countryList = new ArrayList<>();
            this.countryList.addAll(countryList);

            this.originalList = new ArrayList<>();
            this.originalList.addAll(countryList);

        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new PlacesFilter();
            }
            return filter;
        }

        private class ViewHolder {
            TextView code;
            TextView name;
            TextView continent;
            TextView region;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {

                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

                convertView = vi.inflate(R.layout.country_info, null);

                holder = new ViewHolder();

                holder.code = (TextView) convertView.findViewById(R.id.code);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String country = countryList.get(position);

            Debug.DEBUG(LOG_TAG, country);

            holder.code.setText(country);

            return convertView;
        }

        private class PlacesFilter extends Filter {

            private ArrayList<String> resultList;

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            public ArrayList<String> autocomplete(String input) {
                ArrayList<String> resultList = null;

                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                try {
                    StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                    sb.append("?key=" + API_KEY);
                    sb.append("&components=country:mx");
                    sb.append("&language=es");
                    sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                    URL url = new URL(sb.toString());

                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    // Load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, "Error processing Places API URL", e);
                    return resultList;
                } catch (IOException e) {

                    return resultList;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                try {
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                    resultList = new ArrayList<String>(predsJsonArray.length());
                    for (int i = 0; i < predsJsonArray.length(); i++)
                        resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                } catch (JSONException e) {

                }
                return resultList;
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    countryList = (ArrayList<String>)results.values;
                    notifyDataSetChanged();
                    clear();
                    for(int i = 0, l = countryList.size(); i < l; i++)
                    add(countryList.get(i));
                    notifyDataSetInvalidated();

                } else {
                    notifyDataSetInvalidated();
                }
            }
        }


    }


}
