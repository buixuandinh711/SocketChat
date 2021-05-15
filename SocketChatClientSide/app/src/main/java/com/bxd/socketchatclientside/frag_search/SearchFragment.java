package com.bxd.socketchatclientside.frag_search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.SocketManager;
import com.bxd.socketchatclientside.message_activity.MessageActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchFragment extends Fragment {

    ListView listViewSearch;
    ArrayList<String> arrayListClients;
    ArrayAdapter<String> adapter;
    SocketManager socketManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);
        initialize(view);
        clickSearchItem();
        Log.d("ccc", "create search fragment");
        return view;
    }

    private void initialize(View view) {

        listViewSearch = view.findViewById(R.id.listViewSearch);
        arrayListClients = new ArrayList<>();
        socketManager = SocketManager.getInstance();
        adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, arrayListClients);
        listViewSearch.setAdapter(adapter);

    }

    /**
     * Start an {@link MessageActivity} to chat with choose client.
     */
    private void clickSearchItem() {

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra(ClientConst.CLIENT_USERID_INTENT_DATA, arrayListClients.get(position));
                startActivity(intent);
            }
        });

    }

    /**
     * Send a search resquest to server to get the list of server mathches with {@param query}
     * The response of the server contained in listSearch of SocketManager.
     * @param query
     */
    public void searchClient(String query) {

        String requestCode = ClientConst.CODE_SEARCH + SocketManager.getRequestNumber();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> {
            socketManager.sendRequest(requestCode, query);
            int count = 0;
            while (count < 10) {

                if (socketManager.getListSearch().containsKey(requestCode)) {
                    return socketManager.getListSearch().get(requestCode);
                }

                count++;
                Thread.sleep(100);

            }
            return "";
        });

        try {
            String response = future.get();
            String[] clients = response.split("#");
            arrayListClients.clear();
            if (response.contains("#")) {
                arrayListClients.addAll(Arrays.asList(clients));
            }
            adapter.notifyDataSetChanged();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
