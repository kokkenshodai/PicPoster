package ca.ualberta.cs.picposter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ca.ualberta.cs.picposter.model.PicPostModel;
import ca.ualberta.cs.picposter.model.PicPosterModelList;


public class ElasticSearchOperations
{
	public static void pushPicPostModel(final PicPostModel model){
		Thread thread = new Thread(){
			
			@Override
			public void run(){
				Gson gson = new Gson();
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost("http://cmput301.softwareprocess.es:8080/testing/mjnichols/");
				
				try
				{

					String jsonString = gson.toJson(model);
					request.setEntity(new StringEntity(jsonString));
					
					HttpResponse response = client.execute(request);
					Log.w("ElasticSearch", response.getStatusLine().toString());
					
					response.getStatusLine().toString();
					HttpEntity entity = response.getEntity();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
					String output = reader.readLine();
					while(output != null)
					{
						Log.w("ElasticSearch", output);
						output = reader.readLine();
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		};
		
		thread.start();
	}
	
	//Taken from https://github.com/rayzhangcl/ESDemo
	
	public static void pullPicPostModel(final String text, final PicPosterModelList model, final Activity activity){
		
		if(text == null || text.equals("")) return;
		
		Thread thread = new Thread(){
			
			@Override
			public void run(){			
				// Http Connector
				HttpClient httpclient = new DefaultHttpClient();
				// JSON Utilities
				final Gson gson = new Gson();

				try
				{
					HttpPost searchRequest = new HttpPost("http://cmput301.softwareprocess.es:8080/testing/mjnichols/_search");
					String query = "{\"query\" : {\"query_string\" : {\"default_field\" : \"text\",\"query\" : \"*" + text + "*\"}}}";
					StringEntity stringentity = new StringEntity(query);
	
					searchRequest.setHeader("Accept","application/json");
					searchRequest.setEntity(stringentity);
	
					final HttpResponse response = httpclient.execute(searchRequest);
					String status = response.getStatusLine().toString();
					Log.w("ElasticSearch",status);
	
					activity.runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							model.clear();
							
							String json = "";
							try
							{
								json = getEntityContent(response);
							} catch (IOException e)
							{
								e.printStackTrace();
							}
							
							Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<PicPostModel>>(){}.getType();
							ElasticSearchSearchResponse<PicPostModel> esResponse = gson.fromJson(json, elasticSearchSearchResponseType);
							Log.w("ElasticSearch",gson.toJson(esResponse));
							for (ElasticSearchResponse<PicPostModel> r : esResponse.getHits()) {
								PicPostModel recipe = r.getSource();
								model.readdPicPost(recipe);
								Log.w("ElasticSearch",gson.toJson(recipe));
							}
						}
					});

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			//Taken from https://github.com/rayzhangcl/ESDemo
			
			String getEntityContent(HttpResponse response) throws IOException {
				BufferedReader br = new BufferedReader(
				new InputStreamReader((response.getEntity().getContent())));
				String output;
				Log.w("ElasticSearch","Output from Server -> ");
				String json = "";
				while ((output = br.readLine()) != null) {
					Log.w("ElasticSearch",output);
					json += output;
				}
				Log.w("ElasticSearch","JSON:"+json);
				
				return json;
			}
			
		};

		thread.start();
	}
	
}
