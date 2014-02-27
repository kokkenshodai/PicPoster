package ca.ualberta.cs.picposter;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class BitmapSerializer implements JsonSerializer<Bitmap>, JsonDeserializer<Bitmap>
{
	//serialization method taken from http://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string
	@Override
	public JsonElement serialize(Bitmap arg0, Type arg1,
			JsonSerializationContext arg2)
	{
		Log.w("Serialization", "Invoked");
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
		arg0.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] byteArray = byteArrayOutputStream .toByteArray();

		String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
		
		final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("image", encoded);
        
		return  jsonObject;
	}

	//deserialization method taken from http://stackoverflow.com/questions/5243547/decode-byte-array-to-bitmap-that-has-been-compressed-in-java
	@Override
	public Bitmap deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException
	{
		String encodedString = arg0.getAsJsonObject().get("image").toString();
		Log.w("Deserialization",encodedString);
		byte[] bytSig = Base64.decode(encodedString,Base64.NO_WRAP);
		
		return BitmapFactory.decodeByteArray(bytSig, 0, bytSig.length);
	}

}
