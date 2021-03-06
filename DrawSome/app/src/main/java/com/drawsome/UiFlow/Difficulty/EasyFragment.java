package com.drawsome.UiFlow.Difficulty;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.drawsome.R;
import com.drawsome.database.WordBean;
import com.drawsome.database.WordsDBHelper;

import java.util.List;


/**
 * Individual fragment for easy difficulty tab.
 * Author: Syed Omer Salar Khureshi
 */
public class EasyFragment extends Fragment {

    ListView listView;
    String[] easyWords = {"phone", "laptop", "flower", "house", "hill", "banana"};

    OnWordSelectListener onWordSelectListener;

    public EasyFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.easy_fragment, container, false);
       System.out.println(getContext());
       WordsDBHelper wordsDBHelper = new WordsDBHelper(getContext());
        List<WordBean> listWords = wordsDBHelper.getEasyWords();
        easyWords = new String[listWords.size()];
        int index =0;
        for (WordBean bean: listWords) {
            easyWords[index] = bean.getWord();
            index++;
        }

        listView = (ListView) rootView.findViewById(R.id.easy_fragment_list_view);;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_text_view, easyWords);
        listView.setAdapter(arrayAdapter);

//        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                for (int i = 0; i < listView.getCount(); i++) {
//                    TextView item = (TextView) listView.getChildAt(i);
//                    item.setBackgroundColor(Color.parseColor("#ef5350"));
//                }
//                view.setBackgroundColor(Color.parseColor("#BF4240"));
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//
//        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < listView.getCount(); i++) {
                    TextView item = (TextView) listView.getChildAt(i);
                    item.setBackgroundColor(Color.parseColor("#ef5350"));
                }
                view.setBackgroundColor(Color.parseColor("#BF4240"));
                onWordSelectListener.onWordSelect(((TextView)listView.getChildAt(position)).getText().toString(),1);
            }
        });
        return rootView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onWordSelectListener = (OnWordSelectListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnWordSelectListener interface");
        }
    }
}
