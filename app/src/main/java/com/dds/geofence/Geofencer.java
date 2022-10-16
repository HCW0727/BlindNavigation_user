package com.dds.geofence;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Geofencer
{
	private final static int MSG_BASE_DATA_RECEIVE = 0x00;
	private final static int MSG_POLYGON_DATA_RECEIVE = 0x01;

	public static String url = "https://api2.sktelecom.com";
	public static String baseUrl = url + "/tmap/";
	public static String api_key;

	private OnGeofencingBaseDataReceivedCallback baseDataListener = null;
	private OnGeofencingPolygonCreatedCallback polygonListener = null;

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_BASE_DATA_RECEIVE:
					if (baseDataListener != null)
						baseDataListener.onReceived((ArrayList<GeofenceData>) msg.obj);
					break;

				case MSG_POLYGON_DATA_RECEIVE:
					if (polygonListener != null)
						polygonListener.onReceived((ArrayList<TMapPolygon>) msg.obj);
					break;

				default:
					break;
			}
		}
	};

	public Geofencer(String apiKey)
	{
		api_key = apiKey;
	}

	public ArrayList<GeofenceData> toGeofenceData(String str)
	{
		ArrayList<GeofenceData> geofenceDatas = new ArrayList<GeofenceData>();

		if (str != null && !str.trim().equals(""))
		{
			try
			{
				JSONArray jArray = new JSONObject(str).getJSONArray("searchRegionsInfo");

				for (int i = 0; i < jArray.length(); i++)
				{
					JSONObject regionInfo = new JSONObject(jArray.getJSONObject(i).getString("regionInfo"));
					JSONObject properties = new JSONObject(regionInfo.getString("properties"));

					geofenceDatas.add(new GeofenceData(regionInfo.getString("regionId"), regionInfo.getString("regionName"), regionInfo.getString("category"), regionInfo.getString("parentId"),
							regionInfo.getString("description"), properties.getString("guName"), properties.getString("doName"), properties.getString("viewName")));
				}
			}

			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}

		return geofenceDatas;
	}

	public void requestGeofencingBaseData(final String category, final String keyword, OnGeofencingBaseDataReceivedCallback listener)
	{
		baseDataListener = listener;

		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					InputStream is = null;
					InputStreamReader ir = null;

					StringBuilder uri = new StringBuilder();
					uri.append(baseUrl);
					uri.append("geofencing/regions?version=1&appKey=").append(URLEncoder.encode(api_key, "UTF-8"));

					uri.append("&count=20");
					uri.append("&categories=").append(URLEncoder.encode(category, "UTF-8"));
					uri.append("&searchType=KEYWORD");
					uri.append("&searchKeyword=").append(URLEncoder.encode(keyword, "UTF-8"));

					HttpsURLConnection https = getHttps(uri.toString(), 5000, 5000);

					is = https.getInputStream();
					ir = new InputStreamReader(is, "utf-8");

					StringBuffer sb = new StringBuffer();

					int c;

					while ((c = ir.read()) != -1)
					{
						sb.append((char) c);
					}

					ArrayList<GeofenceData> datas = toGeofenceData(sb.toString());

					mHandler.sendMessage(mHandler.obtainMessage(MSG_BASE_DATA_RECEIVE, datas));

					if (is != null)
						closeStream(is);
					if (ir != null)
						closeStream(ir);

				} catch (Exception e)
				{
					Log.d("Exception", e.toString());
					e.printStackTrace();
				}
			}

		}.start();
	}
//backup source
//	public void requestGeofencingPolygon(final GeofenceData data, OnGeofencingPolygonCreatedCallback listener)
//	{
//		if (data != null)
//		{
//			polygonListener = listener;
//
//			new Thread()
//			{
//				@Override
//				public void run()
//				{
//					try
//					{
//						InputStream is = null;
//						InputStreamReader ir = null;
//
//						StringBuilder uri = new StringBuilder();
//						uri.append(baseUrl);
//						uri.append("geofencing/regions/").append(data.getRegionId());
//
//						uri.append("?version=1&appKey=").append(URLEncoder.encode(api_key, "UTF-8"));
//						uri.append("&resCoordType=").append(URLEncoder.encode("WGS84GEO", "UTF-8"));
//
//						HttpsURLConnection https = getHttps(uri.toString(), 5000, 5000);
//
//						is = https.getInputStream();
//						ir = new InputStreamReader(is, "utf-8");
//
//						JsonReader jReader = new JsonReader(ir);
//						jReader.setLenient(true);
//						jReader.beginObject();
//
//						TMapPolygon polygon = new TMapPolygon();
//						polygon.setID("POLYGON_GEOFENCE");
//						polygon.setAreaAlpha(70);
//						polygon.setPolygonWidth(8);
//						polygon.setLineColor(Color.RED);
//
//						while (jReader.hasNext())
//						{
//							String name = jReader.nextName();
//
//							if (name.equals("features"))
//							{
//								jReader.beginArray();
//								while (jReader.hasNext())
//								{
//									jReader.beginObject();
//									while (jReader.hasNext())
//									{
//										String name2 = jReader.nextName();
//										if (name2.equals("geometry"))
//										{
//											jReader.beginObject();
//											while (jReader.hasNext())
//											{
//												String name3 = jReader.nextName();
//												if (name3.equals("coordinates"))
//												{
//													jReader.beginArray();
//
//													while (jReader.hasNext())
//													{
//														jReader.beginArray();
//														int i = 0;
//														while (jReader.hasNext())
//														{
//															jReader.beginArray();
//															while (jReader.hasNext())
//															{
//																double longitude = Double.parseDouble(jReader.nextString().trim());
//																double latitude = Double.parseDouble(jReader.nextString().trim());
//
//																polygon.addPolygonPoint(new TMapPoint(latitude, longitude));
//																i++;
//															}
//
//															jReader.endArray();
//														}
//														// Log.d("Total Count = ",
//														// i + "");
//														jReader.endArray();
//													}
//													jReader.endArray();
//													// jReader.skipValue();
//												}
//
//												else
//													jReader.skipValue();
//											}
//											jReader.endObject();
//										}
//
//										else
//											jReader.skipValue();
//									}
//									jReader.endObject();
//								}
//								jReader.endArray();
//							}
//
//							else
//								jReader.skipValue();
//						}
//
//						mHandler.sendMessage(mHandler.obtainMessage(MSG_POLYGON_DATA_RECEIVE, polygon));
//
//						jReader.endObject();
//						jReader.close();
//
//						if (is != null)
//							closeStream(is);
//						if (ir != null)
//							closeStream(ir);
//
//					} catch (Exception e)
//					{
//						Log.d("Exception", e.toString());
//						e.printStackTrace();
//					}
//				}
//
//			}
//
//			.start();
//		}
//
//		else if (listener != null)
//			listener.onReceived(new TMapPolygon());
//
//	}

	public void requestGeofencingPolygon(final GeofenceData data, OnGeofencingPolygonCreatedCallback listener)
	{
		if (data != null)
		{
			polygonListener = listener;

			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						ArrayList<TMapPolygon> polygons = new ArrayList<TMapPolygon>();
						
						InputStream is = null;
						InputStreamReader ir = null;

						StringBuilder uri = new StringBuilder();
						uri.append(baseUrl);
						uri.append("geofencing/regions/").append(data.getRegionId());

						uri.append("?version=1&appKey=").append(URLEncoder.encode(api_key, "UTF-8"));
						uri.append("&resCoordType=").append(URLEncoder.encode("WGS84GEO", "UTF-8"));

						HttpsURLConnection https = getHttps(uri.toString(), 5000, 5000);

						is = https.getInputStream();
						ir = new InputStreamReader(is, "utf-8");

						JsonReader jReader = new JsonReader(ir);
						jReader.setLenient(true);
						jReader.beginObject();

//						TMapPolygon polygon = new TMapPolygon();
//						polygon.setID("POLYGON_GEOFENCE");
//						polygon.setAreaAlpha(70);
//						polygon.setPolygonWidth(8);
//						polygon.setLineColor(Color.RED);

						while (jReader.hasNext())
						{
							String name = jReader.nextName();

							if (name.equals("features"))
							{
								jReader.beginArray();
								while (jReader.hasNext())
								{
									jReader.beginObject();
									while (jReader.hasNext())
									{
										String name2 = jReader.nextName();
										if (name2.equals("geometry"))
										{
											jReader.beginObject();
											while (jReader.hasNext())
											{
												String name3 = jReader.nextName();
												if (name3.equals("coordinates"))
												{
													jReader.beginArray(); // 첫번째 꺽쇠 열음
													int i = 0;
													while (jReader.hasNext()) //첫번째 꺽쇠 안에 데이터가 있는동안 루프
													{
														jReader.beginArray(); //첫번째 꺽쇠 안에 두번째 꺽쇠 열음
														
														TMapPolygon polygon = new TMapPolygon();
														polygon.setID("POLYGON_GEOFENCE"+i);
														polygon.setAreaAlpha(70);
														polygon.setPolygonWidth(8);
														polygon.setLineColor(Color.RED);
														
														while (jReader.hasNext()) //두번째 꺽쇠 안에 데이터가 있는지
														{
															jReader.beginArray(); // 데이터 있으면 실제 좌표 꺽쇠 열음
															//while (jReader.hasNext())
															//{
																double longitude = Double.parseDouble(jReader.nextString().trim());// 좌표 취하고
																double latitude = Double.parseDouble(jReader.nextString().trim());

																polygon.addPolygonPoint(new TMapPoint(latitude, longitude));
																
															//}

															jReader.endArray(); // 좌표 꺽쇠 닫음
														}
														
														polygons.add(polygon);
														
														// Log.d("Total Count = ",
														// i + "");
														jReader.endArray(); //두번째 꺽쇠안에 데이터가 없으면 탈출하여 꺽쇠 닫음
														//jReader.skipValue();//?
														i++;
													}
													
													jReader.endArray(); //첫번째 꺽쇠안에 더이상 데이터가 없으면 탈출하여 꺽쇠 닫음
													// jReader.skipValue();
												}

												else
													jReader.skipValue();
											}
											jReader.endObject();
										}

										else
											jReader.skipValue();
									}
									jReader.endObject();
								}
								jReader.endArray();
							}

							else
								jReader.skipValue();
						}

						mHandler.sendMessage(mHandler.obtainMessage(MSG_POLYGON_DATA_RECEIVE, polygons));

						jReader.endObject();
						jReader.close();

						if (is != null)
							closeStream(is);
						if (ir != null)
							closeStream(ir);

					} catch (Exception e)
					{
						Log.d("Exception", e.toString());
						e.printStackTrace();
					}
				}

			}

			.start();
		}

		else if (listener != null)
			listener.onReceived(new ArrayList<TMapPolygon>());

	}
	
	
	private HttpsURLConnection getHttps(String url, int connTimeout, int readTimeout)
	{
		trustAllHosts();

		HttpsURLConnection https = null;
		try
		{
			https = (HttpsURLConnection) new URL(url).openConnection();
			https.setConnectTimeout(connTimeout);
			https.setReadTimeout(readTimeout);
			https.setRequestProperty("appKey", api_key);
			https.setRequestProperty("Accept", "application/json");
		}

		catch (MalformedURLException e)
		{

			return null;
		}

		catch (IOException e)
		{

			return null;
		}

		return https;
	}

	private void trustAllHosts()
	{
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return new X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
			}
		} };

		try
		{
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e)
		{

		}
	}

	private String getContentFromNode(Element item, String tagName)
	{
		NodeList list = item.getElementsByTagName(tagName);
		if (list.getLength() > 0)
		{
			if (list.item(0).getFirstChild() != null)
			{
				return list.item(0).getFirstChild().getNodeValue();
			}
		}
		return null;
	}

	private static void closeStream(Closeable stream)
	{
		try
		{
			if (stream != null)
			{
				stream.close();
			}
		} catch (IOException e)
		{

		}
	}

	public String getRegionTypeFromOrder(int order)
	{
		if (order == 1)
			return "gu_gun";

		else if (order == 2)
			return "legalDong";

		else if (order == 3)
			return "adminDong";

		return "city_do";
	}

	public interface OnGeofencingBaseDataReceivedCallback
	{
		public void onReceived(ArrayList<GeofenceData> datas);
	}

	public interface OnGeofencingPolygonCreatedCallback
	{
		public void onReceived(ArrayList<TMapPolygon> polygon);
	}
}
