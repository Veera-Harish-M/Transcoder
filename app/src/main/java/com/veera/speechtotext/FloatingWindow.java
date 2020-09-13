package com.veera.speechtotext;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class FloatingWindow extends Service implements RecognitionListener {

    TextToSpeech tts;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    String res_reco;
    Spinner spinner;
    String[] from={"English"};
    String[] languages={"Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Danish", "German", "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Galician", "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian", "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Georgian", "Kannada", "Korean", "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay", "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Tamil", "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Chinese"};
    String lang="EN",trans_text;
    String[] l_code={"AF", "AR", "BE", "BG", "BN", "CA", "CS", "CY", "DA", "DE", "EL", "EN", "EO", "ES", "ET", "FA", "FI", "FR", "GA", "GL", "GU", "HE", "HI", "HR", "HT", "HU", "ID", "IS", "IT", "JA", "KA", "KN", "KO", "LT", "LV", "MK", "MR", "MS", "MT", "NL", "NO", "PL", "PT", "RO", "RU", "SK", "SL", "SQ", "SV", "SW", "TA", "TE", "TH", "TL", "TR", "UK", "UR", "VI", "ZH"};
    FirebaseTranslator englishGermanTranslator;
    ArrayAdapter<String> adapter;
    TextView dialog_text;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();


        String languagePref = "ta-IN";
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
       // recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"ta");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePref);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        ConvertTextToSpeech();
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }

        });



        final WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.x=0;
        params.y=100;
        params.gravity = Gravity.TOP;





        LayoutInflater inflaters = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mDialogView = inflaters.inflate(R.layout.dialogs, null);

        progressBar = (ProgressBar) mDialogView.findViewById(R.id.progressBar1);

        progressBar.setVisibility(View.INVISIBLE);


        wm.addView(mDialogView, params);

        mDialogView.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = params;
            double x;
            double y;
            double pressedX;
            double pressedY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - pressedY));

                        wm.updateViewLayout(mDialogView, updatedParameters);

                    default:
                        break;
                }

                return false;
            }
        });

        spinner=mDialogView.findViewById(R.id.to_spin);
        adapter = new ArrayAdapter <String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, languages);
        spinner.setAdapter(adapter);



        ArrayAdapter <String> adapter_from_language = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, from);
        Spinner spinner1=mDialogView.findViewById(R.id.from_spin);
        spinner1.setAdapter(adapter_from_language);

        dialog_text =mDialogView.findViewById(R.id.dialog_txt);

        ImageView hearing=mDialogView.findViewById(R.id.hear);
        hearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                speech.startListening(recognizerIntent);
            }

        });


        ImageView tras= mDialogView.findViewById(R.id.transss);
        tras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!res_reco.isEmpty()) {

                    translate_tamil();

                }else{
                    Toast.makeText(getApplicationContext(),"First read a word",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView speaker=mDialogView.findViewById(R.id.speak);
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConvertTextToSpeech();
            }
        });

        ImageView cancel=mDialogView.findViewById(R.id.btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wm.removeViewImmediate(mDialogView);
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        String text;
        try {
            text =trans_text;
        }catch (Exception e){
            text="invalid";
        }
        if(text==null||"".equals(text))
        {
            try {
                text = trans_text;
            }catch (Exception e){
                text="invalid";
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    public void translate_tamil() {
// Create an English-German translator:

        int i=adapter.getPosition(spinner.getSelectedItem().toString());
        Log.i("aaa",l_code[i]);
        lang=l_code[i];

        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.TA)
                .setTargetLanguage(FirebaseTranslateLanguage.languageForLanguageCode(lang))
                .build();
        Log.i("aaa","hwa");
        englishGermanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        Log.i("aaaaaa","hwascsc");

        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
     Log.i("aaaaaaaaaaaaa","here");
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
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

    }

    public void tras() {

        Log.i("aaaaaaaaaa","kslbkj");

        englishGermanTranslator.translate(res_reco)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful
                                // .
                                Log.i("aaaaaaaaaaaaaaaaaaaaaa","hgfdfghgf");
                                trans_text=translatedText;
                                Toast.makeText(getApplicationContext(), translatedText, Toast.LENGTH_SHORT).show();
                                String tex = "<font color=\\'#FF8000\\><br>TRANS: </font>";
                                dialog_text.setText(dialog_text.getText().toString()+"\n"+HtmlCompat.fromHtml(tex, HtmlCompat.FROM_HTML_MODE_LEGACY)+trans_text);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error in Translation=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // ...
                            }
                        });
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        res_reco=errorMessage;
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text="";
        for (String result : matches) {
            text += result;
            break;
        }
        res_reco=text;
        Toast.makeText(FloatingWindow.this,text,Toast.LENGTH_SHORT).show();
        //list_msg.add(text);

        String tex = "<font color=#9400D3>YOU: </font>";
        dialog_text.setText(dialog_text.getText().toString()+"\n"+HtmlCompat.fromHtml(tex,HtmlCompat.FROM_HTML_MODE_LEGACY)+text);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        Log.i("aaaaaa",res_reco);

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }



}