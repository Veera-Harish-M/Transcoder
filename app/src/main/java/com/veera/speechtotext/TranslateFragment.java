package com.veera.speechtotext;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TranslateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TranslateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button btn,spk,tra;
    TextView txt,txt2;
    TextToSpeech tts;
    int result;
    String[] languages={"Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Danish", "German", "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Galician", "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian", "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Georgian", "Kannada", "Korean", "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay", "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Tamil", "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Chinese"};
    String[] l_code={"AF", "AR", "BE", "BG", "BN", "CA", "CS", "CY", "DA", "DE", "EL", "EN", "EO", "ES", "ET", "FA", "FI", "FR", "GA", "GL", "GU", "HE", "HI", "HR", "HT", "HU", "ID", "IS", "IT", "JA", "KA", "KN", "KO", "LT", "LV", "MK", "MR", "MS", "MT", "NL", "NO", "PL", "PT", "RO", "RU", "SK", "SL", "SQ", "SV", "SW", "TA", "TE", "TH", "TL", "TR", "UK", "UR", "VI", "ZH"};
    FirebaseTranslator englishGermanTranslator;
    String lang="EN";
    Spinner spinner;
    ArrayAdapter<String> adapter;
    View view;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TranslateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TranslateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TranslateFragment newInstance(String param1, String param2) {
        TranslateFragment fragment = new TranslateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setHasOptionsMenu(true);

        getActivity().startService(new Intent(getActivity(),FloatingWindow.class));

        view= inflater.inflate(R.layout.fragment_translate, container, false);



        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_PHONE_STATE},1);
        }

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

        txt = view.findViewById(R.id.mtextView);
        txt2=view.findViewById(R.id.mtextView2);
        btn = view.findViewById(R.id.btn);
        tra=view.findViewById(R.id.trans);
        spk = view.findViewById(R.id.speak);
        spinner=view.findViewById(R.id.spinner);

        adapter = new ArrayAdapter <String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, languages);
        spinner.setAdapter(adapter);



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSpeechInput();
            }
        });

        tra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translate_tamil();
            }
        });

        spk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConvertTextToSpeech();
            }
        });


        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        txt.setText("Welcome to Transcoder");
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });

        return view;
    }

    public String get_received(){
        return txt.getText().toString();
    }
    public String get_translated(){
        return txt2.getText().toString();
    }

    public void makesnackbar(String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.BLACK)
                .setAction("Action", null).show();
    }

    public void translate_tamil() {
// Create an English-German translator:

        int i=adapter.getPosition(spinner.getSelectedItem().toString());
        Log.i("aaa",l_code[i]);
        lang=l_code[i];
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.languageForLanguageCode(lang))
                .build();
        englishGermanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();



        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                tras();
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

    }

    public void tras() {

        englishGermanTranslator.translate(txt.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                txt2.setText(translatedText);
                                Toast.makeText(getActivity(), translatedText, Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                Toast.makeText(getActivity(), "Error in Translation=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // ...
                            }
                        });
    }


    public void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(getActivity(), "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txt.setText(result.get(0));
                    Log.i("hhh",result.get(0));
                }
                break;
        }
    }


    public void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        String text = "hello";
        Log.i("hhhhhhhhh",text);
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        }
    }











    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_all,menu);
    }
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
