package de.weiss.android.text;

import java.util.Locale;

import de.weiss.android.text.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class KennyTranslator extends Activity implements OnInitListener {
	private EditText text;
	private int MY_DATA_CHECK_CODE = 0;
	
	private TextToSpeech tts;
	
	//private Button speakButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        text = (EditText) findViewById(R.id.editText1);
        //speakButton = (Button) findViewById(R.id.speak_button);
        
            Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }
    
    public void setOnClickListener(View v) 
    {			
			String say = text.getText().toString();
			if (say!=null && text.length()>0) 
			{
				Toast.makeText(KennyTranslator.this, "Saying: " + say, Toast.LENGTH_LONG).show();
//				if (tts.isLanguageAvailable(Locale.GERMAN) == tts.LANG_AVAILABLE) {						
//					tts.setLanguage(Locale.GERMAN);
//				}
				tts.speak(say, TextToSpeech.QUEUE_ADD, null);
			}
		
    }	

    
    public void onDestroy ()
    {
    	super.onDestroy();
    	tts.shutdown();
    	System.runFinalizersOnExit(true);
    	System.exit(0);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
			} 
			else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}

	}

	@Override
	public void onInit(int status) {		
		if (status == TextToSpeech.SUCCESS) {
			Toast.makeText(KennyTranslator.this, 
					"Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
		}
		else if (status == TextToSpeech.ERROR) {
			Toast.makeText(KennyTranslator.this, 
					"Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
		}
	}
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    	}
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings_title:
            	    finish();
                    return true;
            
            case R.id.exit_title:
                    finish();
                    return true;
        }
      return false;
    }
    
 // This method is called at button click because we assigned the name to the
	// "On Click property" of the button
	public void translateClickHandler(View view) {
			if (text.getText().length() == 0) {
				Toast.makeText(this, "Please enter a valid text",
						Toast.LENGTH_LONG).show();
				return;
			}
		text.setText(translate(text.getText().toString()));
		}
	
	/**
	* The KENNYLETTERS in alphabetical order. Big Letters are the Same with the
	* only difference That the First char is UpperCase
	*/
	public static final String[] KENNYLETTERS = { "mmm", "mmp", "mmf", "mpm",
			"mpp", "mpf", "mfm", "mfp", "mff", "pmm", "pmp", "pmf", "ppm",
			"ppp", "ppf", "pfm", "pfp", "pff", "fmm", "fmp", "fmf", "fpm",
			"fpp", "fpf", "ffm", "ffp" };

	private char translateKennyLetterToNormalLetter(String s) throws Exception {
		char[] input = s.toCharArray();
		boolean bigChar = (input[0] == Character.toUpperCase(input[0]));
		input[0] = Character.toLowerCase(input[0]);
		String tmp = new String(input);
		for (int i = 0; i < KENNYLETTERS.length; i++) {
			if (tmp.equals(KENNYLETTERS[i])) {
				return bigChar ? (char) (i + 'A') : ((char) (i + 'a'));
			}
		}
		throw new Exception(s + " is no Kenny Word!!!");
	}

	private String translateKennyToNormal(String s) {
		String result = "";
		stringiterate: for (int i = 0; i < s.length(); i = i + 3) {
			while (Character.toUpperCase(s.charAt(i)) != 'M'
					&& Character.toUpperCase(s.charAt(i)) != 'P'
					&& Character.toUpperCase(s.charAt(i)) != 'F') {
				result = result + s.charAt(i);
				i++;
				if (i == s.length()) {
					break stringiterate;
				}
			}
			try {
				result = result
						+ translateKennyLetterToNormalLetter(s.substring(i,
								i + 3));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private String translateNormalLetterToKennyLetter(char c) {
		if (c == Character.toUpperCase(c)) {
			char[] result = KENNYLETTERS[c - 'A'].toCharArray();
			result[0] = Character.toUpperCase(result[0]);
			return new String(result);
		} else {
			return KENNYLETTERS[c - 'a'];
		}
	}

	private String translateNormalToKenny(String s) {
		String result = "";
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 'A' || (c > 'Z' && c < 'a') || c > 'z')
				result = result + c;
			// Ordered according to optimisations suggested by Stuart
			else {
				result = result + translateNormalLetterToKennyLetter(c);
			}
		}
		return result;
	}

	private boolean isKenny(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >= 'a' && c <= 'z' && c != 'm' && c != 'f' && c != 'p')
					|| (c >= 'A' && c <= 'Z' && c != 'M' && c != 'F' && c != 'P')) {
				return false;
			}
		}
		return true;
	}

	/**
	* @param s
	*            String to Translate
	* @return translated String
	*/
	public String translate(String s) {
		if (isKenny(s)) {
			return translateKennyToNormal(s);
		} else {
			return translateNormalToKenny(s);
		}
	}
	
    
}