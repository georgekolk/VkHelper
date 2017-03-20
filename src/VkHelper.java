import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class VkHelper {

    private String requestURL = null;
	private URIBuilder builder = new URIBuilder();

	private String charset = "UTF-8";
	private long overallPhotoPostCount;
	//private List downloadPicturesList;
	private ArrayList<String> list = new ArrayList<String>();


    public String getWallUploadServer(String ACCESS_TOKEN, String GROUP_ID)throws URISyntaxException, IOException, ParseException {

		builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.getWallUploadServer")
                .setParameter("group_id", GROUP_ID)
                .setParameter("access_token", ACCESS_TOKEN)
                .setParameter("v", "5.52");
		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = null;
			try {
				instream = entity.getContent();
				String responseAsString = IOUtils.toString(instream);

				////--------------------------debug--------------------
				//System.out.println("---------------------------------------------upload server raw answer------------------------------------");
				//System.out.println(responseAsString);
				////--------------------------debug--------------------

				JSONParser pars = new JSONParser();
				Object obj = null;
				try {
					obj = pars.parse(responseAsString);
					} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
//--------------------------------------Parse-response-getWallUploadServer--------------------------------------------------------------------------------------

				JSONObject jsonObj = (JSONObject) obj;
				jsonObj = (JSONObject) jsonObj.get("response");

				System.out.println("Upload server: "+jsonObj.get("upload_url").toString());


				requestURL = jsonObj.get("upload_url").toString();

                } finally {
                    if (instream != null)
                        instream.close();
                }
            }
        return requestURL;
//---
        }

	public String saveWallPhoto(String ACCESS_TOKEN, String GROUP_ID, String server, String hash, String photo)throws URISyntaxException, IOException, ParseException {
        String multipleAttachments = "";
        JSONObject jsonObj1;

		builder.clearParameters();

		builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.saveWallPhoto")
				.setParameter("group_id", GROUP_ID)
				.setParameter("server", server)
				.setParameter("hash", hash)
				.setParameter("photo", photo)
				.setParameter("access_token", ACCESS_TOKEN)
				.setParameter("v", "5.52");
		URI uri = builder.build();

		HttpGet httpget = new HttpGet(uri);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();


		if (entity != null) {
			InputStream instream = null;
			try {
				instream = entity.getContent();
				String responseAsString = IOUtils.toString(instream);
				System.out.println(responseAsString);

				JSONParser pars = new JSONParser();
				Object obj = null;

				obj = pars.parse(responseAsString);

				JSONObject jsonObj = (JSONObject) obj;
				System.out.println(jsonObj.get("response"));
				obj = jsonObj.get("response");
				JSONArray responseAfterUploading = (JSONArray) obj;
				//JSONObject jsonObj1 = (JSONObject) responseAfterUploading.get(0);

                for(int i = 0; i < responseAfterUploading.size(); i++){
                    jsonObj1 = (JSONObject) responseAfterUploading.get(i);
                    System.out.println("PHOTO_ID: "+ jsonObj1.get("id").toString());
                    multipleAttachments = multipleAttachments + "photo" + jsonObj1.get("owner_id").toString() + "_" + jsonObj1.get("id").toString()+",";
                }

			} finally {
				if (instream != null)
					instream.close();
			}
		}

		return multipleAttachments;
	}

	public Boolean postWallPhoto(String ACCESS_TOKEN, String GROUP_ID, String multipleAttachments, String message)throws URISyntaxException, IOException{
		boolean savePhotoResult = false;
        builder.clearParameters();
        builder.setScheme("https").setHost("api.vk.com").setPath("/method/wall.post")
                .setParameter("owner_id", "-" + GROUP_ID)
                .setParameter("message", message)
                //.setParameter("attachments", "photo" + jsonObj1.get("owner_id").toString() + "_" + jsonObj1.get("id").toString())
                .setParameter("attachments", multipleAttachments)
                .setParameter("access_token", ACCESS_TOKEN)
                .setParameter("v", "5.52");
        //System.out.println(builder.toString());
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream2 = null;
            try {
                instream2 = entity.getContent();
                String responseAfterWallPost = IOUtils.toString(instream2);
				savePhotoResult = responseAfterWallPost.contains("post_id");
                System.out.println(responseAfterWallPost);
            } finally {
                if (instream2 != null)
                    instream2.close();
            }
        }

        return savePhotoResult;
	}

    public List<String> uploadPhoto(String uploadPage, String charset, File fileToProcess)throws URISyntaxException, IOException, ParseException {

        List<String> serverHashPhoto = new ArrayList<String>();

        MultipartUtility multipart = new MultipartUtility(uploadPage, charset);

        multipart.addHeaderField("User-Agent", "jHateSMM");
        multipart.addFormField("description", "Cool Pictures");

					/*int z = 1;
					for (File fileToProcess : uploadFile) {
						multipart.addFilePart("file" + Integer.toString(z), fileToProcess);
						z++;
					}*/

        multipart.addFilePart("file", fileToProcess);
        List<String> response2 = multipart.finish();

					/*for (String line : response2) {
						System.out.println(line);
					}*/

        JSONParser pars = new JSONParser();
        Object obj = null;
        try {
            obj = pars.parse(response2.toString());

            System.out.println("Response after upload: "+response2.toString());

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONArray uploadedPhotoArray = (JSONArray) obj;

        for (int i = 0; i < uploadedPhotoArray.size(); i++) {
            JSONObject uploadedPhoto = (JSONObject) uploadedPhotoArray.get(i);

            System.out.println("Server: " + uploadedPhoto.get("server"));
            System.out.println("Hash: " + uploadedPhoto.get("hash"));
            System.out.println("Photo: " + uploadedPhoto.get("photo"));

            serverHashPhoto.add(uploadedPhoto.get("server").toString());
            serverHashPhoto.add(uploadedPhoto.get("hash").toString());
            serverHashPhoto.add(uploadedPhoto.get("photo").toString());

        }

            return serverHashPhoto;
    }

	public List<String> photosGetAlbums(String ACCESS_TOKEN,String OWNER_ID)throws URISyntaxException, IOException, ParseException {
		List<String> albums = new ArrayList<>();

		builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.getAlbums")
				.setParameter("owner_id", OWNER_ID)
                //.setParameter("need_system", "1")
				.setParameter("access_token", ACCESS_TOKEN)
				.setParameter("v", "5.60");
		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = null;
			try {
				instream = entity.getContent();
				String responseAsString = IOUtils.toString(instream);



				////--------------------------debug--------------------
				//System.out.println("---------------------------------------------upload server raw answer------------------------------------");
				//System.out.println("raw response string: "+responseAsString);
				////--------------------------debug--------------------

				JSONParser pars = new JSONParser();
//				Object obj = null;
				try {
//					obj = pars.parse(responseAsString);
					JSONObject obj = (JSONObject) pars.parse(responseAsString);
					//System.out.println("obj to string looks like: "+obj.toString());
					JSONObject jsonResponse = (JSONObject) obj.get("response");
					//System.out.println("response: "+jsonResponse.toString());
					JSONArray items = (JSONArray) jsonResponse.get("items");
					//System.out.println("items array: "+items.toString());
					//JSONArray items = (JSONArray) jsonResponse.get("items");
//					JSONObject result = (JSONObject) jsonParser.parse(reader);

					for (int i = 0; i < items.size(); i++) {

					JSONObject id = (JSONObject) items.get(i);

						//System.out.println("id: " + id.get("id").toString());

						albums.add(id.get("id").toString());

					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} finally {
				if (instream != null)
					instream.close();
			}
		}
		return albums;

	}

    public List<String> photosGet (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID,long OFFSET, List<String> photos)throws URISyntaxException, IOException, ParseException {

        builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.get")
                .setParameter("owner_id", OWNER_ID)
                .setParameter("album_id", ALBUM_ID)
				.setParameter("offset", "" + OFFSET)
                .setParameter("access_token", ACCESS_TOKEN)
                .setParameter("v", "5.60");

        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
                String responseAsString = IOUtils.toString(instream);

                ////--------------------------debug--------------------
                //System.out.println("---------------------------------------------upload server raw answer------------------------------------");
                System.out.println("raw response string: "+responseAsString);
                ////--------------------------debug--------------------

                JSONParser pars = new JSONParser();
//				Object obj = null;
                try {
//					obj = pars.parse(responseAsString);
                    JSONObject obj = (JSONObject) pars.parse(responseAsString);
                    //System.out.println("obj to string looks like: "+obj.toString());
                    JSONObject jsonResponse = (JSONObject) obj.get("response");
                    System.out.println("response: "+jsonResponse.toString());

                    JSONArray items = (JSONArray) jsonResponse.get("items");

                    System.out.println("items array: " + items.toString());

                    for (int i = 0; i < items.size(); i++) {

                        JSONObject id = (JSONObject) items.get(i);

                    	if (id.containsKey("photo_1280")){
                        	photos.add(id.get("photo_1280").toString());
                    	}else if (id.containsKey("photo_807")){
	                        photos.add(id.get("photo_807").toString());
	                    }else if (id.containsKey("photo_604")){
	                        photos.add(id.get("photo_604").toString());
	                    }else if (id.containsKey("photo_130")){
	                        photos.add(id.get("photo_130").toString());
	                    }else if (id.containsKey("photo_75")){
	                        photos.add(id.get("photo_75").toString());
	                    }

                    }

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    //e.getMessage();
                }

            } finally {
                if (instream != null)
                    instream.close();
            }
        }
        System.out.println("photos array size: " + photos.size());
        return photos;

    }

    public List<String> photosGet (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID)throws URISyntaxException, IOException, ParseException
    {
        //long OFFSET_URI = 0;
        long OFFSET = 1000;
        List<String> photos = new ArrayList<>();

        VkHelper vkHater = new VkHelper();

        long count = vkHater.photosGetCount(ACCESS_TOKEN,OWNER_ID,ALBUM_ID);

        for(long i = 0; i <= count; i = i + OFFSET){

            photos = vkHater.photosGet(ACCESS_TOKEN,OWNER_ID,ALBUM_ID,OFFSET,photos);
        }

        return photos;
    }

    public long photosGetCount (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID)throws URISyntaxException, IOException, ParseException {

        long itemsCount = 0;

        builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.get")
                .setParameter("owner_id", OWNER_ID)
                .setParameter("album_id", ALBUM_ID)
                .setParameter("access_token", ACCESS_TOKEN)
                .setParameter("v", "5.60");

        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
                String responseAsString = IOUtils.toString(instream);

                JSONParser pars = new JSONParser();

                try {
                    JSONObject obj = (JSONObject) pars.parse(responseAsString);
                    JSONObject jsonResponse = (JSONObject) obj.get("response");

                    itemsCount = (long) jsonResponse.get("count");

                    System.out.println("items count: "+itemsCount);

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    //e.getMessage();
                }

            } finally {
                if (instream != null)
                    instream.close();
            }
        }

        return itemsCount;
    }

	public long getOverallPhotoPosts(String OWNER_ID, String ACCESS_TOKEN)throws IOException{
		URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&access_token=" + ACCESS_TOKEN + "&count=1&v=5.62");

		try {
			HttpURLConnection connection = (HttpURLConnection) getPhotos.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();
			connection.disconnect();

			//System.out.println(content.toString());

			JSONParser pars = new JSONParser();

			try {

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("obj to string looks like: " + obj.toString());
				JSONObject jsonResponse = (JSONObject) obj.get("response");
				//System.out.println("response: " + jsonResponse.toString());
				this.overallPhotoPostCount = (long) jsonResponse.get("count");

				//this.overallPhotoPostCount = (long) blogInfo.get("posts");
				System.out.println("photo posts count: " + overallPhotoPostCount);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}catch (Exception E){
			E.printStackTrace();
		}

		return overallPhotoPostCount;
	}

	public ArrayList getPhotosListOffset(String OWNER_ID, String ACCESS_TOKEN, long offset)throws IOException{
		URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&access_token=" + ACCESS_TOKEN + "&count=1&offset=" + offset + "&v=5.62");

		try {
			HttpURLConnection connection = (HttpURLConnection) getPhotos.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();

			connection.disconnect();

			//System.out.println(content.toString());

			JSONParser pars = new JSONParser();

			try {

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("obj to string looks like: " + obj.toString());
				JSONObject jsonResponse = (JSONObject) obj.get("response");
				JSONArray jsonItems = (JSONArray) jsonResponse.get("items");
				//TODO: вот тут должен быть обработчки если нету вообще ключа Items
				//System.out.println("jsonItems: " + jsonItems);
				for (int i = 0; i < jsonItems.size(); i++) {

					JSONObject jsonItemStep = (JSONObject) jsonItems.get(i);
					//System.out.println("jsonItemStep: " + jsonItemStep);

					JSONArray jsonAttachments = (JSONArray) jsonItemStep.get("attachments");
					System.out.println("jsonAttachments: " + jsonAttachments);

					if (jsonAttachments != null) {
						for (int z = 0; z < jsonAttachments.size(); z++) {

							JSONObject jsonAttachmentsStep = (JSONObject) jsonAttachments.get(z);
							//System.out.println("jsonAttachmentsStep: " + jsonAttachmentsStep);

								if (jsonAttachmentsStep.get("photo") != null) {

									JSONObject jsonAttachmentsStepGetPhoto = (JSONObject) jsonAttachmentsStep.get("photo");
									//System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);

									if (jsonAttachmentsStepGetPhoto.containsKey("photo_1280")) {
										//System.out.println("1280: " + jsonAttachmentsStepGetPhoto.get("photo_1280"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_1280").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_807")) {
										//System.out.println("807: " + jsonAttachmentsStepGetPhoto.get("photo_807"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_807").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_604")) {
										//System.out.println("604: " + jsonAttachmentsStepGetPhoto.get("photo_604"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_604").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_130")) {
										System.out.println("130: " + jsonAttachmentsStepGetPhoto.get("photo_130"));
										//list.add(jsonAttachmentsStepGetPhoto.get("photo_130").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_75")) {
										//System.out.println("75: " + jsonAttachmentsStepGetPhoto.get("photo_75"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_75").toString());
									}
								}else{

									return null;
								}

							/*JSONObject jsonAttachmentsStepGetPhoto = (JSONObject) jsonAttachmentsStep.get("photo");
							System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);

							JSONObject jsonAttachmentsStepGetDoc = (JSONObject) jsonAttachmentsStep.get("doc");
							System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);*/

						}
					}else{
						return null;
					}

				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}catch (Exception E){
			E.printStackTrace();
		}

		return list;
	}


}






