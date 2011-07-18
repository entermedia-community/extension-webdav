importPackage( Packages.com.openedit.util );
importPackage( Packages.java.util );
importPackage( Packages.java.io );
importPackage( Packages.java.lang );
importPackage( Packages.com.openedit.modules.update );

var war = "http://dev.entermediasoftware.com/jenkins/job/entermedia-webdav/lastSuccessfulBuild/artifact/deploy/ROOT.war";

var root = moduleManager.getBean("root").getAbsolutePath();
var web = root + "/WEB-INF";
var tmp = web + "/tmp";

log.add("1. DOWNLOADED THE LATEST WEBDAV BUILD");
var downloader = new Downloader();
downloader.download( war, tmp + "/ROOT.war");

log.add("2. UNZIP WAR FILE");
var unziper = new ZipUtil();
unziper.unzip(  tmp + "/ROOT.war",  tmp );

log.add("3. REPLACE JARS");
var files = new FileUtils();

files.deleteMatch( web + "/lib/eiistrainer*.jar");
files.deleteMatch( web + "/lib/eiiusersystem*.jar");
files.deleteMatch( web + "/lib/jtidy*.jar");
files.deleteMatch( web + "/lib/bsf*.jar");
files.deleteMatch( web + "/lib/js*.jar");
files.deleteMatch( web + "/lib/repository*.jar");

files.deleteMatch( web + "/lib/spring*.jar");
files.deleteMatch( web + "/lib/lucene*.jar");
files.deleteMatch( web + "/lib/velocity*.jar");
files.deleteMatch( web + "/lib/commons-fileupload*.jar");
files.deleteMatch( web + "/lib/commons-httpclient*.jar");
files.deleteMatch( web + "/lib/commons-logging*.jar");
files.deleteMatch( web + "/lib/commons-collections*.jar");
files.deleteMatch( web + "/lib/commons-codec*.jar");
files.deleteMatch( web + "/lib/dom4j*.jar");
files.deleteMatch( web + "/lib/activation*.jar");
files.deleteMatch( web + "/lib/mail*.jar");
files.deleteMatch( web + "/lib/jazzy*.jar");
files.deleteMatch( web + "/lib/openedit-editor*.jar");
files.deleteMatch( web + "/lib/openedit-3*.jar");
files.deleteMatch( web + "/lib/openedit-4*.jar");
files.deleteMatch( web + "/lib/openedit-5*.jar");
files.deleteMatch( web + "/lib/jakarta-*.jar");

files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-9*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-8*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-7*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-6*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-5*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-editor*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/spring*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/lucene*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/velocity*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/commons-*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/dom4j*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/activation*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/bsf*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/js*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/mail*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/jazzy*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/jakarta-oro*.jar", web + "/lib/");
var requiresRestart = false; 

log.add("4. UPGRADE BASE DIR. (moved into the /WEB-INF/base/ directory)");
files.deleteAll( root + "/base/openedit");
files.deleteAll( root + "/WEB-INF/base/openedit");
files.copyFiles( tmp + "/WEB-INF/base/openedit", root + "/WEB-INF/base/openedit");


log.add("5. UPGRADE BASE DIR. (moved into the /WEB-INF/base/ directory)");
files.deleteAll( root + "/WEB-INF/bin");
files.copyFiles( tmp + "/WEB-INF/bin", root + "/WEB-INF/bin");


log.add("6. CLEAN UP");
files.deleteAll(tmp);


if (requiresRestart == true)
{
	log.add("************");
	log.add("NOTE: Duplicate jars detected in WEB-INF/lib directory.");
	log.add("Please shut down server and manually delete older jars.");
	log.add("************");
}
else
{
	log.add("6. UPGRADE COMPLETED");
}
