package Managers;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import Utilities.Constants;
import Utilities.Utilities;

public class DownloadManager {
	
	private List<String> scriptNames;
	private List<String> subtitleNames;
	private List<String> scriptUrls;
	private List<String> subtitleIds;
	private List<String> movieNames;
	
	public DownloadManager() {
		Utilities.log("[" + Constants.INFO + "] " + "DownloadManager\n");
		this.scriptNames = new ArrayList<String>();
		this.subtitleNames = new ArrayList<String>();
		this.scriptUrls = new ArrayList<String>();
		this.subtitleIds = new ArrayList<String>();
		this.movieNames = new ArrayList<String>();
	}
	
	public List<String> getScriptNames() {
		return this.scriptNames;
	}
	
	public List<String> getSubtitleNames() {
		return this.subtitleNames;
	}
	
	public List<String> getMovieNames() {
		return this.movieNames;
	}
	
	private boolean addMovieName(String movieName) {
		return this.movieNames.add(movieName);
	}
	
	private boolean addScriptName(String scriptName) {
		return this.scriptNames.add(scriptName);
	}
	
	private boolean addSubtitleName(String subtitleName) {
		return this.subtitleNames.add(subtitleName);
	}
	
	private boolean addScriptUrl(String scriptUrl) {
		return this.scriptUrls.add(scriptUrl);
	}
	
	private boolean addSubtitleId(String subtitleId) {
		return this.subtitleIds.add(subtitleId);
	}
	
	private String modifySubtitle(String text) {
		StringBuilder result = new StringBuilder();
		String[] lines = text.split("\n");
		
		int lastNotEmpty = -1;
		for (int i = lines.length - 1; i >= 0; i--) {
			if (!lines[i].trim().isEmpty()) {
				lastNotEmpty = i;
				break;
			}
		}
		
		int emptyLine = -1;
		for (int i = lastNotEmpty; i >= 0; i--) {
			String line = lines[i];
			if (line.trim().isEmpty()) {
				emptyLine = i;
				break;
			}
		}
		emptyLine = Integer.parseInt(lines[emptyLine + 1]);

		int subtitleIndex = 0;
		boolean firstSubtitleUnit = true;
		boolean lastLineEmpty = false;
		for (String line: lines) {
			if (firstSubtitleUnit && line.trim().isEmpty()) {
				firstSubtitleUnit = false;
			}
			if (line.trim().isEmpty()) {
				subtitleIndex++;
				if (subtitleIndex == emptyLine - 1) {
					break;
				}
				lastLineEmpty = true;
				if (subtitleIndex != 1) {
					result.append("\n");
				}
				continue;
			}
			if (firstSubtitleUnit) {
				continue;
			}
			if (lastLineEmpty) {
				result.append(subtitleIndex);
				result.append("\n");
				lastLineEmpty = false;
				continue;
			}
			result.append(line);
			result.append("\n");
		}
		return result.toString();
	}
	
	private void downloadSubtitles(String subtitlesFolder) {
		int numberOfMovies = this.scriptNames.size();
		boolean loggedIn = false;
		try {
			Object response = null;
			XmlRpcClient client = null;
			String token = null;
			
			for (int i = 0; i < numberOfMovies; i++) {
				String subtitleId = this.subtitleIds.get(i);
				File subtitleFile = new File(subtitlesFolder + File.separator + this.subtitleNames.get(i));
				if (!subtitleFile.exists()) {
					Utilities.log("[" + Constants.INFO + "] " + "Download " + this.movieNames.get(i) + " subtitle file\n");
					if (!loggedIn) {
						loggedIn = true;
						XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
						config.setServerURL(new URL("http://api.opensubtitles.org/xml-rpc"));
						client = new XmlRpcClient();
						client.setConfig(config);
						
						String[] loginParameters = new String[4];
						loginParameters[0] = ""; // username
						loginParameters[1] = ""; // password
						loginParameters[2] = "eng"; // language
						loginParameters[3] = "OSTestUserAgent"; // default username
								
						response = client.execute("LogIn", loginParameters);
						if (!(response instanceof Map<?, ?>)) {
							Utilities.log("[" + Constants.INFO + "] " + "OpenSubtitles.org: Login response isn't a Map object");
							System.exit(1);
						}
						Map<? extends Object, ? extends Object> loginResponse = (Map<?, ?>)response;
						String status = (String)loginResponse.get("status");
						if (!status.startsWith("2")) {
							Utilities.log("[" + Constants.INFO + "] " + "Cannot login in open subtitles api\n");
						}
						token = (String)loginResponse.get("token");
					}
					
			        String[] searchIMDBParams = new String[2];
			        searchIMDBParams[0] = token;
			        searchIMDBParams[1] = this.movieNames.get(i).replaceAll("_", " ");
			        
			        response = client.execute("SearchMoviesOnIMDB", searchIMDBParams);
			        if (!(response instanceof Map<?, ?>)) {
						Utilities.log("[" + Constants.INFO + "] " + subtitleFile + " OpenSubtitles.org: SearchMoviesOnIMDB response isn't a Map object");
						System.exit(1);
					}
			        
			        Map<? extends Object,? extends Object> searchIMDBResponse = (Map<?, ?>)response;
			        Map<Object, Object> searchParams = new HashMap<Object, Object>();
			        searchParams.put("sublanguageid", "eng");
			        searchParams.put("imdbid", ((Map<?, ?>)((Object[])searchIMDBResponse.get("data"))[0]).get("id"));
			        
			        Object[] searchList = new Object[1];
			        searchList[0] = searchParams;
			       
			        Object[] searchSubtitleParams = new Object[2];
			        searchSubtitleParams[0] = token;
			        searchSubtitleParams[1] = (Object)searchList;
			        
			        response = client.execute("SearchSubtitles", searchSubtitleParams);
			        if (!(response instanceof Map<?, ?>)) {
						Utilities.log("[" + Constants.INFO + "] " + subtitleFile + " OpenSubtitles.org: SearchSubtitles response isn't a Map object");
						System.exit(1);
					}
			        
			        Map<? extends Object,? extends Object> searchSubtitleResponse = (Map<?, ?>)response;
			        Map<? extends Object,? extends Object> subtitleMap = null;
			        Object[] subtitleData = (Object[])searchSubtitleResponse.get("data");
			        
			        for (Object subtitle: subtitleData) {
			        	subtitleMap = (Map<?, ?>)subtitle;
			        	String id = subtitleMap.get("SubtitlesLink").toString();
			        	if (id.contains("/" + subtitleId + "/")) {
			        		break;
			        	}
			        }
			        
			        String archiveId = (String)subtitleMap.get("SubDownloadLink");
			        archiveId = archiveId.substring(archiveId.lastIndexOf("/") + 1);
			        archiveId = archiveId.substring(0, archiveId.indexOf(".gz"));
			        
					Map<Object, Object> downloadData = new HashMap<Object, Object>();
					downloadData.put("data", archiveId);
					Object[] downloadParams = new Object[2];
					downloadParams[0] = token;
					downloadParams[1] = downloadData;
					
					response = client.execute("DownloadSubtitles", downloadParams);
					if (!(response instanceof Map<?, ?>)) {
						Utilities.log("[" + Constants.INFO + "] " + subtitleFile + " OpenSubtitles.org: DownloadSubtitles response isn't a Map object");
						System.exit(1);
					}
					
					Map<? extends Object,? extends Object> downloadResponse = (Map<?, ?>)response; 
					Map<? extends Object,? extends Object> downloadBase64Response = (Map<?, ?>)((Object[])downloadResponse.get("data"))[0];
					
					byte[] byteContent = DatatypeConverter.parseBase64Binary((String)(downloadBase64Response.get("data")));
					BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(byteContent))));
					StringBuilder text = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						text.append(line);
						text.append("\n");
					}
					Utilities.writeFile(this.modifySubtitle(text.toString()), subtitleFile.getPath());
					reader.close();
				}
				else {
					Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " subtitle file\n");					
				}
			}
			
			if (loggedIn) {
				String[] logoutParams = new String[] {token};
				client.execute("LogOut", logoutParams);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Downloadeaza scriputurile si subtitrarile de la link-urile citite anterior
	 * 
	 */
	private void downloadScripts(String scriptsFolder) {
		int numberOfMovies = this.scriptNames.size();
		for (int i = 0; i < numberOfMovies; i++) {
			try {
				File scriptFile = new File(scriptsFolder + File.separator + this.scriptNames.get(i));
				if (!scriptFile.exists()) {
					Utilities.log("[" + Constants.INFO + "] " + "Download " + this.movieNames.get(i) + " script file\n");
					
					URL scriptUrl = new URL(this.scriptUrls.get(i));
					BufferedReader reader = new BufferedReader(new InputStreamReader(scriptUrl.openStream(), StandardCharsets.ISO_8859_1));
					PrintWriter writer = new PrintWriter(scriptFile, "ISO-8859-1");
					String line;
					while ((line = reader.readLine()) != null) {
						writer.write(line + "\n");
					}
					reader.close();
					writer.close();
				}
				else {
					Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " script file\n");			
				}
			} catch (MalformedURLException e) {
				Utilities.log("[" + Constants.EXCEPTION + "] " + "[DownloadManager]: Something went wrong! Invalid URL for a script " + scriptUrls.get(i) + "\n");
			} catch (IOException e1) {
				Utilities.log("[" + Constants.EXCEPTION + "] " + "[DownloadManager]: Something went wrong! Script content problem\n");
			}
		}
	}
	
	/**
	 * Citeste din fisierul movieInfos numele filmului si link-urile la care se gasesc scriptul si subtitrarea
	 * Liniile care incep cu // sunt considerate comentarii si nu se iau in considerare
	 */
	public void start() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Constants.MOVIES_INFOS));
			String line;
			
			String step1Path = Constants.STEP1_FOLDER;
			String scriptsPath = Constants.STEP1_SCRIPTS_FOLDER;
			String subtitlesPath =  Constants.STEP1_SUBTITLES_FOLDER;
					
			Utilities.createFolder(step1Path);
			Utilities.createFolder(scriptsPath);
			Utilities.createFolder(subtitlesPath);
			
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//")) {
					continue;
				}
				String[] tokens = line.split(" ");
				this.addMovieName(tokens[0]);
				this.addScriptName(tokens[0] + "_" + Constants.SCRIPT);
				this.addSubtitleName(tokens[0] + "_" + Constants.SUBTITLE);
				this.addScriptUrl(tokens[1]);
				this.addSubtitleId(tokens[2]);
			}
			this.downloadScripts(scriptsPath);
			this.downloadSubtitles(subtitlesPath);
			reader.close();
		} catch (FileNotFoundException e) {
			Utilities.log("[" + Constants.EXCEPTION + "] " + "[DownloadManager]: Something went wrong! Movie Infos not found\n");
			System.exit(1);
		} catch (IOException e1) {
			Utilities.log("[" + Constants.EXCEPTION + "] " + "[DownloadManager]: Something went wrong!\n");
			System.exit(1);
		}
	}
}